package distributed.persistence

import clearing.model.*
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import com.datastax.oss.driver.api.core.cql.{AsyncResultSet, BoundStatement, PreparedStatement}
import distributed.kafka.RecordEnvelope
import zio.*

import java.net.InetSocketAddress
import java.time.{Instant, LocalDate}
import scala.jdk.CollectionConverters.*

final case class Position(
  bank: BankCode,
  date: LocalDate,
  amount: Money,
  transactionCount: Int
)

final case class HistoryRow(
  transactionId: TransactionId,
  sender: BankCode,
  receiver: BankCode,
  amount: Money,
  status: String,
  createdAt: Instant
)

trait ClearingRepository extends ProcessingRepository:
  def saveHistory(
    tx: Transaction,
    date: LocalDate,
    bucket: Short,
    occurredAt: Instant
  ): Task[Unit]

  def saveTransactionByBank(
    tx: Transaction,
    date: LocalDate,
    occurredAt: Instant
  ): Task[Unit]

  def saveNetPositions(
    transactionId: TransactionId,
    date: LocalDate,
    occurredAt: Instant,
    positions: List[NetPositionProjection]
  ): Task[Unit]

  def savePairActivity(tx: Transaction, date: LocalDate): Task[Unit]

  def getPositionsByBank(
    bank: BankCode,
    date: LocalDate,
    limit: Int
  ): Task[List[Position]]

  def getHistoryByDate(
    date: LocalDate,
    buckets: Range = 0 until 16
  ): Task[List[HistoryRow]]

object ClearingRepository:
  def historyBucket(transactionId: TransactionId): Short =
    Math.floorMod(transactionId.value.hashCode, 16).toShort

  val inMemory: ULayer[ClearingRepository] =
    ZLayer.fromZIO {
      for
        stages <- Ref.make(Map.empty[TransactionId, ProcessingStage])
        history <- Ref.make(
          Map.empty[(LocalDate, Short, Instant, TransactionId), HistoryRow]
        )
        positions <- Ref.make(
          Map.empty[(BankCode, LocalDate, TransactionId), Position]
        )
      yield new ClearingRepository:
        def stage(id: TransactionId): UIO[Option[ProcessingStage]] =
          stages.get.map(_.get(id))

        def markStage(
          id: TransactionId,
          stage: ProcessingStage,
          record: RecordEnvelope
        ): UIO[Unit] =
          stages.update(_.updated(id, stage)).unit

        def saveHistory(
          tx: Transaction,
          date: LocalDate,
          bucket: Short,
          occurredAt: Instant
        ): UIO[Unit] =
          val row = HistoryRow(
            tx.id,
            tx.sender,
            tx.receiver,
            tx.amount,
            tx.status.toString,
            occurredAt
          )
          history
            .update(_.updated((date, bucket, occurredAt, tx.id), row))
            .unit

        def saveTransactionByBank(
          tx: Transaction,
          date: LocalDate,
          occurredAt: Instant
        ): UIO[Unit] =
          ZIO.unit

        def saveNetPositions(
          transactionId: TransactionId,
          date: LocalDate,
          occurredAt: Instant,
          values: List[NetPositionProjection]
        ): UIO[Unit] =
          positions.update { current =>
            values.foldLeft(current) { (acc, projection) =>
              val value = Position(
                projection.bank,
                date,
                projection.amount,
                projection.transactionCount
              )
              acc.updated((projection.bank, date, transactionId), value)
            }
          }.unit

        def savePairActivity(tx: Transaction, date: LocalDate): UIO[Unit] =
          ZIO.unit

        def getPositionsByBank(
          bank: BankCode,
          date: LocalDate,
          limit: Int
        ): UIO[List[Position]] =
          positions.get.map(
            _.collect {
              case ((storedBank, storedDate, _), value)
                  if storedBank == bank && storedDate == date =>
                value
            }.take(limit).toList
          )

        def getHistoryByDate(
          date: LocalDate,
          buckets: Range
        ): UIO[List[HistoryRow]] =
          history.get.map(
            _.collect {
              case ((storedDate, bucket, _, _), row)
                  if storedDate == date && buckets.contains(bucket.toInt) =>
                row
            }.toList.sortBy(_.createdAt)(Ordering[Instant].reverse)
          )
    }

