package distributed.pipeline

import clearing.contract.ContractCodec
import clearing.model.BankCode
import distributed.kafka.*
import distributed.persistence.*
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.TopicPartition

import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

object ClearingPipelineApp:
  private val knownBanks = Set("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe)
  private val recordProcessor = new RecordProcessor(knownBanks)

  private def envelope(record: ConsumerRecord[String, String]): RecordEnvelope =
    RecordEnvelope(
      record.topic(),
      record.partition(),
      record.offset(),
      Instant.ofEpochMilli(record.timestamp()),
      Option(record.key()),
      record.value()
    )

  private def processRecord(
    record: ConsumerRecord[String, String],
    publisher: ResultPublisher,
    repository: ClearingRepository,
    durableProcessor: DurableProcessor
  ): Unit =
    ??? // TODO : Récupérer l'enveloppe, traiter via recordProcessor, et selon la décision rejected ou validated, appeler durableProcessor et publier


  private def processPartition(
    partition: TopicPartition,
    records: List[ConsumerRecord[String, String]],
    publisher: ResultPublisher,
    repository: ClearingRepository,
    durableProcessor: DurableProcessor
  ): Option[(TopicPartition, OffsetAndMetadata)] =
    try
      var lastOffset: Option[Long] = None
      records.foreach { record =>
        processRecord(record, publisher, repository, durableProcessor)
        lastOffset = Some(record.offset())
      }
      lastOffset.map(offset => partition -> new OffsetAndMetadata(offset + 1))
    catch
      case NonFatal(error) =>
        System.err.println(
          s"partition.failed topic=${partition.topic()} partition=${partition.partition()} error=${error.getMessage}"
        )
        None

  def loop(
    consumer: KafkaConsumer[String, String],
    publisher: ResultPublisher,
    repository: ClearingRepository,
    durableProcessor: DurableProcessor
  ): Nothing =
    while true do
      try
        val records = consumer.poll(Duration.ofMillis(500))
        val offsets = records.partitions().asScala.toList.flatMap { partition =>
          processPartition(
            partition,
            records.records(partition).asScala.toList,
            publisher,
            repository,
            durableProcessor
          )
        }
        val committed = offsets.toMap
        if committed.nonEmpty then consumer.commitSync(committed.asJava)
      catch
        case NonFatal(error) =>
          System.err.println(s"poll.failed: ${error.getMessage}")
          Thread.sleep(1000)
    throw new AssertionError("unreachable")

@main def runClearingPipeline(): Unit =
  val kafkaSettings = KafkaSettings.fromEnvironment()
  val cassandraSettings = CassandraSettings.fromEnvironment()
  val session = CassandraSession.connect(cassandraSettings)
  try
    val statements = PreparedStatements(session)
    val repository = new LiveClearingRepository(session, statements)
    val durableProcessor = new DurableProcessor(
      Set("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe),
      repository
    )
    val consumer = KafkaClients.consumer(kafkaSettings)
    val producer = KafkaClients.producer(kafkaSettings)
    try
      consumer.subscribe(java.util.List.of(kafkaSettings.inputTopic))
      ClearingPipelineApp.loop(
        consumer,
        new ResultPublisher(producer, kafkaSettings),
        repository,
        durableProcessor
      )
    finally
      consumer.close()
      producer.close()
  finally session.close()
