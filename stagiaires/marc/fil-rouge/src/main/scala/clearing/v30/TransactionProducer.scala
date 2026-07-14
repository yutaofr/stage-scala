package clearing.v30

import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.{CountDownLatch, TimeUnit, TimeoutException}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, Producer, ProducerRecord, RecordMetadata}
import scala.util.Try
import scala.util.control.NonFatal

final case class ProducerReport(
  attempted: Int,
  acknowledged: Int,
  failed: Int
)

final class Pacer private (waitAction: () => Unit):
  def awaitNext(): Unit = waitAction()

object Pacer:
  def apply(waitAction: () => Unit): Pacer = new Pacer(waitAction)

  val noWait: Pacer = Pacer(() => ())

  def fixedRate(eventsPerSecond: Int): Pacer =
    require(eventsPerSecond > 0, "le débit doit être strictement positif")
    val intervalNanos = 1000000000L / eventsPerSecond
    Pacer(() => LockSupport.parkNanos(intervalNanos))

final class TransactionProducer(
  producer: Producer[String, String],
  pacer: Pacer,
  callbackTimeout: Duration = Duration.ofMinutes(2)
) extends AutoCloseable:
  def send(events: List[InputTransactionEvent]): ProducerReport =
    val acknowledged = new AtomicInteger(0)
    val failed = new AtomicInteger(0)
    val callbacks = new CountDownLatch(events.size)
    val callback = new Callback:
      def onCompletion(
        metadata: RecordMetadata,
        exception: Exception
      ): Unit =
        if exception == null then acknowledged.incrementAndGet()
        else failed.incrementAndGet()
        callbacks.countDown()

    events.zipWithIndex.foreach: (event, index) =>
      val record = new ProducerRecord[String, String](
        KafkaSettings.InputTopic,
        event.sender,
        EventCodec.encodeInput(event)
      )
      record
        .headers()
        .add(
          "transaction-id",
          event.id.toString.getBytes(StandardCharsets.UTF_8)
        )
      try producer.send(record, callback)
      catch
        case NonFatal(_) =>
          failed.incrementAndGet()
          callbacks.countDown()

      if index < events.size - 1 then pacer.awaitNext()

    producer.flush()
    val completed = callbacks.await(
      callbackTimeout.toMillis,
      TimeUnit.MILLISECONDS
    )
    if !completed then
      throw new TimeoutException("callbacks Kafka incomplets avant timeout")

    ProducerReport(events.size, acknowledged.get(), failed.get())

  override def close(): Unit = producer.close()

object TransactionProducer:
  def live(
    bootstrapServers: String,
    eventsPerSecond: Int
  ): TransactionProducer =
    val producer = new KafkaProducer[String, String](
      KafkaProducerSettings.properties(bootstrapServers)
    )
    TransactionProducer(producer, Pacer.fixedRate(eventsPerSecond))

final case class ProducerCommand(
  count: Int,
  seed: Long,
  rate: Int,
  bootstrapServers: String,
  rejectEvery: Option[Int] = None
)

object ProducerCli:
  val Usage =
    "Usage : producer [--count N] [--seed N] [--rate N] " +
      "[--reject-every N] " +
      "[--bootstrap-servers HOST:PORT]"

  private val default = ProducerCommand(50, 1500L, 10, "localhost:9092")

  def parse(args: List[String]): Either[String, ProducerCommand] =
    def loop(
      remaining: List[String],
      command: ProducerCommand
    ): Either[String, ProducerCommand] = remaining match
      case Nil => Right(command)
      case "--count" :: raw :: tail =>
        positiveInt(raw).flatMap(value => loop(tail, command.copy(count = value)))
      case "--seed" :: raw :: tail =>
        Try(raw.toLong).toEither.left.map(_ => Usage).flatMap: value =>
          loop(tail, command.copy(seed = value))
      case "--rate" :: raw :: tail =>
        positiveInt(raw).flatMap(value => loop(tail, command.copy(rate = value)))
      case "--reject-every" :: raw :: tail =>
        positiveInt(raw).flatMap(value =>
          loop(tail, command.copy(rejectEvery = Some(value)))
        )
      case "--bootstrap-servers" :: value :: tail if value.nonEmpty =>
        loop(tail, command.copy(bootstrapServers = value))
      case _ => Left(Usage)

    loop(args, default)

  private def positiveInt(raw: String): Either[String, Int] =
    Try(raw.toInt).toEither.left
      .map(_ => Usage)
      .flatMap(value => Either.cond(value > 0, value, Usage))

object ProducerApp:
  def run(command: ProducerCommand): ProducerReport =
    val producer = TransactionProducer.live(
      command.bootstrapServers,
      command.rate
    )
    try
      producer.send(
        command.rejectEvery match
          case Some(rejectEvery) =>
            TransactionGenerator.generateMixed(
              command.count,
              command.seed,
              rejectEvery
            )
          case None =>
            TransactionGenerator.generate(command.count, command.seed)
      )
    finally producer.close()

@main def runTransactionProducerV30(args: String*): Unit =
  ProducerCli.parse(args.toList) match
    case Left(error) => V30Reporter.print(error)
    case Right(command) =>
      val report = ProducerApp.run(command)
      V30Reporter.print(V30Renderer.producer(report))
