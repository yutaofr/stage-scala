package clearing.v31

import clearing.v30.ValidatedEvent
import java.time.{Instant, LocalDate}
import java.time.ZoneOffset

enum ProcessingStage:
  case Received, Projected, Completed

final case class DurableIdentity(
  eventKey: String,
  payloadFingerprint: String
)

final case class ProcessingState(
  eventKey: String,
  payloadFingerprint: String,
  stage: ProcessingStage,
  decisionKind: String,
  topic: String,
  partition: Int,
  offset: Long,
  updatedAt: Instant
)

final case class HistoryRow(
  eventKey: String,
  transactionId: Int,
  sender: String,
  receiver: String,
  amount: BigDecimal,
  currency: String,
  status: String,
  occurredAt: Instant
)

final case class BankMovement(
  bank: String,
  date: LocalDate,
  eventKey: String,
  direction: String,
  counterparty: String,
  amount: BigDecimal,
  currency: String,
  occurredAt: Instant
)

final case class PositionContribution(
  bank: String,
  date: LocalDate,
  eventKey: String,
  amount: BigDecimal,
  transactionCount: Int,
  occurredAt: Instant
)

final case class PairActivity(
  date: LocalDate,
  pair: String,
  eventKey: String,
  amount: BigDecimal,
  transactionCount: Int
)

final case class BankPosition(
  bank: String,
  date: LocalDate,
  amount: BigDecimal,
  transactionCount: Int
)

final case class PairSummary(
  pair: String,
  amount: BigDecimal,
  transactionCount: Int
)

final case class DurableSnapshot(
  historyRows: Int,
  bankMovements: Int,
  positionContributions: Int,
  pairActivities: Int
)

final case class ValidatedProjectionSet(
  date: LocalDate,
  bucket: Int,
  history: HistoryRow,
  movements: List[BankMovement],
  positions: List[PositionContribution],
  pairActivity: PairActivity
)

object ValidatedProjectionSet:
  def from(
    identity: DurableIdentity,
    event: ValidatedEvent
  ): ValidatedProjectionSet =
    val date = event.occurredAt.atZone(ZoneOffset.UTC).toLocalDate
    val amount = BigDecimal(event.settlementAmount)
    val history = HistoryRow(
      identity.eventKey,
      event.transactionId,
      event.sender,
      event.receiver,
      amount,
      event.currency,
      event.status,
      event.occurredAt
    )
    val movements = List(
      BankMovement(
        event.sender,
        date,
        identity.eventKey,
        "OUT",
        event.receiver,
        amount,
        event.currency,
        event.occurredAt
      ),
      BankMovement(
        event.receiver,
        date,
        identity.eventKey,
        "IN",
        event.sender,
        amount,
        event.currency,
        event.occurredAt
      )
    )
    val positions = List(
      PositionContribution(
        event.sender,
        date,
        identity.eventKey,
        -amount,
        1,
        event.occurredAt
      ),
      PositionContribution(
        event.receiver,
        date,
        identity.eventKey,
        amount,
        1,
        event.occurredAt
      )
    )
    val pair = List(event.sender, event.receiver).sorted.mkString("|")

    ValidatedProjectionSet(
      date,
      HistoryBucket.forEvent(identity.eventKey),
      history,
      movements,
      positions,
      PairActivity(date, pair, identity.eventKey, amount, 1)
    )

object HistoryBucket:
  val Count = 16

  def forEvent(eventKey: String): Int =
    Math.floorMod(eventKey.hashCode, Count)

object HistoryOrdering:
  def before(left: HistoryRow, right: HistoryRow): Boolean =
    if left.occurredAt != right.occurredAt then
      left.occurredAt.isAfter(right.occurredAt)
    else left.eventKey < right.eventKey
