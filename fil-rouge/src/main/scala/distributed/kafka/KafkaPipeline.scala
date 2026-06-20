package distributed.kafka

import clearing.contract.*
import clearing.model.*
import org.apache.kafka.clients.consumer.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import java.nio.charset.StandardCharsets
import java.time.{Duration, Instant}
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal
import zio.json.*

final case class KafkaSettings(
  bootstrapServers: String,
  inputTopic: String = "clearing-input",
  outputTopic: String = "clearing-output",
  dlqTopic: String = "clearing-dlq",
  groupId: String = "clearing-engine"
)

object KafkaSettings:
  def fromEnvironment(env: Map[String, String] = sys.env): KafkaSettings =
    KafkaSettings(
      bootstrapServers = env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
      inputTopic = env.getOrElse("KAFKA_INPUT_TOPIC", "clearing-input"),
      outputTopic = env.getOrElse("KAFKA_OUTPUT_TOPIC", "clearing-output"),
      dlqTopic = env.getOrElse("KAFKA_DLQ_TOPIC", "clearing-dlq"),
      groupId = env.getOrElse("KAFKA_GROUP_ID", "clearing-engine")
    )

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
  def process(record: RecordEnvelope): ProcessingDecision =
    ContractCodec.decode(record.payload) match
      case Left(decodingError) =>
        ProcessingDecision.Rejected(
          record.key,
          TransactionRejectedV1(None, "INVALID_JSON", decodingError, record.payload)
        )
      case Right(event) =>
        event.toDomain(knownBanks) match
          case Right(tx) =>
            ProcessingDecision.Validated(tx.sender.value, TransactionValidatedV1.fromDomain(tx))
          case Left(error) =>
            ProcessingDecision.Rejected(
              Some(event.id),
              TransactionRejectedV1(Some(event.id), error.code, error.message, record.payload)
            )

object DeduplicationCache:
  private val processed = new ConcurrentHashMap[String, Boolean]()

  def contains(transactionId: String): Boolean =
    processed.containsKey(transactionId)

  def markProcessed(transactionId: String): Unit =
    processed.put(transactionId, true)

  def clear(): Unit = processed.clear()

final class ResultPublisher(
  producer: KafkaProducer[String, String],
  settings: KafkaSettings
):
  def publish(decision: ProcessingDecision): java.util.concurrent.Future[RecordMetadata] =
    val record = decision match
      case ProcessingDecision.Validated(key, event) =>
        new ProducerRecord[String, String](settings.outputTopic, key, event.toJson)
      case ProcessingDecision.Rejected(key, event) =>
        new ProducerRecord[String, String](settings.dlqTopic, key.orNull, event.toJson)

    val transactionId = decision match
      case ProcessingDecision.Validated(_, event) => Some(event.id)
      case ProcessingDecision.Rejected(_, event)  => event.id

    transactionId.foreach { id =>
      record.headers.add(RecordHeader("transaction-id", id.getBytes(StandardCharsets.UTF_8)))
    }
    producer.send(record)

object KafkaClients:
  def producer(settings: KafkaSettings): KafkaProducer[String, String] =
    val properties = Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.bootstrapServers)
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    properties.put(ProducerConfig.ACKS_CONFIG, "all")
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
    properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "5000")
    properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000")
    properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "5000")
    new KafkaProducer[String, String](properties)

  def consumer(settings: KafkaSettings): KafkaConsumer[String, String] =
    val properties = Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.bootstrapServers)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, settings.groupId)
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    new KafkaConsumer[String, String](properties)

final class KafkaConsumerLoop(
  consumer: KafkaConsumer[String, String],
  publisher: ResultPublisher,
  processor: RecordProcessor,
  settings: KafkaSettings
):
  def pollOnce(timeout: Duration = Duration.ofMillis(500)): Unit =
    val records = consumer.poll(timeout)
    val offsets = scala.collection.mutable.Map.empty[TopicPartition, OffsetAndMetadata]

    records.partitions().asScala.foreach { partition =>
      try
        var lastPublishedOffset: Option[Long] = None
        records.records(partition).asScala.foreach { record =>
          val envelope = RecordEnvelope(
            record.topic(),
            record.partition(),
            record.offset(),
            Instant.ofEpochMilli(record.timestamp()),
            Option(record.key()),
            record.value()
          )
          publisher.publish(processor.process(envelope)).get()
          lastPublishedOffset = Some(record.offset())
        }
        lastPublishedOffset.foreach { offset =>
          offsets.update(partition, OffsetAndMetadata(offset + 1))
        }
      catch
        case NonFatal(error) =>
          System.err.println(
            s"Partition ${partition.topic()}/${partition.partition()} non validée: ${error.getMessage}"
          )
    }

    if offsets.nonEmpty then consumer.commitSync(offsets.toMap.asJava)

  def run(): Unit =
    consumer.subscribe(java.util.List.of(settings.inputTopic))
    try while true do pollOnce()
    finally consumer.close()

object TransactionProducer:
  def send(
    producer: KafkaProducer[String, String],
    settings: KafkaSettings,
    tx: Transaction
  ): java.util.concurrent.Future[RecordMetadata] =
    val event = TransactionSubmittedV1.fromDomain(tx)
    val record = new ProducerRecord[String, String](
      settings.inputTopic,
      tx.sender.value,
      event.toJson
    )
    record.headers.add(
      RecordHeader("transaction-id", tx.id.value.getBytes(StandardCharsets.UTF_8))
    )
    producer.send(record)

@main def runKafkaConsumer(): Unit =
  val settings = KafkaSettings.fromEnvironment()
  val knownBanks = Set("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe)
  val consumer = KafkaClients.consumer(settings)
  val producer = KafkaClients.producer(settings)
  try
    new KafkaConsumerLoop(
      consumer,
      new ResultPublisher(producer, settings),
      new RecordProcessor(knownBanks),
      settings
    ).run()
  finally producer.close()
