package clearing.v32

import clearing.v30.*
import clearing.v31.*
import io.micrometer.prometheusmetrics.{PrometheusConfig, PrometheusMeterRegistry}
import java.time.{Duration, Instant}
import org.apache.kafka.clients.consumer.{ConsumerRecord, MockConsumer, OffsetResetStrategy}
import org.apache.kafka.common.TopicPartition
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters.*

final class KafkaLagObserverSpec extends AnyFlatSpec with Matchers:
  "KafkaLagObserver" should "publish end offset minus current position" in:
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val observer = KafkaLagObserver(registry)
    val consumer = mockConsumer(endOffset = 12L)
    val partition = new TopicPartition(KafkaSettings.InputTopic, 0)
    consumer.seek(partition, 5L)

    observer.afterPoll(consumer)

    gauge(registry, partition) shouldBe 7.0
    consumer.seek(partition, 12L)
    observer.afterPoll(consumer)
    gauge(registry, partition) shouldBe 0.0

  it should "be called by the v3.1 loop after every poll" in:
    val consumer = mockConsumer(endOffset = 1L)
    consumer.addRecord(
      ConsumerRecord(KafkaSettings.InputTopic, 0, 0L, "AWB", "payload")
    )
    var observations = 0
    val observer = new ConsumerPollObserver:
      def afterPoll(current: org.apache.kafka.clients.consumer.Consumer[String, String]) =
        observations += 1
    val runner = V31ConsumerBatchRunner(
      DurableBatchCoordinator(
        DurableRecordProcessor(
          _ => ProcessingDecision.Rejected(
            RejectedEvent(
              Some(1),
              "TEST",
              "rejet",
              "a" * 64,
              Instant.parse("2026-07-14T12:00:00Z")
            )
          ),
          InMemoryDurableRepository(),
          DecisionPublisher((_, _) => Right(()))
        )
      ),
      OffsetCommitter(_ => ()),
      PartitionRewinder(_ => ())
    )
    val loop = KafkaConsumerLoopV31(
      consumer,
      runner,
      Nil,
      Duration.ofMillis(1),
      observer
    )

    loop.pollOnce()
    loop.pollOnce()

    observations shouldBe 2

  private def mockConsumer(endOffset: Long): MockConsumer[String, String] =
    val consumer = MockConsumer[String, String](OffsetResetStrategy.EARLIEST)
    val partition = TopicPartition(KafkaSettings.InputTopic, 0)
    consumer.assign(List(partition).asJava)
    consumer.updateBeginningOffsets(Map(partition -> Long.box(0L)).asJava)
    consumer.updateEndOffsets(Map(partition -> Long.box(endOffset)).asJava)
    consumer

  private def gauge(
    registry: PrometheusMeterRegistry,
    partition: TopicPartition
  ): Double =
    registry
      .find("clearing.kafka.lag.records")
      .tag("topic", partition.topic())
      .tag("partition", partition.partition().toString)
      .gauge()
      .value()
