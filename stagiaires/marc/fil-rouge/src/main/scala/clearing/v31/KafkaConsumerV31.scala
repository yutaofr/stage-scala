package clearing.v31

import clearing.v13.SecurityUtils
import clearing.v22.{HashBoundary, HashFailure, V22Profiles}
import clearing.v22.DomainTypes.*
import clearing.v30.*
import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicBoolean
import org.apache.kafka.clients.consumer.{Consumer, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, Producer}
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

final class V31ConsumerBatchRunner(
  coordinator: DurableBatchCoordinator,
  committer: OffsetCommitter,
  rewinder: PartitionRewinder
):
  def run(records: List[RecordEnvelope]): BatchReport =
    val report = coordinator.process(records)
    if report.committableOffsets.nonEmpty then
      committer.commit(report.committableOffsets)
    if report.retryOffsets.nonEmpty then rewinder.rewind(report.retryOffsets)
    report

trait ConsumerPollObserver:
  def afterPoll(consumer: Consumer[String, String]): Unit

object ConsumerPollObserver:
  val noop: ConsumerPollObserver = new ConsumerPollObserver:
    def afterPoll(consumer: Consumer[String, String]): Unit = ()

final class KafkaConsumerLoopV31(
  consumer: Consumer[String, String],
  runner: V31ConsumerBatchRunner,
  resources: List[AutoCloseable] = Nil,
  pollTimeout: Duration = Duration.ofSeconds(1),
  pollObserver: ConsumerPollObserver = ConsumerPollObserver.noop
) extends AutoCloseable:
  def pollOnce(): BatchReport =
    val records = consumer
      .poll(pollTimeout)
      .iterator()
      .asScala
      .map(KafkaRecordAdapter.toEnvelope)
      .toList
    pollObserver.afterPoll(consumer)
    runner.run(records)

  def runUntil(stopRequested: () => Boolean): Unit =
    while !stopRequested() do pollOnce()

  def wakeup(): Unit = consumer.wakeup()

  override def close(): Unit =
    ResourceClosing.closeAll(consumer :: resources)

object KafkaConsumerLoopV31:
  def live(
    bootstrapServers: String,
    groupId: String,
    cassandraSettings: CassandraSettings = CassandraSettings.fromEnvironment()
  ): KafkaConsumerLoopV31 =
    val session = CassandraSession.open(cassandraSettings)
    ResourceClosing.protect(session):
      val consumer = new KafkaConsumer[String, String](
        KafkaConsumerSettings.properties(
          bootstrapServers,
          groupId,
          maxPollRecords = Some(1)
        )
      )
      ResourceClosing.protect(consumer):
        consumer.subscribe(List(KafkaSettings.InputTopic).asJava)
        val producerProperties = KafkaProducerSettings.properties(bootstrapServers)
        producerProperties.setProperty("client.id", "marc-v31-output-publisher")
        val producer = new KafkaProducer[String, String](producerProperties)
        ResourceClosing.protect(producer):
          val repository = LiveCassandraRepository(
            session,
            CassandraStatements.prepare(session)
          )
          val processor = DurableRecordProcessor(
            V30RecordProcessor(V22Profiles.clearingMAD, V31HashBoundary.sha256)
              .process,
            repository,
            KafkaDecisionPublisher(producer)
          )
          val runner = V31ConsumerBatchRunner(
            DurableBatchCoordinator(processor),
            KafkaOffsetCommitter(consumer),
            KafkaPartitionRewinder(consumer)
          )
          KafkaConsumerLoopV31(
            consumer,
            runner,
            List(producer, session)
          )

private object V31HashBoundary:
  val sha256: HashBoundary = iban =>
    SecurityUtils
      .hashIbanTry(iban.value)
      .toEither
      .left
      .map(_ => HashFailure.Unavailable)
      .flatMap: rawHash =>
        IbanHash.from(rawHash).left.map(_ => HashFailure.InvalidDigest)

private object ResourceClosing:
  def closeAll(resources: List[AutoCloseable]): Unit =
    var firstFailure: Option[Throwable] = None
    resources.foreach: resource =>
      try resource.close()
      catch
        case NonFatal(error) =>
          firstFailure match
            case None => firstFailure = Some(error)
            case Some(first) => first.addSuppressed(error)
    firstFailure.foreach(throw _)

  def protect[A <: AutoCloseable, B](resource: A)(body: => B): B =
    try body
    catch
      case NonFatal(error) =>
        try resource.close()
        catch case NonFatal(closeError) => error.addSuppressed(closeError)
        throw error

object V31ConsumerApp:
  def run(
    bootstrapServers: String,
    groupId: String,
    cassandraSettings: CassandraSettings = CassandraSettings.fromEnvironment()
  ): Unit =
    val stopped = new AtomicBoolean(false)
    val loop = KafkaConsumerLoopV31.live(
      bootstrapServers,
      groupId,
      cassandraSettings
    )
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
    loop: KafkaConsumerLoopV31,
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
        val combined = V31BatchReports.combine(current, next)
        poll(
          combined,
          handled + nextHandled,
          if nextHandled == 0 then emptyPolls + 1 else 0
        )

    poll(BatchReport(Map.empty, 0, 0, Set.empty), 0, 0)

object V31BatchReports:
  def combine(current: BatchReport, next: BatchReport): BatchReport =
    val recovered = next.committableOffsets.keySet -- next.failedPartitions
    BatchReport(
      current.committableOffsets ++ next.committableOffsets,
      current.published + next.published,
      current.duplicates + next.duplicates,
      (current.failedPartitions -- recovered) ++ next.failedPartitions,
      (current.retryOffsets -- recovered) ++ next.retryOffsets
    )

final case class V31ConsumerCommand(
  bootstrapServers: String,
  groupId: String,
  maxRecords: Option[Int]
)

object V31ConsumerCli:
  val Usage =
    "Usage : consumer [--bootstrap-servers HOST:PORT] [--group-id ID] " +
      "[--max-records N]"

  private val default = V31ConsumerCommand(
    "localhost:9092",
    "marc-clearing-v31",
    None
  )

  def parse(args: List[String]): Either[String, V31ConsumerCommand] =
    def loop(
      remaining: List[String],
      command: V31ConsumerCommand
    ): Either[String, V31ConsumerCommand] = remaining match
      case Nil => Right(command)
      case "--bootstrap-servers" :: value :: tail if value.nonEmpty =>
        loop(tail, command.copy(bootstrapServers = value))
      case "--group-id" :: value :: tail if value.nonEmpty =>
        loop(tail, command.copy(groupId = value))
      case "--max-records" :: raw :: tail =>
        raw.toIntOption
          .filter(_ > 0)
          .toRight(Usage)
          .flatMap(value => loop(tail, command.copy(maxRecords = Some(value))))
      case _ => Left(Usage)

    loop(args, default)
