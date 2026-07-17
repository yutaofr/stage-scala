package clearing.v30

import clearing.v22.{HashBoundary, HashFailure, V22Profiles}
import clearing.v22.DomainTypes.*
import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class V30RecordProcessorSpec extends AnyFlatSpec with Matchers:
  private val occurredAt = Instant.parse("2026-07-14T12:00:00Z")
  private val sourceIban = "MA64ATH00000000000000000"
  private val destinationIban = "MA64BOA00000000000000000"
  private val safeHash = IbanHash.from("a" * 64).toOption.get
  private val workingHash: HashBoundary = _ => Right(safeHash)

  private def envelope(value: String): RecordEnvelope = RecordEnvelope(
    topic = "clearing-input",
    partition = 1,
    offset = 9L,
    key = Some("ATH"),
    headers = Map("transaction-id" -> "1"),
    value = value,
    occurredAt = occurredAt
  )

  private def json(
    amount: String = "100.00",
    receiver: String = "BOA"
  ): String = EventCodec.encodeInput(
    InputTransactionEvent(
      id = 1,
      sender = "ATH",
      receiver = receiver,
      sourceIban = sourceIban,
      destinationIban = destinationIban,
      amount = BigDecimal(amount),
      transactionType = "VIR",
      currency = "MAD"
    )
  )

  "V30RecordProcessor" should "réutiliser le railway v2.3 pour une transaction valide" in:
    val decision = V30RecordProcessor(
      V22Profiles.clearingMAD,
      workingHash
    ).process(envelope(json()))

    decision match
      case ProcessingDecision.Validated(event) =>
        event.transactionId shouldBe 1
        event.sender shouldBe "ATH"
        event.receiver shouldBe "BOA"
        event.settlementAmount shouldBe "100.00"
        event.sourceIbanHash shouldBe safeHash.value
        event.destinationIbanHash shouldBe safeHash.value
        event.occurredAt shouldBe occurredAt
      case other => fail(s"décision inattendue : $other")

  it should "router un rejet métier vers une décision privée" in:
    val decision = V30RecordProcessor(
      V22Profiles.clearingMAD,
      workingHash
    ).process(envelope(json(receiver = "ATH")))

    decision match
      case ProcessingDecision.Rejected(event) =>
        event.transactionId shouldBe Some(1)
        event.code shouldBe "VALIDATION_TRANSACTION"
        event.payloadFingerprint should fullyMatch regex "[0-9a-f]{64}"
        EventCodec.encodeDecision(decision) should not include sourceIban
        EventCodec.encodeDecision(decision) should not include destinationIban
      case other => fail(s"décision inattendue : $other")

  it should "isoler un JSON illisible sans recopier le payload" in:
    val raw = s"{not-json-$sourceIban"
    val decision = V30RecordProcessor(
      V22Profiles.clearingMAD,
      workingHash
    ).process(envelope(raw))

    decision match
      case ProcessingDecision.Rejected(event) =>
        event.transactionId shouldBe None
        event.code shouldBe "EVENT_JSON_INVALID"
        event.payloadFingerprint should fullyMatch regex "[0-9a-f]{64}"
        EventCodec.encodeDecision(decision) should not include raw
        EventCodec.encodeDecision(decision) should not include sourceIban
      case other => fail(s"décision inattendue : $other")

  it should "conserver une panne de hash comme rejet technique" in:
    val failingHash: HashBoundary = _ => Left(HashFailure.Unavailable)
    val decision = V30RecordProcessor(
      V22Profiles.clearingMAD,
      failingHash
    ).process(envelope(json()))

    decision match
      case ProcessingDecision.Rejected(event) =>
        event.transactionId shouldBe Some(1)
        event.code shouldBe "TECH_HASH_IBAN"
      case other => fail(s"décision inattendue : $other")
