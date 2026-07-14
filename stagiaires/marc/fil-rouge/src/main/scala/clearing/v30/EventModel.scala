package clearing.v30

import java.time.Instant

final case class InputTransactionEvent(
  id: Int,
  sender: String,
  receiver: String,
  sourceIban: String,
  destinationIban: String,
  amount: BigDecimal,
  transactionType: String,
  currency: String
)

final case class RecordEnvelope(
  topic: String,
  partition: Int,
  offset: Long,
  key: Option[String],
  headers: Map[String, String],
  value: String,
  occurredAt: Instant
)

final case class ValidatedEvent(
  transactionId: Int,
  sender: String,
  receiver: String,
  settlementAmount: String,
  fee: String,
  currency: String,
  transactionType: String,
  status: String,
  sourceIbanHash: String,
  destinationIbanHash: String,
  label: String,
  warnings: List[String],
  occurredAt: Instant
)

final case class RejectedEvent(
  transactionId: Option[Int],
  code: String,
  message: String,
  payloadFingerprint: String,
  occurredAt: Instant
)

enum ProcessingDecision:
  case Validated(event: ValidatedEvent)
  case Rejected(event: RejectedEvent)

  def transactionId: Option[Int] = this match
    case Validated(event) => Some(event.transactionId)
    case Rejected(event)  => event.transactionId

  def outputTopic: String = this match
    case Validated(_) => KafkaSettings.OutputTopic
    case Rejected(_)  => KafkaSettings.DlqTopic

enum EventDecodingError:
  case InvalidJson
