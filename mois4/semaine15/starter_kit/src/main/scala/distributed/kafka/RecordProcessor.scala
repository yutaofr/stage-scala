package distributed.kafka

import clearing.contract.*
import clearing.model.*
import java.time.Instant

final case class RecordEnvelope(
  topic: String,
  partition: Int,
  offset: Long,
  occurredAt: Instant,
  key: Option[String],
  payload: String
)

enum ProcessingDecision:
  case Validated(key: String, event: TransactionValidatedV1)
  case Rejected(key: Option[String], event: TransactionRejectedV1)

final class RecordProcessor(knownBanks: Set[BankCode]):
  
  /**
   * Analyse le record brut, désérialise le JSON et applique la validation du domaine.
   * Cet élément est entièrement fourni pour permettre au débutant de se concentrer sur
   * les concepts d'infrastructure Kafka et la gestion de la boucle de consommation.
   */
  def process(record: RecordEnvelope): ProcessingDecision =
    ContractCodec.decode(record.payload) match
      case Left(error) =>
        ProcessingDecision.Rejected(
          key = record.key,
          event = TransactionRejectedV1(
            id = None,
            errorCode = "INVALID_JSON",
            message = error,
            originalPayload = record.payload
          )
        )
      case Right(submitted) =>
        submitted.toDomain(knownBanks) match
          case Left(error) =>
            ProcessingDecision.Rejected(
              key = Some(submitted.sender),
              event = TransactionRejectedV1(
                id = Some(submitted.id),
                errorCode = error.code,
                message = error.message,
                originalPayload = record.payload
              )
            )
          case Right(tx) =>
            ProcessingDecision.Validated(
              key = tx.sender.value,
              event = TransactionValidatedV1.fromDomain(tx)
            )
