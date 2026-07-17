package clearing.v30

import clearing.v13.SecurityUtils
import clearing.v22.{HashBoundary, HashFailure, V22Profiles}
import clearing.v22.DomainTypes.*
import java.nio.charset.StandardCharsets
import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicBoolean
import org.apache.kafka.clients.consumer.{Consumer, ConsumerRecord, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import scala.jdk.CollectionConverters.*
import scala.util.Try
import scala.util.control.NonFatal

object KafkaRecordAdapter:
  def toEnvelope(record: ConsumerRecord[String, String]): RecordEnvelope =
    val occurredAt =
      if record.timestamp() >= 0 then Instant.ofEpochMilli(record.timestamp())
      else Instant.EPOCH
    val headers = record.headers().iterator().asScala.map: header =>
      val value = Option(header.value()).fold("")(
        bytes => new String(bytes, StandardCharsets.UTF_8)
      )
      header.key() -> value

    RecordEnvelope(
      topic = record.topic(),
      partition = record.partition(),
      offset = record.offset(),
      key = Option(record.key()),
      headers = headers.toMap,
      value = record.value(),
      occurredAt = occurredAt
    )

final class KafkaDecisionPublisher(
  producer: Producer[String, String],
  headerInjector: KafkaRecordHeaderInjector = KafkaRecordHeaderInjector.noop
) extends DecisionPublisher:
  def publish(
    _key: Option[String],
    decision: ProcessingDecision
  ): Either[PublishingFailure, Unit] =
    val outputKey = decision match
      case ProcessingDecision.Validated(event) => event.sender
      case ProcessingDecision.Rejected(event) =>
        event.transactionId
          .map(_.toString)
          .getOrElse(s"dlq-${event.payloadFingerprint.take(16)}")
    val record = new ProducerRecord[String, String](
      decision.outputTopic,
      outputKey,
      EventCodec.encodeDecision(decision)
    )
    decision.transactionId.foreach: id =>
      record
        .headers()
        .add(
          "transaction-id",
          id.toString.getBytes(StandardCharsets.UTF_8)
        )
    headerInjector.inject(record)

    try
      producer.send(record).get()
      Right(())
    catch
      case NonFatal(_) => Left(PublishingFailure("publication Kafka impossible"))

trait KafkaRecordHeaderInjector:
  def inject(record: ProducerRecord[String, String]): Unit

object KafkaRecordHeaderInjector:
  val noop: KafkaRecordHeaderInjector = new KafkaRecordHeaderInjector:
    def inject(record: ProducerRecord[String, String]): Unit = ()

final class KafkaOffsetCommitter(
  consumer: Consumer[String, String]
) extends OffsetCommitter:
  def commit(offsets: Map[InputPartition, Long]): Unit =
    val kafkaOffsets = offsets.map: (partition, nextOffset) =>
      new TopicPartition(KafkaSettings.InputTopic, partition.value) ->
        new OffsetAndMetadata(nextOffset)
    consumer.commitSync(kafkaOffsets.asJava)

final class KafkaPartitionRewinder(
  consumer: Consumer[String, String]
) extends PartitionRewinder:
  def rewind(offsets: Map[InputPartition, Long]): Unit =
    offsets.foreach: (partition, offset) =>
      consumer.seek(
        new TopicPartition(KafkaSettings.InputTopic, partition.value),
        offset
      )

final class ConsumerBatchRunner(
  coordinator: BatchCoordinator,
  committer: OffsetCommitter,
  rewinder: PartitionRewinder
):
  def run(records: List[RecordEnvelope]): BatchReport =
    val report = coordinator.process(records)
    if report.committableOffsets.nonEmpty then
      committer.commit(report.committableOffsets)
    if report.retryOffsets.nonEmpty then rewinder.rewind(report.retryOffsets)
    report

final class KafkaConsumerLoop(
  consumer: Consumer[String, String],
  runner: ConsumerBatchRunner,
  outputProducer: Option[Producer[String, String]] = None,
  pollTimeout: Duration = Duration.ofSeconds(1)
) extends AutoCloseable:
  def pollOnce(): BatchReport =
    val records = consumer
      .poll(pollTimeout)
      .iterator()
      .asScala
      .map(KafkaRecordAdapter.toEnvelope)
      .toList
    runner.run(records)

  def runUntil(stopRequested: () => Boolean): Unit =
    while !stopRequested() do pollOnce()

  def wakeup(): Unit = consumer.wakeup()

  override def close(): Unit =
    try consumer.close()
    finally outputProducer.foreach(_.close())

object KafkaConsumerLoop:
  def live(
    bootstrapServers: String,
    groupId: String,
    registry: DeduplicationRegistry = InMemoryDeduplicationRegistry(),
    maxPollRecords: Option[Int] = None
  ): KafkaConsumerLoop =
    val consumer = new KafkaConsumer[String, String](
      KafkaConsumerSettings.properties(
        bootstrapServers,
        groupId,
        maxPollRecords
      )
    )
    consumer.subscribe(List(KafkaSettings.InputTopic).asJava)
    val producerProperties = KafkaProducerSettings.properties(bootstrapServers)
    producerProperties.setProperty("client.id", "marc-v30-output-publisher")
    val outputProducer = new KafkaProducer[String, String](producerProperties)
    val processor = V30RecordProcessor(
      V22Profiles.clearingMAD,
      V30HashBoundary.sha256
    )
    val coordinator = BatchCoordinator(
      processor.process,
      KafkaDecisionPublisher(outputProducer),
      registry
    )
    val runner = ConsumerBatchRunner(
      coordinator,
      KafkaOffsetCommitter(consumer),
      KafkaPartitionRewinder(consumer)
    )
    KafkaConsumerLoop(consumer, runner, Some(outputProducer))

private object V30HashBoundary:
  val sha256: HashBoundary = iban =>
    SecurityUtils
      .hashIbanTry(iban.value)
      .toEither
      .left
      .map(_ => HashFailure.Unavailable)
      .flatMap: rawHash =>
        IbanHash.from(rawHash).left.map(_ => HashFailure.InvalidDigest)

object ConsumerApp:
  def run(
    bootstrapServers: String,
    groupId: String
  ): Unit =
    val stopped = new AtomicBoolean(false)
    val loop = KafkaConsumerLoop.live(bootstrapServers, groupId)
    val hook = new Thread(() =>
      stopped.set(true)
      loop.wakeup()
    )
    Runtime.getRuntime.addShutdownHook(hook)
    try loop.runUntil(() => stopped.get())
    catch
      case _: org.apache.kafka.common.errors.WakeupException if stopped.get() =>
        ()
    finally
      loop.close()
      if !stopped.get() then Runtime.getRuntime.removeShutdownHook(hook)

  def runBounded(
    loop: KafkaConsumerLoop,
    maxRecords: Int,
    maxEmptyPolls: Int = 10
  ): BatchReport =
    require(maxRecords > 0, "maxRecords doit être strictement positif")

    @annotation.tailrec
    def poll(
      current: BatchReport,
      handled: Int,
      emptyPolls: Int
    ): BatchReport =
      if handled >= maxRecords then current
      else if emptyPolls >= maxEmptyPolls then
        throw new java.util.concurrent.TimeoutException(
          s"seulement $handled records reçus sur $maxRecords attendus"
        )
      else
        val next = loop.pollOnce()
        val nextHandled = next.published + next.duplicates
        val combined = BatchReport(
          committableOffsets = current.committableOffsets ++
            next.committableOffsets,
          published = current.published + next.published,
          duplicates = current.duplicates + next.duplicates,
          failedPartitions = current.failedPartitions ++ next.failedPartitions,
          retryOffsets = current.retryOffsets ++ next.retryOffsets
        )
        poll(
          combined,
          handled + nextHandled,
          if nextHandled == 0 then emptyPolls + 1 else 0
        )

    poll(BatchReport(Map.empty, 0, 0, Set.empty), 0, 0)

final case class ConsumerCommand(
  bootstrapServers: String,
  groupId: String,
  maxRecords: Option[Int]
)

object ConsumerCli:
  val Usage =
    "Usage : consumer [--bootstrap-servers HOST:PORT] [--group-id ID] " +
      "[--max-records N]"

  private val default = ConsumerCommand(
    "localhost:9092",
    "marc-clearing-v30",
    None
  )

  def parse(args: List[String]): Either[String, ConsumerCommand] =
    def loop(
      remaining: List[String],
      command: ConsumerCommand
    ): Either[String, ConsumerCommand] = remaining match
      case Nil => Right(command)
      case "--bootstrap-servers" :: value :: tail if value.nonEmpty =>
        loop(tail, command.copy(bootstrapServers = value))
      case "--group-id" :: value :: tail if value.nonEmpty =>
        loop(tail, command.copy(groupId = value))
      case "--max-records" :: raw :: tail =>
        positiveInt(raw).flatMap(value =>
          loop(tail, command.copy(maxRecords = Some(value)))
        )
      case _ => Left(Usage)

    loop(args, default)

  private def positiveInt(raw: String): Either[String, Int] =
    Try(raw.toInt).toEither.left
      .map(_ => Usage)
      .flatMap(value => Either.cond(value > 0, value, Usage))

@main def runKafkaConsumerV30(args: String*): Unit =
  ConsumerCli.parse(args.toList) match
    case Left(error) => V30Reporter.print(error)
    case Right(command) =>
      command.maxRecords match
        case None =>
          ConsumerApp.run(command.bootstrapServers, command.groupId)
        case Some(maxRecords) =>
          val loop = KafkaConsumerLoop.live(
            command.bootstrapServers,
            command.groupId,
            maxPollRecords = Some(1)
          )
          try
            val report = ConsumerApp.runBounded(loop, maxRecords)
            V30Reporter.print(V30Renderer.consumer(report))
          finally loop.close()
