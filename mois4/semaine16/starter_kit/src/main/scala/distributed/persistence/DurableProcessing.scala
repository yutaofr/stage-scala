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
    ??? // TODO : Charger l'état actuel. Si Completed, retourner AlreadyCompleted. Sinon, enregistrer Received si absent, enregistrer l'historique et les écritures dans Cassandra, marquer Projected puis Completed, puis retourner le résultat.


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
