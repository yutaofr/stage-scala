package clearing.v30

import clearing.v22.{HashBoundary, NumberedLine, PreparedTransaction, TypedRailwayEngine, V22Config, V22Error, V22Warning}
import clearing.v22.DomainTypes.*
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

final class V30RecordProcessor(
  config: V22Config,
  hash: HashBoundary
):
  def process(envelope: RecordEnvelope): ProcessingDecision =
    EventCodec.decodeInput(envelope.value) match
      case Left(_) =>
        rejected(
          envelope,
          None,
          "EVENT_JSON_INVALID",
          "événement JSON illisible"
        )
      case Right(event) =>
        TypedRailwayEngine
          .processLine(config, Set.empty, hash)(
            NumberedLine(lineNumber(envelope.offset), canonicalCsv(event))
          )
          .fold(
            error => rejected(envelope, error),
            transaction => validated(envelope, transaction)
          )

  private def validated(
    envelope: RecordEnvelope,
    transaction: PreparedTransaction
  ): ProcessingDecision =
    ProcessingDecision.Validated(
      ValidatedEvent(
        transactionId = transaction.id,
        sender = transaction.sender.value,
        receiver = transaction.receiver.value,
        settlementAmount = decimal(transaction.settlementAmount.value),
        fee = decimal(transaction.fee.value),
        currency = transaction.referenceCurrency.toString,
        transactionType = transaction.transactionType.code,
        status = transaction.status.toString,
        sourceIbanHash = transaction.sourceIbanHash.value,
        destinationIbanHash = transaction.destinationIbanHash.value,
        label = transaction.label,
        warnings = transaction.warnings.map:
          case V22Warning.MissingLabel(defaultLabel) =>
            s"MISSING_LABEL:$defaultLabel",
        occurredAt = envelope.occurredAt
      )
    )

  private def rejected(
    envelope: RecordEnvelope,
    error: V22Error
  ): ProcessingDecision =
    rejected(
      envelope,
      error.transactionId,
      error.code,
      error.message
    )

  private def rejected(
    envelope: RecordEnvelope,
    transactionId: Option[Int],
    code: String,
    message: String
  ): ProcessingDecision =
    ProcessingDecision.Rejected(
      RejectedEvent(
        transactionId = transactionId,
        code = code,
        message = message,
        payloadFingerprint = PayloadFingerprint.sha256(envelope.value),
        occurredAt = envelope.occurredAt
      )
    )

  private def canonicalCsv(event: InputTransactionEvent): String =
    List(
      event.id.toString,
      event.sender,
      event.receiver,
      event.sourceIban,
      event.destinationIban,
      event.amount.bigDecimal.toPlainString,
      event.transactionType,
      event.currency
    ).mkString(",")

  private def lineNumber(offset: Long): Int =
    Math.min(offset + 1L, Int.MaxValue.toLong).toInt

  private def decimal(value: BigDecimal): String =
    value.setScale(2).bigDecimal.toPlainString

private object PayloadFingerprint:
  def sha256(raw: String): String =
    val digest = MessageDigest.getInstance("SHA-256")
    digest
      .digest(raw.getBytes(StandardCharsets.UTF_8))
      .map(byte => f"${byte & 0xff}%02x")
      .mkString
