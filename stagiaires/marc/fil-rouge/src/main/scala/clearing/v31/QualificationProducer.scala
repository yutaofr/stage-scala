package clearing.v31

import clearing.v30.{EventCodec, KafkaProducerSettings, KafkaSettings, ProducerReport, TransactionGenerator}
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.{CountDownLatch, TimeUnit, TimeoutException}
import java.util.concurrent.atomic.AtomicInteger
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, Producer, ProducerRecord, RecordMetadata}
import scala.util.control.NonFatal

final case class QualificationRecord(
  key: String,
  value: String,
  transactionId: Option[Int]
)

object V31QualificationScenario:
  val UniqueValid = 485
  val Invalid = 5
  val Replays = 10
  val Total = UniqueValid + Invalid + Replays

  def records(seed: Long): List[QualificationRecord] =
    val unique = TransactionGenerator
      .generate(UniqueValid, seed)
      .map: event =>
        QualificationRecord(
          event.sender,
          EventCodec.encodeInput(event),
          Some(event.id)
        )
    val invalid = (1 to Invalid).map: index =>
      QualificationRecord(
        key = s"invalid-$index",
        value = s"{invalid-json-$index",
        transactionId = None
      )

    unique ::: invalid.toList ::: unique.take(Replays)

final class V31QualificationProducer(
  producer: Producer[String, String],
  callbackTimeout: Duration = Duration.ofMinutes(2)
) extends AutoCloseable:
  def send(records: List[QualificationRecord]): ProducerReport =
    val acknowledged = new AtomicInteger(0)
    val failed = new AtomicInteger(0)
    val callbacks = new CountDownLatch(records.size)
    val callback = new Callback:
      def onCompletion(metadata: RecordMetadata, error: Exception): Unit =
        if error == null then acknowledged.incrementAndGet()
        else failed.incrementAndGet()
        callbacks.countDown()

    records.foreach: value =>
      val record = new ProducerRecord[String, String](
        KafkaSettings.InputTopic,
        value.key,
        value.value
      )
      value.transactionId.foreach: id =>
        record.headers().add(
          "transaction-id",
          id.toString.getBytes(StandardCharsets.UTF_8)
        )
      try producer.send(record, callback)
      catch
        case NonFatal(_) =>
          failed.incrementAndGet()
          callbacks.countDown()

    producer.flush()
    val complete = callbacks.await(
      callbackTimeout.toMillis,
      TimeUnit.MILLISECONDS
    )
    if !complete then
      throw new TimeoutException("callbacks Kafka v3.1 incomplets avant timeout")

    ProducerReport(records.size, acknowledged.get(), failed.get())

  override def close(): Unit = producer.close()

object V31QualificationProducer:
  def live(bootstrapServers: String): V31QualificationProducer =
    V31QualificationProducer(
      new KafkaProducer[String, String](
        KafkaProducerSettings.properties(bootstrapServers)
      )
    )

final case class QualificationCommand(
  seed: Long,
  bootstrapServers: String
)

object QualificationCli:
  val Usage =
    "Usage : qualify [--seed N] [--bootstrap-servers HOST:PORT]"

  private val default = QualificationCommand(1600L, "localhost:9092")

  def parse(args: List[String]): Either[String, QualificationCommand] =
    def loop(
      remaining: List[String],
      command: QualificationCommand
    ): Either[String, QualificationCommand] = remaining match
      case Nil => Right(command)
      case "--seed" :: raw :: tail =>
        raw.toLongOption
          .toRight(Usage)
          .flatMap(value => loop(tail, command.copy(seed = value)))
      case "--bootstrap-servers" :: value :: tail if value.nonEmpty =>
        loop(tail, command.copy(bootstrapServers = value))
      case _ => Left(Usage)

    loop(args, default)
