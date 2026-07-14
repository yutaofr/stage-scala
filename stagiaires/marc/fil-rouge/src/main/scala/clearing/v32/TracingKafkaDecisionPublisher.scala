package clearing.v32

import clearing.v30.{DecisionPublisher, KafkaDecisionPublisher, KafkaRecordHeaderInjector}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapSetter
import java.nio.charset.StandardCharsets
import org.apache.kafka.clients.producer.{Producer, ProducerRecord}

object TracingKafkaDecisionPublisher:
  def apply(
    producer: Producer[String, String],
    openTelemetry: OpenTelemetry
  ): DecisionPublisher =
    KafkaDecisionPublisher(
      producer,
      OpenTelemetryKafkaHeaderInjector(openTelemetry)
    )

final class OpenTelemetryKafkaHeaderInjector(
  openTelemetry: OpenTelemetry
) extends KafkaRecordHeaderInjector:
  private val propagator = openTelemetry.getPropagators.getTextMapPropagator

  def inject(record: ProducerRecord[String, String]): Unit =
    propagator.inject(Context.current(), record, ProducerRecordSetter)

private object ProducerRecordSetter
    extends TextMapSetter[ProducerRecord[String, String]]:
  def set(
    carrier: ProducerRecord[String, String],
    key: String,
    value: String
  ): Unit =
    if carrier != null then
      carrier.headers().remove(key)
      carrier.headers().add(key, value.getBytes(StandardCharsets.UTF_8))
