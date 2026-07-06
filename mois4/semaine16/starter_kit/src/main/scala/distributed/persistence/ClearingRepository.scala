package distributed.persistence

import clearing.model.*
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import distributed.kafka.RecordEnvelope

import java.net.InetSocketAddress
import java.time.{Instant, LocalDate}
import java.util.concurrent.atomic.AtomicReference
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
  ): Unit

  def saveTransactionByBank(
    tx: Transaction,
    date: LocalDate,
    occurredAt: Instant
  ): Unit

  def saveNetPositions(
    transactionId: TransactionId,
    date: LocalDate,
    occurredAt: Instant,
    positions: List[NetPositionProjection]
  ): Unit

  def savePairActivity(tx: Transaction, date: LocalDate): Unit

  def getPositionsByBank(
    bank: BankCode,
    date: LocalDate,
    limit: Int
  ): List[Position]

  def getHistoryByDate(
    date: LocalDate,
    buckets: Range = 0 until 16
  ): List[HistoryRow]

object ClearingRepository:
  def historyBucket(transactionId: TransactionId): Short =
    Math.floorMod(transactionId.value.hashCode, 16).toShort

  def inMemory(): ClearingRepository =
    val stages = AtomicReference(Map.empty[TransactionId, ProcessingStage])
    val history = AtomicReference(
      Map.empty[(LocalDate, Short, Instant, TransactionId), HistoryRow]
    )
    val positions = AtomicReference(
      Map.empty[(BankCode, LocalDate, TransactionId), Position]
    )

    new ClearingRepository:
      def stage(id: TransactionId): Option[ProcessingStage] =
        stages.get().get(id)

      def markStage(
        id: TransactionId,
        s: ProcessingStage,
        record: RecordEnvelope
      ): Unit =
        stages.updateAndGet(_.updated(id, s))
        ()

      def saveHistory(
        tx: Transaction,
        date: LocalDate,
        bucket: Short,
        occurredAt: Instant
      ): Unit =
        val row = HistoryRow(
          tx.id,
          tx.sender,
          tx.receiver,
          tx.amount,
          tx.status.toString,
          occurredAt
        )
        history.updateAndGet(_.updated((date, bucket, occurredAt, tx.id), row))
        ()

      def saveTransactionByBank(
        tx: Transaction,
        date: LocalDate,
        occurredAt: Instant
      ): Unit = ()

      def saveNetPositions(
        transactionId: TransactionId,
        date: LocalDate,
        occurredAt: Instant,
        values: List[NetPositionProjection]
      ): Unit =
        positions.updateAndGet { current =>
          values.foldLeft(current) { (acc, projection) =>
            val value = Position(
              projection.bank,
              date,
              projection.amount,
              projection.transactionCount
            )
            acc.updated((projection.bank, date, transactionId), value)
          }
        }
        ()

      def savePairActivity(tx: Transaction, date: LocalDate): Unit = ()

      def getPositionsByBank(
        bank: BankCode,
        date: LocalDate,
        limit: Int
      ): List[Position] =
        positions.get().collect {
          case ((storedBank, storedDate, _), value)
              if storedBank == bank && storedDate == date =>
            value
        }.take(limit).toList

      def getHistoryByDate(
        date: LocalDate,
        buckets: Range
      ): List[HistoryRow] =
        history.get().collect {
          case ((storedDate, bucket, _, _), row)
              if storedDate == date && buckets.contains(bucket.toInt) =>
            row
        }.toList.sortBy(_.createdAt)(Ordering[Instant].reverse)

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
  def connect(settings: CassandraSettings): CqlSession =
    CqlSession
      .builder()
      .addContactPoint(new InetSocketAddress(settings.host, settings.port))
      .withLocalDatacenter(settings.datacenter)
      .withKeyspace(CqlIdentifier.fromCql(settings.keyspace))
      .build()

object PreparedStatements:
  def apply(session: CqlSession): PreparedStatements =
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

final class LiveClearingRepository(
  session: CqlSession,
  statements: PreparedStatements
) extends ClearingRepository:
  private def execute(statement: BoundStatement) =
    session.executeAsync(statement).toCompletableFuture.get()

  def stage(id: TransactionId): Option[ProcessingStage] =
    ??? // TODO : Récupérer l'état d'avancement de la transaction dans processing_state

  def markStage(
    id: TransactionId,
    stage: ProcessingStage,
    record: RecordEnvelope
  ): Unit =
    ??? // TODO : Enregistrer le nouvel état (Received, Projected, Completed) et les métadonnées de l'enveloppe Kafka

  def saveHistory(
    tx: Transaction,
    date: LocalDate,
    bucket: Short,
    occurredAt: Instant
  ): Unit =
    ??? // TODO : Insérer l'historique dans clearing_history_by_day

  def saveTransactionByBank(
    tx: Transaction,
    date: LocalDate,
    occurredAt: Instant
  ): Unit =
    ??? // TODO : Insérer le flux entrant et sortant pour chaque banque concernée (double écriture IN / OUT)

  def saveNetPositions(
    transactionId: TransactionId,
    date: LocalDate,
    occurredAt: Instant,
    positions: List[NetPositionProjection]
  ): Unit =
    ??? // TODO : Mettre à jour les positions nettes cumulées dans bank_positions pour chaque banque impliquée

  def savePairActivity(tx: Transaction, date: LocalDate): Unit =
    ??? // TODO : Mettre à jour l'activité cumulée de la paire de banques triée par ordre alphabétique

  def getPositionsByBank(
    bank: BankCode,
    date: LocalDate,
    limit: Int
  ): List[Position] =
    ??? // TODO : Lire les dernières positions pour une banque et une date données

  def getHistoryByDate(
    date: LocalDate,
    buckets: Range
  ): List[HistoryRow] =
    ??? // TODO : Parcourir l'ensemble des 16 buckets pour récupérer l'historique complet d'une journée donnée et le trier par date décroissante

