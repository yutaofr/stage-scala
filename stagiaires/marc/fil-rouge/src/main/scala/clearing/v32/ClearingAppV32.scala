package clearing.v32

import clearing.v13.SecurityUtils
import clearing.v22.{HashBoundary, HashFailure, V22Profiles}
import clearing.v22.DomainTypes.*
import clearing.v30.*
import clearing.v31.*
import io.micrometer.prometheusmetrics.{PrometheusConfig, PrometheusMeterRegistry}
import java.util.concurrent.atomic.AtomicBoolean
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

final case class V32Settings(
  bootstrapServers: String,
  groupId: String,
  metricsPort: Int,
  otlpEndpoint: String,
  serviceName: String
)

object V32Settings:
  def fromEnvironment(): Either[String, V32Settings] = from(sys.env)

  def from(environment: Map[String, String]): Either[String, V32Settings] =
    val bootstrap = value(
      environment,
      "KAFKA_BOOTSTRAP_SERVERS",
      "localhost:9092"
    )
    val group = value(environment, "KAFKA_GROUP_ID", "marc-clearing-v32")
    val endpoint = value(
      environment,
      "OTEL_EXPORTER_OTLP_ENDPOINT",
      "http://localhost:4317"
    )
    val service = value(environment, "OTEL_SERVICE_NAME", "clearing-engine")
    val port = environment
      .getOrElse("METRICS_PORT", "8080")
      .toIntOption
      .filter(port => port > 0 && port <= 65535)
      .toRight("METRICS_PORT doit être compris entre 1 et 65535")

    port.flatMap: metricsPort =>
      if List(bootstrap, group, endpoint, service).exists(_.isEmpty) then
        Left("les paramètres v3.2 ne peuvent pas être vides")
      else Right(V32Settings(bootstrap, group, metricsPort, endpoint, service))

  private def value(
    environment: Map[String, String],
    name: String,
    default: String
  ): String = environment.getOrElse(name, default).trim

trait ConsumerControl extends AutoCloseable:
  def pollOnce(): BatchReport
  def runUntil(stopRequested: () => Boolean): Unit
  def wakeup(): Unit

