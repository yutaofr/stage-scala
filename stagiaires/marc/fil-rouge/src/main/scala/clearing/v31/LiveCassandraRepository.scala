package clearing.v31

import clearing.v30.ValidatedEvent
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{AsyncResultSet, BoundStatement, PreparedStatement, Row}
import java.time.LocalDate
import java.util.concurrent.CompletionStage
import scala.jdk.CollectionConverters.*

object CassandraStatementQueries:
  val selectStates =
    """SELECT payload_fingerprint, stage, decision_kind, source_topic,
      |source_partition, source_offset, updated_at
      |FROM processing_state WHERE event_key = ?""".stripMargin

  val markState =
    """INSERT INTO processing_state
      |(event_key, payload_fingerprint, stage, decision_kind, source_topic,
      |source_partition, source_offset, updated_at)
      |VALUES (?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin

  val saveHistory =
    """INSERT INTO clearing_history_by_day
      |(clearing_date, bucket, occurred_at, event_key, transaction_id,
      |sender, receiver, settlement_amount, currency, status)
      |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin

  val saveMovement =
    """INSERT INTO transactions_by_bank_day
      |(bank_id, clearing_date, occurred_at, event_key, direction,
      |counterparty, amount, currency)
      |VALUES (?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin

  val savePosition =
    """INSERT INTO bank_positions
      |(bank_id, clearing_date, event_key, contribution, transaction_count,
      |occurred_at) VALUES (?, ?, ?, ?, ?, ?)""".stripMargin

  val savePair =
    """INSERT INTO pair_activity_by_day
      |(clearing_date, bank_pair, event_key, amount, transaction_count)
      |VALUES (?, ?, ?, ?, ?)""".stripMargin

  val selectMovements =
    """SELECT occurred_at, event_key, direction, counterparty, amount, currency
      |FROM transactions_by_bank_day
      |WHERE bank_id = ? AND clearing_date = ? LIMIT ?""".stripMargin

  val selectPositions =
    """SELECT event_key, contribution, transaction_count, occurred_at
      |FROM bank_positions WHERE bank_id = ? AND clearing_date = ?""".stripMargin

  val selectHistoryBucket =
    """SELECT occurred_at, event_key, transaction_id, sender, receiver,
      |settlement_amount, currency, status
      |FROM clearing_history_by_day
      |WHERE clearing_date = ? AND bucket = ?""".stripMargin

  val selectPairs =
    """SELECT bank_pair, event_key, amount, transaction_count
      |FROM pair_activity_by_day WHERE clearing_date = ?""".stripMargin

  val all = Map(
    "selectStates" -> selectStates,
    "markState" -> markState,
    "saveHistory" -> saveHistory,
    "saveMovement" -> saveMovement,
    "savePosition" -> savePosition,
    "savePair" -> savePair,
    "selectMovements" -> selectMovements,
    "selectPositions" -> selectPositions,
    "selectHistoryBucket" -> selectHistoryBucket,
    "selectPairs" -> selectPairs
  )

final case class CassandraStatements(
  selectStates: PreparedStatement,
  markState: PreparedStatement,
  saveHistory: PreparedStatement,
  saveMovement: PreparedStatement,
  savePosition: PreparedStatement,
  savePair: PreparedStatement,
  selectMovements: PreparedStatement,
  selectPositions: PreparedStatement,
  selectHistoryBucket: PreparedStatement,
  selectPairs: PreparedStatement
)

object CassandraStatements:
  def prepare(session: CqlSession): CassandraStatements =
    CassandraStatements(
      session.prepare(CassandraStatementQueries.selectStates),
      session.prepare(CassandraStatementQueries.markState),
      session.prepare(CassandraStatementQueries.saveHistory),
      session.prepare(CassandraStatementQueries.saveMovement),
      session.prepare(CassandraStatementQueries.savePosition),
      session.prepare(CassandraStatementQueries.savePair),
      session.prepare(CassandraStatementQueries.selectMovements),
      session.prepare(CassandraStatementQueries.selectPositions),
      session.prepare(CassandraStatementQueries.selectHistoryBucket),
      session.prepare(CassandraStatementQueries.selectPairs)
    )

final class LiveCassandraRepository(
  session: CqlSession,
  statements: CassandraStatements
) extends DurableRepository:
  def states(eventKey: String): CompletionStage[List[ProcessingState]] =
    allRows(session.executeAsync(statements.selectStates.bind(eventKey)))
      .thenApply: rows =>
        rows.map: row =>
          ProcessingState(
            eventKey,
            row.getString("payload_fingerprint"),
            ProcessingStage.valueOf(row.getString("stage")),
            row.getString("decision_kind"),
            row.getString("source_topic"),
            row.getInt("source_partition"),
            row.getLong("source_offset"),
            row.getInstant("updated_at")
          )

  def markState(state: ProcessingState): CompletionStage[Unit] =
    executeUnit(
      statements.markState
        .boundStatementBuilder()
        .setString(0, state.eventKey)
        .setString(1, state.payloadFingerprint)
        .setString(2, state.stage.toString)
        .setString(3, state.decisionKind)
        .setString(4, state.topic)
        .setInt(5, state.partition)
        .setLong(6, state.offset)
        .setInstant(7, state.updatedAt)
        .build()
    )

  def saveValidated(
    identity: DurableIdentity,
    event: ValidatedEvent
  ): CompletionStage[Unit] =
    val projection = ValidatedProjectionSet.from(identity, event)
    val history = projection.history
    val historyWrite = statements.saveHistory
      .boundStatementBuilder()
      .setLocalDate(0, projection.date)
      .setShort(1, projection.bucket.toShort)
      .setInstant(2, history.occurredAt)
      .setString(3, history.eventKey)
      .setInt(4, history.transactionId)
      .setString(5, history.sender)
      .setString(6, history.receiver)
      .setBigDecimal(7, history.amount.bigDecimal)
      .setString(8, history.currency)
      .setString(9, history.status)
      .build()

    val movementWrites = projection.movements.map: movement =>
      statements.saveMovement
        .boundStatementBuilder()
        .setString(0, movement.bank)
        .setLocalDate(1, movement.date)
        .setInstant(2, movement.occurredAt)
        .setString(3, movement.eventKey)
        .setString(4, movement.direction)
        .setString(5, movement.counterparty)
        .setBigDecimal(6, movement.amount.bigDecimal)
        .setString(7, movement.currency)
        .build()

    val positionWrites = projection.positions.map: position =>
      statements.savePosition
        .boundStatementBuilder()
        .setString(0, position.bank)
        .setLocalDate(1, position.date)
        .setString(2, position.eventKey)
        .setBigDecimal(3, position.amount.bigDecimal)
        .setInt(4, position.transactionCount)
        .setInstant(5, position.occurredAt)
        .build()

    val pair = projection.pairActivity
    val pairWrite = statements.savePair
      .boundStatementBuilder()
      .setLocalDate(0, pair.date)
      .setString(1, pair.pair)
      .setString(2, pair.eventKey)
      .setBigDecimal(3, pair.amount.bigDecimal)
      .setInt(4, pair.transactionCount)
      .build()

    CompletionStages.sequence(
      (historyWrite :: movementWrites ::: positionWrites ::: List(pairWrite))
        .map(executeUnit)
    )

  def movementsByBank(
    bank: String,
    date: LocalDate,
    limit: Int
  ): CompletionStage[List[BankMovement]] =
    require(limit > 0, "limit doit être strictement positif")
    val statement = statements.selectMovements
      .boundStatementBuilder()
      .setString(0, bank)
      .setLocalDate(1, date)
      .setInt(2, limit)
      .build()
    allRows(session.executeAsync(statement)).thenApply: rows =>
      rows.map: row =>
        BankMovement(
          bank,
          date,
          row.getString("event_key"),
          row.getString("direction"),
          row.getString("counterparty"),
          BigDecimal(row.getBigDecimal("amount")),
          row.getString("currency"),
          row.getInstant("occurred_at")
        )

  def positionByBank(
    bank: String,
    date: LocalDate
  ): CompletionStage[BankPosition] =
    val statement = statements.selectPositions
      .boundStatementBuilder()
      .setString(0, bank)
      .setLocalDate(1, date)
      .build()
    allRows(session.executeAsync(statement)).thenApply: rows =>
      BankPosition(
        bank,
        date,
        rows.map(row => BigDecimal(row.getBigDecimal("contribution"))).sum,
        rows.map(_.getInt("transaction_count")).sum
      )

  def historyByDate(date: LocalDate): CompletionStage[List[HistoryRow]] =
    val reads = (0 until HistoryBucket.Count).toList.map: bucket =>
      val statement = statements.selectHistoryBucket
        .boundStatementBuilder()
        .setLocalDate(0, date)
        .setShort(1, bucket.toShort)
        .build()
      allRows(session.executeAsync(statement))

    CompletionStages.values(reads).thenApply: pages =>
      pages.flatten
        .map(historyRow)
        .sortBy(_.occurredAt)(Ordering[java.time.Instant].reverse)

  def topPairsByDate(
    date: LocalDate,
    limit: Int
  ): CompletionStage[List[PairSummary]] =
    require(limit > 0, "limit doit être strictement positif")
    val statement = statements.selectPairs.bind(date)
    allRows(session.executeAsync(statement)).thenApply: rows =>
      rows
        .groupBy(_.getString("bank_pair"))
        .map: (pair, pairRows) =>
          PairSummary(
            pair,
            pairRows.map(row => BigDecimal(row.getBigDecimal("amount"))).sum,
            pairRows.map(_.getInt("transaction_count")).sum
          )
        .toList
        .sortBy(summary => (-summary.amount, summary.pair))
        .take(limit)

  private def historyRow(row: Row): HistoryRow =
    HistoryRow(
      row.getString("event_key"),
      row.getInt("transaction_id"),
      row.getString("sender"),
      row.getString("receiver"),
      BigDecimal(row.getBigDecimal("settlement_amount")),
      row.getString("currency"),
      row.getString("status"),
      row.getInstant("occurred_at")
    )

  private def executeUnit(statement: BoundStatement): CompletionStage[Unit] =
    session.executeAsync(statement).thenApply(_ => ())

  private def allRows(
    first: CompletionStage[AsyncResultSet]
  ): CompletionStage[List[Row]] =
    first.thenCompose(result => collectRows(result, Nil))

  private def collectRows(
    result: AsyncResultSet,
    accumulated: List[Row]
  ): CompletionStage[List[Row]] =
    val next = accumulated ::: result.currentPage().asScala.toList
    if result.hasMorePages then
      result.fetchNextPage().thenCompose(page => collectRows(page, next))
    else CompletionStages.successful(next)