final case class PreparedStatements(
  selectStage: PreparedStatement,
  markStage: PreparedStatement,
  saveHistory: PreparedStatement,
  saveTransactionByBank: PreparedStatement,
  saveNetPosition: PreparedStatement,
  savePairActivity: PreparedStatement,
  selectPositions: PreparedStatement,
  selectHistoryBucket: PreparedStatement
)

object CassandraSession:
  val live: ZLayer[CassandraSettings, Throwable, CqlSession] =
    ZLayer.scoped {
      for
        settings <- ZIO.service[CassandraSettings]
        session <- ZIO.acquireRelease(
          ZIO.attempt {
            CqlSession
              .builder()
              .addContactPoint(new InetSocketAddress(settings.host, settings.port))
              .withLocalDatacenter(settings.datacenter)
              .withKeyspace(CqlIdentifier.fromCql(settings.keyspace))
              .build()
          }
        )(session => ZIO.attempt(session.close()).orDie)
      yield session
    }

object PreparedStatements:
  val live: ZLayer[CqlSession, Throwable, PreparedStatements] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[CqlSession] { session =>
        ZIO.attempt {
          PreparedStatements(
            selectStage = session.prepare(
              "SELECT stage FROM processing_state WHERE tx_id = ?"
            ),
            markStage = session.prepare(
              """INSERT INTO processing_state
                |(tx_id, stage, source_topic, source_partition, source_offset, updated_at)
                |VALUES (?, ?, ?, ?, ?, ?)""".stripMargin
            ),
            saveHistory = session.prepare(
              """INSERT INTO clearing_history_by_day
                |(clearing_date, bucket, created_at, tx_id, sender, receiver, amount, status)
                |VALUES (?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
            ),
            saveTransactionByBank = session.prepare(
              """INSERT INTO transactions_by_bank_day
                |(bank_id, clearing_date, created_at, tx_id, direction, counterparty, amount)
                |VALUES (?, ?, ?, ?, ?, ?, ?)""".stripMargin
            ),
            saveNetPosition = session.prepare(
              """INSERT INTO bank_positions
                |(bank_id, clearing_date, tx_id, net_position, tx_count, updated_at)
                |VALUES (?, ?, ?, ?, ?, ?)""".stripMargin
            ),
            savePairActivity = session.prepare(
              """INSERT INTO pair_activity_by_day
                |(clearing_date, bank_pair, tx_id, amount)
                |VALUES (?, ?, ?, ?)""".stripMargin
            ),
            selectPositions = session.prepare(
              """SELECT net_position, tx_count
                |FROM bank_positions
                |WHERE bank_id = ? AND clearing_date = ?
                |LIMIT ?""".stripMargin
            ),
            selectHistoryBucket = session.prepare(
              """SELECT created_at, tx_id, sender, receiver, amount, status
                |FROM clearing_history_by_day
                |WHERE clearing_date = ? AND bucket = ?""".stripMargin
            )
          )
        }
      }
    }

final class LiveClearingRepository(
  session: CqlSession,
  statements: PreparedStatements
) extends ClearingRepository:
  private def execute(statement: BoundStatement): Task[AsyncResultSet] =
    ZIO.fromCompletionStage(session.executeAsync(statement))

  def stage(id: TransactionId): Task[Option[ProcessingStage]] =
    execute(statements.selectStage.bind().setString(0, id.value)).map { result =>
      Option(result.one()).flatMap { row =>
        val value = row.getString("stage")
        ProcessingStage.values.find(_.toString == value)
      }
    }

  def markStage(
    id: TransactionId,
    stage: ProcessingStage,
    record: RecordEnvelope
  ): Task[Unit] =
    execute(
      statements.markStage
        .bind()
        .setString(0, id.value)
        .setString(1, stage.toString)
        .setString(2, record.topic)
        .setInt(3, record.partition)
        .setLong(4, record.offset)
        .setInstant(5, record.occurredAt)
    ).unit

  def saveHistory(
    tx: Transaction,
    date: LocalDate,
    bucket: Short,
    occurredAt: Instant
  ): Task[Unit] =
    execute(
      statements.saveHistory
        .bind()
        .setLocalDate(0, date)
        .setShort(1, bucket)
        .setInstant(2, occurredAt)
        .setString(3, tx.id.value)
        .setString(4, tx.sender.value)
        .setString(5, tx.receiver.value)
        .setBigDecimal(6, tx.amount.value.bigDecimal)
        .setString(7, tx.status.toString)
    ).unit

  def saveTransactionByBank(
    tx: Transaction,
    date: LocalDate,
    occurredAt: Instant
  ): Task[Unit] =
    def write(
      bank: BankCode,
      direction: String,
      counterparty: BankCode
    ): Task[Unit] =
      execute(
        statements.saveTransactionByBank
          .bind()
          .setString(0, bank.value)
          .setLocalDate(1, date)
          .setInstant(2, occurredAt)
          .setString(3, tx.id.value)
          .setString(4, direction)
          .setString(5, counterparty.value)
          .setBigDecimal(6, tx.amount.value.bigDecimal)
      ).unit

    write(tx.sender, "OUT", tx.receiver) *>
      write(tx.receiver, "IN", tx.sender)

  def saveNetPositions(
    transactionId: TransactionId,
    date: LocalDate,
    occurredAt: Instant,
    positions: List[NetPositionProjection]
  ): Task[Unit] =
    ZIO.foreachDiscard(positions) { position =>
      execute(
        statements.saveNetPosition
          .bind()
          .setString(0, position.bank.value)
          .setLocalDate(1, date)
          .setString(2, transactionId.value)
          .setBigDecimal(3, position.amount.value.bigDecimal)
          .setInt(4, position.transactionCount)
          .setInstant(5, occurredAt)
      )
    }

  def savePairActivity(tx: Transaction, date: LocalDate): Task[Unit] =
    val pair = List(tx.sender.value, tx.receiver.value).sorted.mkString("-")
    execute(
      statements.savePairActivity
        .bind()
        .setLocalDate(0, date)
        .setString(1, pair)
        .setString(2, tx.id.value)
        .setBigDecimal(3, tx.amount.value.bigDecimal)
    ).unit

  def getPositionsByBank(
    bank: BankCode,
    date: LocalDate,
    limit: Int
  ): Task[List[Position]] =
    execute(
      statements.selectPositions
        .bind()
        .setString(0, bank.value)
        .setLocalDate(1, date)
        .setInt(2, limit)
    ).map { result =>
      result.currentPage().asScala.toList.map { row =>
        Position(
          bank,
          date,
          Money(BigDecimal(row.getBigDecimal("net_position"))),
          row.getInt("tx_count")
        )
      }
    }

  def getHistoryByDate(
    date: LocalDate,
    buckets: Range
  ): Task[List[HistoryRow]] =
    ZIO
      .foreach(buckets) { bucket =>
        execute(
          statements.selectHistoryBucket
            .bind()
            .setLocalDate(0, date)
            .setShort(1, bucket.toShort)
        ).map(_.currentPage().asScala.toList)
      }
      .map(
        _.flatten.map { row =>
          HistoryRow(
            TransactionId.unsafe(row.getString("tx_id")),
            BankCode.unsafe(row.getString("sender")),
            BankCode.unsafe(row.getString("receiver")),
            Money(BigDecimal(row.getBigDecimal("amount"))),
            row.getString("status"),
            row.getInstant("created_at")
          )
        }.toList.sortBy(_.createdAt)(Ordering[Instant].reverse)
      )

object LiveClearingRepository:
  val layer
    : ZLayer[CqlSession & PreparedStatements, Nothing, ClearingRepository] =
    ZLayer.fromZIO {
      for
        session <- ZIO.service[CqlSession]
        statements <- ZIO.service[PreparedStatements]
      yield new LiveClearingRepository(session, statements)
    }