final class V32ConsumerRuntime(
  control: ConsumerControl,
  resourcesAfterConsumer: List[AutoCloseable]
) extends AutoCloseable:
  private val closed = AtomicBoolean(false)

  def runUntil(stopRequested: () => Boolean): Unit =
    control.runUntil(stopRequested)

  def wakeup(): Unit = control.wakeup()

  def runBounded(
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
        throw java.util.concurrent.TimeoutException(
          s"seulement $handled records reçus sur $maxRecords attendus"
        )
      else
        val next = control.pollOnce()
        val nextHandled = next.published + next.duplicates
        poll(
          V31BatchReports.combine(current, next),
          handled + nextHandled,
          if nextHandled == 0 then emptyPolls + 1 else 0
        )

    poll(BatchReport(Map.empty, 0, 0, Set.empty), 0, 0)

  def close(): Unit =
    if closed.compareAndSet(false, true) then
      RuntimeResources.closeAll(control :: resourcesAfterConsumer)

object V32ConsumerRuntime:
  def live(
    settings: V32Settings,
    cassandraSettings: CassandraSettings = CassandraSettings.fromEnvironment()
  ): V32ConsumerRuntime =
    val acquisitions = ResourceAcquisitions()
    try
      val telemetry = acquisitions.acquire(
        ClearingTelemetry.live(settings.serviceName, settings.otlpEndpoint)
      )
      val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
      val registryResource = acquisitions.acquire(RegistryResource(registry))
      val metrics = ClearingMetrics(registry)
      val metricsServer = acquisitions.acquire(
        MetricsHttpServer.start(registry, settings.metricsPort)
      )
      val session = acquisitions.acquire(CassandraSession.open(cassandraSettings))
      val consumer = acquisitions.acquire(
        KafkaConsumer[String, String](
          KafkaConsumerSettings.properties(
            settings.bootstrapServers,
            settings.groupId,
            maxPollRecords = Some(1)
          )
        )
      )
      consumer.subscribe(List(KafkaSettings.InputTopic).asJava)
      val producerProperties = KafkaProducerSettings.properties(
        settings.bootstrapServers
      )
      producerProperties.setProperty("client.id", "marc-v32-output-publisher")
      val producer = acquisitions.acquire(
        KafkaProducer[String, String](producerProperties)
      )
      val repository = LiveCassandraRepository(
        session,
        CassandraStatements.prepare(session)
      )
      val openTelemetry = telemetry.openTelemetry
      val decision = V30RecordProcessor(
        V22Profiles.clearingMAD,
        V32HashBoundary.sha256,
        OpenTelemetryRailwayStageObserver(openTelemetry)
      )
      val durable = DurableRecordProcessor(
        decision.process,
        repository,
        TracingKafkaDecisionPublisher(producer, openTelemetry),
        observer = OpenTelemetryDurableStageObserver(openTelemetry)
      )
      val processing = TracingDurableProcessor(
        ObservedDurableProcessor(metrics.instrument(durable)),
        openTelemetry
      )
      val runner = V31ConsumerBatchRunner(
        DurableBatchCoordinator(processing),
        KafkaOffsetCommitter(consumer),
        KafkaPartitionRewinder(consumer)
      )
      val loop = KafkaConsumerLoopV31(
        consumer,
        runner,
        List(producer, session),
        pollObserver = KafkaLagObserver(registry)
      )
      new V32ConsumerRuntime(
        KafkaConsumerControl(loop),
        List(metricsServer, telemetry, registryResource)
      )
    catch
      case NonFatal(error) =>
        acquisitions.closeAfter(error)
        throw error

private final class KafkaConsumerControl(
  loop: KafkaConsumerLoopV31
) extends ConsumerControl:
  def pollOnce(): BatchReport = loop.pollOnce()
  def runUntil(stopRequested: () => Boolean): Unit = loop.runUntil(stopRequested)
  def wakeup(): Unit = loop.wakeup()
  def close(): Unit = loop.close()

private final class RegistryResource(
  registry: PrometheusMeterRegistry
) extends AutoCloseable:
  def close(): Unit = registry.close()

private object V32HashBoundary:
  val sha256: HashBoundary = iban =>
    SecurityUtils
      .hashIbanTry(iban.value)
      .toEither
      .left
      .map(_ => HashFailure.Unavailable)
      .flatMap: rawHash =>
        IbanHash.from(rawHash).left.map(_ => HashFailure.InvalidDigest)

private final class ResourceAcquisitions:
  private val acquired = ListBuffer.empty[AutoCloseable]

  def acquire[A <: AutoCloseable](resource: => A): A =
    val value = resource
    acquired.prepend(value)
    value

  def closeAfter(error: Throwable): Unit =
    try RuntimeResources.closeAll(acquired.toList)
    catch case NonFatal(closeError) => error.addSuppressed(closeError)

private object ResourceAcquisitions:
  def apply(): ResourceAcquisitions = new ResourceAcquisitions()

private object RuntimeResources:
  def closeAll(resources: List[AutoCloseable]): Unit =
    var firstFailure: Option[Throwable] = None
    resources.foreach: resource =>
      try resource.close()
      catch
        case NonFatal(error) =>
          firstFailure match
            case None        => firstFailure = Some(error)
            case Some(first) => first.addSuppressed(error)
    firstFailure.foreach(throw _)

object ClearingAppV32:
  def run(command: V31Command, baseSettings: V32Settings): Unit = command match
    case V31Command.Consume(consumerCommand) =>
      val settings = baseSettings.copy(
        bootstrapServers = consumerCommand.bootstrapServers,
        groupId =
          if consumerCommand.groupId == "marc-clearing-v31" then
            baseSettings.groupId
          else consumerCommand.groupId
      )
      val runtime = V32ConsumerRuntime.live(settings)
      consumerCommand.maxRecords match
        case Some(maxRecords) =>
          try
            val report = runtime.runBounded(maxRecords)
            V30Reporter.print(
              s"CONSUMER_V32 published=${report.published} " +
                s"duplicates=${report.duplicates} " +
                s"failedPartitions=${report.failedPartitions.size}"
            )
          finally runtime.close()
        case None => runContinuously(runtime)
    case other => ClearingAppV31.run(other)

  private def runContinuously(runtime: V32ConsumerRuntime): Unit =
    val stopped = AtomicBoolean(false)
    val hook = Thread(() =>
      stopped.set(true)
      runtime.wakeup()
    )
    Runtime.getRuntime.addShutdownHook(hook)
    try runtime.runUntil(() => stopped.get())
    catch
      case _: org.apache.kafka.common.errors.WakeupException if stopped.get() =>
        ()
    finally
      runtime.close()
      if !stopped.get() then Runtime.getRuntime.removeShutdownHook(hook)

@main def runClearingAppV32(args: String*): Unit =
  (V31Cli.parse(args.toList), V32Settings.fromEnvironment()) match
    case (Left(error), _)          => V30Reporter.print(error)
    case (_, Left(settingsError)) => V30Reporter.print(settingsError)
    case (Right(command), Right(settings)) =>
      ClearingAppV32.run(command, settings)
