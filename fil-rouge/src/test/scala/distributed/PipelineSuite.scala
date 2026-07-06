package distributed

import clearing.contract.TransactionSubmittedV1
import clearing.model.BankCode
import distributed.kafka.*
import distributed.persistence.*
import munit.FunSuite

import java.time.Instant

final class PipelineSuite extends FunSuite:
  private val knownBanks = Set("AWB", "CIH").map(BankCode.unsafe)
  private val record = RecordEnvelope(
    topic = "clearing-input",
    partition = 0,
    offset = 42,
    occurredAt = Instant.parse("2026-06-20T10:15:30Z"),
    key = Some("AWB"),
    payload = """{"id":"tx-42","sender":"AWB","receiver":"CIH","amount":100,"status":"Pending","transactionType":"Transfer"}"""
  )

  test("RecordProcessor conserve l'ID et produit une sortie validée"):
    val decision = new RecordProcessor(knownBanks).process(record)
    decision match
      case ProcessingDecision.Validated(key, event) =>
        assertEquals(key, "AWB")
        assertEquals(event.id, "tx-42")
      case other => fail(s"Sortie inattendue: $other")

  test("RecordProcessor envoie un JSON invalide vers la DLQ"):
    val invalid = record.copy(payload = "{")
    val decision = new RecordProcessor(knownBanks).process(invalid)
    decision match
      case ProcessingDecision.Rejected(_, event) =>
        assertEquals(event.errorCode, "INVALID_JSON")
        assertEquals(event.originalPayload, "{")
      case other => fail(s"Sortie inattendue: $other")

  test("DurableProcessor reprend puis reconnaît Completed"):
    val event = TransactionSubmittedV1("tx-42", "AWB", "CIH", BigDecimal(100))
    val repository = ClearingRepository.inMemory()
    val processor = new DurableProcessor(knownBanks, repository)

    val first = processor.process(record, event)
    val second = processor.process(record, event)

    assert(first.exists(_.isInstanceOf[DurableResult.Completed]), s"premier résultat: $first")
    assert(second.exists(_.isInstanceOf[DurableResult.AlreadyCompleted]), s"second résultat: $second")

  test("configuration Kafka lit le listener interne"):
    val settings = KafkaSettings.fromEnvironment(
      Map("KAFKA_BOOTSTRAP_SERVERS" -> "kafka:29092")
    )
    assertEquals(settings.bootstrapServers, "kafka:29092")
