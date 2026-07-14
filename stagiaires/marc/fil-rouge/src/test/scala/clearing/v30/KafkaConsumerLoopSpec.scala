package clearing.v30

import java.nio.charset.StandardCharsets
import java.time.Instant
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, MockConsumer, OffsetResetStrategy}
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.record.TimestampType
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.common.TopicPartition
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters.*

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

    KafkaConsumerSettings
      .properties("localhost:9092", "bounded", maxPollRecords = Some(1))
      .getProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG) shouldBe "1"

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

    val untrustedKey = "MA64ATH00000000000000000"
    publisher.publish(Some(untrustedKey), rejected) shouldBe Right(())
    val record = kafka.history().get(0)
    record.topic() shouldBe KafkaSettings.DlqTopic
    record.key() shouldBe "42"
    EventCodec.encodeDecision(rejected) shouldBe record.value()
    new String(
      record.headers().lastHeader("transaction-id").value(),
      StandardCharsets.UTF_8
    ) shouldBe "42"
    record.key() should not include untrustedKey
    record.value() should not include untrustedKey
    record.headers().iterator().asScala.foreach: header =>
      new String(header.value(), StandardCharsets.UTF_8) should not include
        untrustedKey

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
      ),
      PartitionRewinder(_ => ())
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

  "KafkaConsumerLoop" should "rewind une partition échouée avant le poll suivant" in:
    val consumer = new MockConsumer[String, String](OffsetResetStrategy.EARLIEST)
    val partition = new TopicPartition(KafkaSettings.InputTopic, 0)
    consumer.assign(List(partition).asJava)
    consumer.updateBeginningOffsets(
      Map(partition -> java.lang.Long.valueOf(0L)).asJava
    )
    List(0L -> "1", 1L -> "2", 2L -> "3").foreach: (offset, value) =>
      consumer.addRecord(
        new ConsumerRecord(KafkaSettings.InputTopic, 0, offset, "ATH", value)
      )
    val attempts = collection.mutable.ListBuffer.empty[Int]
    var failTwoOnce = true
    val coordinator = BatchCoordinator(
      envelope =>
        val id = envelope.value.toInt
        ProcessingDecision.Rejected(
          RejectedEvent(Some(id), "TEST", "rejet", id.toString * 64, instant)
        ),
      DecisionPublisher: (_, value) =>
        val id = value.transactionId.get
        attempts += id
        if id == 2 && failTwoOnce then
          failTwoOnce = false
          Left(PublishingFailure("panne"))
        else Right(()),
      InMemoryDeduplicationRegistry()
    )
    val runner = ConsumerBatchRunner(
      coordinator,
      KafkaOffsetCommitter(consumer),
      KafkaPartitionRewinder(consumer)
    )
    val loop = KafkaConsumerLoop(consumer, runner)

    val first = loop.pollOnce()
    first.retryOffsets shouldBe Map(InputPartition(0) -> 1L)
    consumer.position(partition) shouldBe 1L
    consumer.committed(Set(partition).asJava).get(partition).offset() shouldBe 1L

    List(1L -> "2", 2L -> "3").foreach: (offset, value) =>
      consumer.addRecord(
        new ConsumerRecord(KafkaSettings.InputTopic, 0, offset, "ATH", value)
      )
    loop.pollOnce().retryOffsets shouldBe empty

    attempts.toList shouldBe List(1, 2, 2, 3)
    consumer.committed(Set(partition).asJava).get(partition).offset() shouldBe 3L
    loop.pollOnce().published shouldBe 0
    attempts.toList shouldBe List(1, 2, 2, 3)
