package clearing.v30

import java.nio.charset.StandardCharsets
import java.time.Instant
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.record.TimestampType
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class KafkaConsumerLoopSpec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")

  "ConsumerCli" should "fournir un consumer continu par défaut" in:
    ConsumerCli.parse(Nil) shouldBe Right(
      ConsumerCommand(
        bootstrapServers = "localhost:9092",
        groupId = "marc-clearing-v30",
        maxRecords = None
      )
    )

  it should "accepter un run borné pour les preuves" in:
    ConsumerCli.parse(
      List(
        "--bootstrap-servers",
        "kafka:29092",
        "--group-id",
        "preuve",
        "--max-records",
        "51"
      )
    ) shouldBe Right(ConsumerCommand("kafka:29092", "preuve", Some(51)))
    ConsumerCli.parse(List("--max-records", "0")) shouldBe
      Left(ConsumerCli.Usage)

  "KafkaConsumerSettings" should "désactiver l'auto-commit" in:
    val properties = KafkaConsumerSettings.properties(
      "localhost:9092",
      "marc-v30-test"
    )

    properties.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG) shouldBe
      "false"
    properties.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG) shouldBe
      "earliest"
    properties.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG) shouldBe
      classOf[StringDeserializer].getName
    properties.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG) shouldBe
      classOf[StringDeserializer].getName

  "KafkaRecordAdapter" should "conserver position, clé, headers et date" in:
    val header = new RecordHeader(
      "transaction-id",
      "42".getBytes(StandardCharsets.UTF_8)
    )
    val kafkaRecord = new ConsumerRecord[String, String](
      KafkaSettings.InputTopic,
      2,
      17L,
      instant.toEpochMilli,
      TimestampType.CREATE_TIME,
      3,
      7,
      "ATH",
      "payload",
      new org.apache.kafka.common.header.internals.RecordHeaders(
        Array[org.apache.kafka.common.header.Header](header)
      ),
      java.util.Optional.of(Integer.valueOf(1))
    )

    KafkaRecordAdapter.toEnvelope(kafkaRecord) shouldBe RecordEnvelope(
      topic = KafkaSettings.InputTopic,
      partition = 2,
      offset = 17L,
      key = Some("ATH"),
      headers = Map("transaction-id" -> "42"),
      value = "payload",
      occurredAt = instant
    )

  "KafkaDecisionPublisher" should "attendre l'ack de la sortie choisie" in:
    val kafka = new MockProducer[String, String](
      true,
      null,
      new StringSerializer,
      new StringSerializer
    )
    val publisher = KafkaDecisionPublisher(kafka)
    val rejected = ProcessingDecision.Rejected(
      RejectedEvent(
        transactionId = Some(42),
        code = "TEST",
        message = "rejet",
        payloadFingerprint = "a" * 64,
        occurredAt = instant
      )
    )

    publisher.publish(Some("ATH"), rejected) shouldBe Right(())
    val record = kafka.history().get(0)
    record.topic() shouldBe KafkaSettings.DlqTopic
    record.key() shouldBe "ATH"
    EventCodec.encodeDecision(rejected) shouldBe record.value()
    new String(
      record.headers().lastHeader("transaction-id").value(),
      StandardCharsets.UTF_8
    ) shouldBe "42"

  "ConsumerBatchRunner" should "committer seulement après publication et marquage" in:
    val actions = collection.mutable.ListBuffer.empty[String]
    val decision = ProcessingDecision.Rejected(
      RejectedEvent(Some(9), "TEST", "rejet", "b" * 64, instant)
    )
    val coordinator = BatchCoordinator(
      _ => decision,
      DecisionPublisher: (_, _) =>
        actions += "publish"
        Right(()),
      InMemoryDeduplicationRegistry(_ => actions += "mark")
    )
    val runner = ConsumerBatchRunner(
      coordinator,
      OffsetCommitter(offsets =>
        actions += s"commit:${offsets(InputPartition(0))}"
      )
    )
    val envelope = RecordEnvelope(
      KafkaSettings.InputTopic,
      0,
      6L,
      Some("ATH"),
      Map.empty,
      "payload",
      instant
    )

    runner.run(List(envelope)).committableOffsets shouldBe
      Map(InputPartition(0) -> 7L)
    actions.toList shouldBe List("publish", "mark", "commit:7")
