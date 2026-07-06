package distributed.persistence

import clearing.contract.TransactionSubmittedV1
import clearing.core.*
import clearing.model.*
import distributed.kafka.RecordEnvelope

import java.time.ZoneOffset

enum ProcessingStage:
  case Received, Projected, Completed

final case class NetPositionProjection(
  bank: BankCode,
  amount: Money,
  transactionCount: Int
)

trait ProcessingRepository:
  def stage(id: TransactionId): Option[ProcessingStage]
  def markStage(
    id: TransactionId,
    stage: ProcessingStage,
    record: RecordEnvelope
  ): Unit

enum DurableResult:
  case AlreadyCompleted(id: TransactionId)
  case Completed(id: TransactionId, positions: List[NetPositionProjection])

final class DurableProcessor(
  knownBanks: Set[BankCode],
  repository: ClearingRepository
):
  def process(
    record: RecordEnvelope,
    event: TransactionSubmittedV1
  ): Either[ClearingError, DurableResult] =
    for
      tx <- event.toDomain(knownBanks)
      result <- repository.stage(tx.id) match
        case Some(ProcessingStage.Completed) =>
          Right(DurableResult.AlreadyCompleted(tx.id))
        case current =>
          val date = record.occurredAt.atZone(ZoneOffset.UTC).toLocalDate
          val bucket = ClearingRepository.historyBucket(tx.id)
          val positions = PureNettingCalculator
            .calculateNetPositions(List(tx))
            .map((bank, amount) => NetPositionProjection(bank, amount, 1))
            .toList

          if current.isEmpty then
            repository.markStage(tx.id, ProcessingStage.Received, record)
          // Toutes les clés incluent l'ID et le timestamp Kafka stables.
          // Une reprise peut rejouer ces upserts sans créer de nouvelle opération.
          repository.saveHistory(tx, date, bucket, record.occurredAt)
          repository.saveTransactionByBank(tx, date, record.occurredAt)
          repository.saveNetPositions(tx.id, date, record.occurredAt, positions)
          repository.savePairActivity(tx, date)
          repository.markStage(tx.id, ProcessingStage.Projected, record)
          repository.markStage(tx.id, ProcessingStage.Completed, record)
          Right(DurableResult.Completed(tx.id, positions))
    yield result

final case class CassandraSettings(
  host: String,
  port: Int,
  datacenter: String,
  keyspace: String
)

object CassandraSettings:
  def fromEnvironment(env: Map[String, String] = sys.env): CassandraSettings =
    CassandraSettings(
      host = env.getOrElse("CASSANDRA_HOST", "localhost"),
      port = env.get("CASSANDRA_PORT").flatMap(_.toIntOption).getOrElse(9042),
      datacenter = env.getOrElse("CASSANDRA_DATACENTER", "datacenter1"),
      keyspace = env.getOrElse("CASSANDRA_KEYSPACE", "clearing")
    )
