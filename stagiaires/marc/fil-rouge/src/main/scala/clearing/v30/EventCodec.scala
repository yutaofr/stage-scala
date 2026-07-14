package clearing.v30

import io.circe.{Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.parser.decode
import io.circe.syntax.*

object EventCodec:
  private given Encoder[InputTransactionEvent] = deriveEncoder
  private given io.circe.Decoder[InputTransactionEvent] = deriveDecoder

  def encodeInput(event: InputTransactionEvent): String = event.asJson.noSpaces

  def decodeInput(
    raw: String
  ): Either[EventDecodingError, InputTransactionEvent] =
    decode[InputTransactionEvent](raw).left.map(_ => EventDecodingError.InvalidJson)

  def encodeDecision(decision: ProcessingDecision): String =
    val json = decision match
      case ProcessingDecision.Validated(event) =>
        Json.obj(
          "kind" -> Json.fromString("validated"),
          "transactionId" -> Json.fromInt(event.transactionId),
          "sender" -> Json.fromString(event.sender),
          "receiver" -> Json.fromString(event.receiver),
          "settlementAmount" -> Json.fromString(event.settlementAmount),
          "fee" -> Json.fromString(event.fee),
          "currency" -> Json.fromString(event.currency),
          "transactionType" -> Json.fromString(event.transactionType),
          "status" -> Json.fromString(event.status),
          "sourceIbanHash" -> Json.fromString(event.sourceIbanHash),
          "destinationIbanHash" -> Json.fromString(
            event.destinationIbanHash
          ),
          "label" -> Json.fromString(event.label),
          "warnings" -> Json.arr(event.warnings.map(Json.fromString)*),
          "occurredAt" -> Json.fromString(event.occurredAt.toString)
        )
      case ProcessingDecision.Rejected(event) =>
        Json.obj(
          "kind" -> Json.fromString("rejected"),
          "transactionId" -> event.transactionId.fold(Json.Null)(
            Json.fromInt
          ),
          "code" -> Json.fromString(event.code),
          "message" -> Json.fromString(event.message),
          "payloadFingerprint" -> Json.fromString(event.payloadFingerprint),
          "occurredAt" -> Json.fromString(event.occurredAt.toString)
        )

    json.noSpaces
