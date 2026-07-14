package clearing.v30

import clearing.v22.{HashBoundary, V22Profiles}
import clearing.v22.DomainTypes.*
import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV30Spec extends AnyFlatSpec with Matchers:
  private val occurredAt = Instant.parse("2026-07-14T12:00:00Z")
  private val hash = IbanHash.from("a" * 64).toOption.get
  private val stableHash: HashBoundary = _ => Right(hash)

  "Le gate v3.0" should "classifier 1000 événements et conserver le netting" in:
    val inputs = TransactionGenerator.generateMixed(
      count = 1000,
      seed = 1500L,
      rejectEvery = 10
    )
    val processor = V30RecordProcessor(
      V22Profiles.clearingMAD,
      stableHash
    )
    val decisions = inputs.zipWithIndex.map: (event, index) =>
      processor.process(
        RecordEnvelope(
          topic = KafkaSettings.InputTopic,
          partition = index % 3,
          offset = index.toLong,
          key = Some(event.sender),
          headers = Map("transaction-id" -> event.id.toString),
          value = EventCodec.encodeInput(event),
          occurredAt = occurredAt
        )
      )
    val validated = decisions.collect:
      case ProcessingDecision.Validated(event) => event
    val rejected = decisions.collect:
      case ProcessingDecision.Rejected(event) => event

    decisions should have size 1000
    validated should have size 900
    rejected should have size 100
    validated.size + rejected.size shouldBe inputs.size

    val positions = validated.foldLeft(Map.empty[String, BigDecimal]):
      (current, event) =>
        val amount = BigDecimal(event.settlementAmount)
        val debited = current.updatedWith(event.sender):
          case Some(value) => Some(value - amount)
          case None        => Some(-amount)
        debited.updatedWith(event.receiver):
          case Some(value) => Some(value + amount)
          case None        => Some(amount)

    positions.values.sum shouldBe BigDecimal(0)

  it should "ne publier aucun IBAN brut dans output ou DLQ" in:
    val inputs = TransactionGenerator.generateMixed(100, 42L, 10)
    val processor = V30RecordProcessor(V22Profiles.clearingMAD, stableHash)

    inputs.zipWithIndex.foreach: (event, index) =>
      val decision = processor.process(
        RecordEnvelope(
          KafkaSettings.InputTopic,
          index % 3,
          index.toLong,
          Some(event.sender),
          Map.empty,
          EventCodec.encodeInput(event),
          occurredAt
        )
      )
      val output = EventCodec.encodeDecision(decision)
      output should not include event.sourceIban
      output should not include event.destinationIban
