package distributed.persistence

import clearing.contract.TransactionSubmittedV1
import clearing.core.*
import clearing.model.*
import distributed.kafka.RecordEnvelope
import zio.*

import java.time.ZoneOffset

enum ProcessingStage:
  case Received, Projected, Completed

final case class NetPositionProjection(
  bank: BankCode,
  amount: Money,
  transactionCount: Int
)

trait ProcessingRepository:
  def stage(id: TransactionId): Task[Option[ProcessingStage]]
  def markStage(
    id: TransactionId,
    stage: ProcessingStage,
    record: RecordEnvelope
  ): Task[Unit]

enum DurableResult:
  case AlreadyCompleted(id: TransactionId)
  case Completed(id: TransactionId, positions: List[NetPositionProjection])

final class DurableProcessor(knownBanks: Set[BankCode]):
  def process(
    record: RecordEnvelope,
    event: TransactionSubmittedV1
  ): ZIO[ClearingRepository, ClearingError | Throwable, DurableResult] =
    for
      tx <- ZIO.fromEither(event.toDomain(knownBanks))
      repository <- ZIO.service[ClearingRepository]
      current <- repository.stage(tx.id)
      result <- current match
        case Some(ProcessingStage.Completed) =>
          ZIO.succeed(DurableResult.AlreadyCompleted(tx.id))
        case _ =>
          val date = record.occurredAt.atZone(ZoneOffset.UTC).toLocalDate
          val bucket = ClearingRepository.historyBucket(tx.id)
          val positions = PureNettingCalculator
            .calculateNetPositions(List(tx))
            .map((bank, amount) => NetPositionProjection(bank, amount, 1))
            .toList

          for
            _ <- ZIO.when(current.isEmpty)(
              repository.markStage(tx.id, ProcessingStage.Received, record)
            )
            // Toutes les clés incluent l'ID et le timestamp Kafka stables.
            // Une reprise peut rejouer ces upserts sans créer de nouvelle opération.
            _ <- repository.saveHistory(tx, date, bucket, record.occurredAt)
            _ <- repository.saveTransactionByBank(tx, date, record.occurredAt)
            _ <- repository.saveNetPositions(
              tx.id,
              date,
              record.occurredAt,
              positions
            )
            _ <- repository.savePairActivity(tx, date)
            _ <- repository.markStage(tx.id, ProcessingStage.Projected, record)
            _ <- repository.markStage(tx.id, ProcessingStage.Completed, record)
          yield DurableResult.Completed(tx.id, positions)
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
