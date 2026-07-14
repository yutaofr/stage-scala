package clearing.v31

import clearing.v30.*
import java.time.{Duration, Instant, LocalDate}
import org.apache.kafka.clients.consumer.{ConsumerRecord, MockConsumer, OffsetResetStrategy}
import org.apache.kafka.common.TopicPartition
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters.*

final class KafkaConsumerV31Spec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")

  "V31ConsumerCli" should "conserver un consumer borné à un record par poll" in:
    V31ConsumerCli.parse(Nil) shouldBe Right(
      V31ConsumerCommand("localhost:9092", "marc-clearing-v31", None)
    )
    V31ConsumerCli.parse(
      List("--group-id", "preuve", "--max-records", "500")
    ) shouldBe Right(
      V31ConsumerCommand("localhost:9092", "preuve", Some(500))
    )
    V31ConsumerCli.parse(List("--max-records", "0")) shouldBe
      Left(V31ConsumerCli.Usage)

    KafkaConsumerSettings
      .properties("localhost:9092", "v31", Some(1))
      .getProperty("max.poll.records") shouldBe "1"

  "KafkaConsumerLoopV31" should "committer puis rewind avant le poll suivant" in:
    val consumer = new MockConsumer[String, String](OffsetResetStrategy.EARLIEST)
    val partition = new TopicPartition(KafkaSettings.InputTopic, 0)
    consumer.assign(List(partition).asJava)
    consumer.updateBeginningOffsets(
      Map(partition -> java.lang.Long.valueOf(0L)).asJava
    )
    List(0L -> "1", 1L -> "2", 2L -> "3").foreach: (offset, value) =>
      consumer.addRecord(
        new ConsumerRecord(KafkaSettings.InputTopic, 0, offset, "AWB", value)
      )

    val attempts = collection.mutable.ListBuffer.empty[Int]
    var failTwoOnce = true
    val repository = InMemoryDurableRepository()
    val recordProcessor = DurableRecordProcessor(
      envelope => rejected(envelope.value.toInt),
      repository,
      DecisionPublisher: (_, decision) =>
        val id = decision.transactionId.get
        attempts += id
        if id == 2 && failTwoOnce then
          failTwoOnce = false
          Left(PublishingFailure("panne"))
        else Right(()),
      () => instant
    )
    val runner = V31ConsumerBatchRunner(
      DurableBatchCoordinator(recordProcessor),
      KafkaOffsetCommitter(consumer),
      KafkaPartitionRewinder(consumer)
    )
    val loop = KafkaConsumerLoopV31(
      consumer,
      runner,
      Nil,
      Duration.ofMillis(1)
    )

    val first = loop.pollOnce()
    first.retryOffsets shouldBe Map(InputPartition(0) -> 1L)
    consumer.position(partition) shouldBe 1L
    consumer.committed(Set(partition).asJava).get(partition).offset() shouldBe 1L

    List(1L -> "2", 2L -> "3").foreach: (offset, value) =>
      consumer.addRecord(
        new ConsumerRecord(KafkaSettings.InputTopic, 0, offset, "AWB", value)
      )
    loop.pollOnce().retryOffsets shouldBe empty

    attempts.toList shouldBe List(1, 2, 2, 3)
    consumer.committed(Set(partition).asJava).get(partition).offset() shouldBe 3L
    loop.pollOnce().published shouldBe 0
    attempts.toList shouldBe List(1, 2, 2, 3)

  it should "fermer toutes les ressources même dans le mode test" in:
    val consumer = new MockConsumer[String, String](OffsetResetStrategy.EARLIEST)
    val runner = V31ConsumerBatchRunner(
      DurableBatchCoordinator(
        DurableRecordProcessor(
          _ => rejected(1),
          InMemoryDurableRepository(),
          DecisionPublisher((_, _) => Right(())),
          () => instant
        )
      ),
      OffsetCommitter(_ => ()),
      PartitionRewinder(_ => ())
    )
    val closed = collection.mutable.ListBuffer.empty[String]
    def resource(name: String): AutoCloseable = () => closed += name
    val loop = KafkaConsumerLoopV31(
      consumer,
      runner,
      List(resource("producer"), resource("cassandra"))
    )

    loop.close()

    closed.toList shouldBe List("producer", "cassandra")

  "V31Cli" should "router producer, consumer et report" in:
    V31Cli.parse(List("producer", "--count", "50")) shouldBe Right(
      V31Command.Produce(ProducerCommand(50, 1500L, 10, "localhost:9092"))
    )
    V31Cli.parse(
      List("consumer", "--group-id", "demo", "--max-records", "50")
    ) shouldBe Right(
      V31Command.Consume(
        V31ConsumerCommand("localhost:9092", "demo", Some(50))
      )
    )
    V31Cli.parse(
      List("report", "--bank", "AWB", "--date", "2026-07-14")
    ) shouldBe Right(
      V31Command.Report(ReportCommand("AWB", LocalDate.parse("2026-07-14"), 10))
    )

  "V31BatchReports" should "retirer une partition après sa reprise" in:
    val partition = InputPartition(0)
    val failed = BatchReport(
      committableOffsets = Map(partition -> 1L),
      published = 1,
      duplicates = 0,
      failedPartitions = Set(partition),
      retryOffsets = Map(partition -> 1L)
    )
    val recovered = BatchReport(
      committableOffsets = Map(partition -> 3L),
      published = 2,
      duplicates = 0,
      failedPartitions = Set.empty
    )

    V31BatchReports.combine(failed, recovered) shouldBe BatchReport(
      committableOffsets = Map(partition -> 3L),
      published = 3,
      duplicates = 0,
      failedPartitions = Set.empty,
      retryOffsets = Map.empty
    )

  private def rejected(id: Int): ProcessingDecision =
    ProcessingDecision.Rejected(
      RejectedEvent(Some(id), "TEST", "rejet", id.toString * 64, instant)
    )
