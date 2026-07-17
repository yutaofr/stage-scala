package clearing.v30

import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class EventCodecSpec extends AnyFlatSpec with Matchers:
  private val event = InputTransactionEvent(
    id = 42,
    sender = "ATH",
    receiver = "BOA",
    sourceIban = "MA64ATH00000000000000000",
    destinationIban = "MA64BOA00000000000000000",
    amount = BigDecimal("125.50"),
    transactionType = "VIR",
    currency = "MAD"
  )

  "EventCodec" should "préserver les huit champs dans un round-trip JSON" in:
    EventCodec.decodeInput(EventCodec.encodeInput(event)) shouldBe Right(event)

  it should "refuser un JSON illisible avec une erreur stable" in:
    EventCodec.decodeInput("{not-json") shouldBe
      Left(EventDecodingError.InvalidJson)

  it should "rendre une sortie validée sans IBAN brut" in:
    val validated = ValidatedEvent(
      transactionId = 42,
      sender = "ATH",
      receiver = "BOA",
      settlementAmount = "125.50",
      fee = "0.13",
      currency = "MAD",
      transactionType = "VIR",
      status = "Pending",
      sourceIbanHash = "a" * 64,
      destinationIbanHash = "b" * 64,
      label = "Facture",
      warnings = Nil,
      occurredAt = Instant.parse("2026-07-14T12:00:00Z")
    )
    val rendered = EventCodec.encodeDecision(
      ProcessingDecision.Validated(validated)
    )

    rendered should include("\"transactionId\":42")
    rendered should include("a" * 64)
    rendered should not include event.sourceIban
    rendered should not include event.destinationIban
