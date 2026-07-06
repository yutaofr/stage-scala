package distributed.kafka

import clearing.contract.*
import clearing.model.*
import io.circe.*
import io.circe.syntax.*
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
    ??? // TODO : Décoder le payload JSON, valider par rapport au domaine et retourner la décision appropriée (Validated ou Rejected)


object DeduplicationCache:
  private val processed = new ConcurrentHashMap[String, Boolean]()

  def contains(transactionId: String): Boolean =
    ??? // TODO : Vérifier si l'ID a déjà été traité

  def markProcessed(transactionId: String): Unit =
    ??? // TODO : Marquer l'ID comme traité

  def clear(): Unit = processed.clear()


final class ResultPublisher(
  producer: KafkaProducer[String, String],
  settings: KafkaSettings
):
  def publish(decision: ProcessingDecision): java.util.concurrent.Future[RecordMetadata] =
    ??? // TODO : Convertir la décision en ProducerRecord (vers outputTopic ou dlqTopic), ajouter l'en-tête "transaction-id" et envoyer via le producer


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
    ??? // TODO : Poll les records, regrouper le traitement par TopicPartition, traiter séquentiellement, publier le résultat et faire un commitSync manuel et précis par partition


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
    ??? // TODO : Convertir la transaction en TransactionSubmittedV1, sérialiser en JSON et envoyer avec le bon header


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
