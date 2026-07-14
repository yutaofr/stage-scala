package clearing.v31

import clearing.v30.ValidatedEvent
import java.time.{LocalDate, ZoneOffset}
import java.util.concurrent.{CompletableFuture, CompletionStage}
import scala.collection.mutable

trait DurableRepository:
  def states(eventKey: String): CompletionStage[List[ProcessingState]]

  def markState(state: ProcessingState): CompletionStage[Unit]

  def saveValidated(
    identity: DurableIdentity,
    event: ValidatedEvent
  ): CompletionStage[Unit]

  def movementsByBank(
    bank: String,
    date: LocalDate,
    limit: Int
  ): CompletionStage[List[BankMovement]]

  def positionByBank(
    bank: String,
    date: LocalDate
  ): CompletionStage[BankPosition]

  def historyByDate(date: LocalDate): CompletionStage[List[HistoryRow]]

  def topPairsByDate(
    date: LocalDate,
    limit: Int
  ): CompletionStage[List[PairSummary]]

object CompletionStages:
  def successful[A](value: A): CompletionStage[A] =
    CompletableFuture.completedFuture(value)

  def unit: CompletionStage[Unit] = successful(())

  def sequence(stages: List[CompletionStage[?]]): CompletionStage[Unit] =
    val futures = stages.map(_.toCompletableFuture)
    CompletableFuture
      .allOf(futures*)
      .thenApply(_ => ())

  def values[A](stages: List[CompletionStage[A]]): CompletionStage[List[A]] =
    val futures = stages.map(_.toCompletableFuture)
    CompletableFuture
      .allOf(futures*)
      .thenApply(_ => futures.map(_.join()))

final class InMemoryDurableRepository private () extends DurableRepository:
  private val stateRows = mutable.Map.empty[(String, String), ProcessingState]
  private val historyRows =
    mutable.Map.empty[(LocalDate, Int, java.time.Instant, String), HistoryRow]
  private val movementRows = mutable.Map.empty[
    (String, LocalDate, java.time.Instant, String, String),
    BankMovement
  ]
  private val positionRows =
    mutable.Map.empty[(String, LocalDate, String), PositionContribution]
  private val pairRows =
    mutable.Map.empty[(LocalDate, String, String), PairActivity]

  def states(eventKey: String): CompletionStage[List[ProcessingState]] =
    CompletionStages.successful:
      synchronized:
        stateRows.values
          .filter(_.eventKey == eventKey)
          .toList

  def markState(state: ProcessingState): CompletionStage[Unit] =
    synchronized:
      stateRows.update(
        state.eventKey -> state.payloadFingerprint,
        state
      )
    CompletionStages.unit

  def saveValidated(
    identity: DurableIdentity,
    event: ValidatedEvent
  ): CompletionStage[Unit] =
    val projections = ValidatedProjectionSet.from(identity, event)

    synchronized:
      historyRows.update(
        (
          projections.date,
          projections.bucket,
          event.occurredAt,
          identity.eventKey
        ),
        projections.history
      )
      projections.movements.foreach: movement =>
        movementRows.update(
          (
            movement.bank,
            movement.date,
            movement.occurredAt,
            movement.eventKey,
            movement.direction
          ),
          movement
        )
      projections.positions.foreach: position =>
        positionRows.update(
          (position.bank, position.date, position.eventKey),
          position
        )
      val activity = projections.pairActivity
      pairRows.update((activity.date, activity.pair, activity.eventKey), activity)

    CompletionStages.unit

  def movementsByBank(
    bank: String,
    date: LocalDate,
    limit: Int
  ): CompletionStage[List[BankMovement]] =
    require(limit > 0, "limit doit être strictement positif")
    CompletionStages.successful:
      synchronized:
        movementRows.values
          .filter(row => row.bank == bank && row.date == date)
          .toList
          .sortBy(_.occurredAt)(Ordering[java.time.Instant].reverse)
          .take(limit)

  def positionByBank(
    bank: String,
    date: LocalDate
  ): CompletionStage[BankPosition] =
    CompletionStages.successful:
      synchronized:
        val rows = positionRows.values.filter: row =>
          row.bank == bank && row.date == date
        BankPosition(
          bank,
          date,
          rows.map(_.amount).sum,
          rows.map(_.transactionCount).sum
        )

  def historyByDate(date: LocalDate): CompletionStage[List[HistoryRow]] =
    CompletionStages.successful:
      synchronized:
        historyRows.values
          .filter(row => row.occurredAt.atZone(ZoneOffset.UTC).toLocalDate == date)
          .toList
          .sortBy(_.occurredAt)(Ordering[java.time.Instant].reverse)

  def topPairsByDate(
    date: LocalDate,
    limit: Int
  ): CompletionStage[List[PairSummary]] =
    require(limit > 0, "limit doit être strictement positif")
    CompletionStages.successful:
      synchronized:
        pairRows.values
          .filter(_.date == date)
          .groupBy(_.pair)
          .map: (pair, rows) =>
            PairSummary(
              pair,
              rows.map(_.amount).sum,
              rows.map(_.transactionCount).sum
            )
          .toList
          .sortBy(summary => (-summary.amount, summary.pair))
          .take(limit)

  def snapshot: DurableSnapshot = synchronized:
    DurableSnapshot(
      historyRows.size,
      movementRows.size,
      positionRows.size,
      pairRows.size
    )

object InMemoryDurableRepository:
  def apply(): InMemoryDurableRepository = new InMemoryDurableRepository()
