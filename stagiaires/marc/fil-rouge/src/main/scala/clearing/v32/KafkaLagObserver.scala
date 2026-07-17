package clearing.v32

import clearing.v31.ConsumerPollObserver
import io.micrometer.core.instrument.Gauge
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

final class KafkaLagObserver(
  registry: PrometheusMeterRegistry
) extends ConsumerPollObserver:
  private val logger = LoggerFactory.getLogger(classOf[KafkaLagObserver])
  private val values = ConcurrentHashMap[TopicPartition, AtomicLong]()

  def afterPoll(consumer: Consumer[String, String]): Unit =
    try
      val assigned = consumer.assignment()
      val endOffsets = consumer.endOffsets(assigned)
      assigned.asScala.foreach: partition =>
        val lag = Math.max(
          0L,
          endOffsets.get(partition).longValue() - consumer.position(partition)
        )
        valueFor(partition).set(lag)
    catch
      case NonFatal(_) =>
        logger.warn("Kafka lag observation failed")

  private def valueFor(partition: TopicPartition): AtomicLong =
    values.computeIfAbsent(
      partition,
      current =>
        val value = AtomicLong(0L)
        Gauge
          .builder(
            "clearing.kafka.lag.records",
            value,
            currentValue => currentValue.get().toDouble
          )
          .description("Kafka records remaining for the assigned partition")
          .tag("topic", current.topic())
          .tag("partition", current.partition().toString)
          .register(registry)
        value
    )
