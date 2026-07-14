package clearing.v32

import clearing.v30.*
import clearing.v31.{DurableProcessing, DurableRecordOutcome}
import java.nio.charset.StandardCharsets
import java.time.Instant
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters.*

final class KafkaTracePropagationSpec extends AnyFlatSpec with Matchers:
  "TracingKafkaDecisionPublisher" should "inject W3C context and continue it on consume" in:
    val test = TestTelemetry()
    val kafka = MockProducer[String, String](
      true,
      null,
      StringSerializer(),
      StringSerializer()
    )
    try
      val parent = test.openTelemetry
        .getTracer("clearing.v32.test")
        .spanBuilder("upstream")
        .startSpan()
      val publisher = TracingKafkaDecisionPublisher(kafka, test.openTelemetry)

      val scope = parent.makeCurrent()
      try publisher.publish(Some("ATH"), rejected) shouldBe Right(())
      finally
        scope.close()
        parent.end()

      val output = kafka.history().get(0)
      val traceparent = String(
        output.headers().lastHeader("traceparent").value(),
        StandardCharsets.UTF_8
      )
      traceparent should fullyMatch regex
        "00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}"
      val propagated = RecordEnvelope(
        KafkaSettings.InputTopic,
        0,
        7L,
        Some("ATH"),
        Map("transaction-id" -> "42", "traceparent" -> traceparent),
        "payload",
        instant
      )
      TracingDurableProcessor(success, test.openTelemetry).process(propagated)

      val spans = test.finished
      val upstream = spans.find(_.getName == "upstream").get
      val consume = spans.find(_.getName == "clearing.consume").get
      consume.getTraceId shouldBe upstream.getTraceId
      consume.getParentSpanId shouldBe upstream.getSpanId
    finally
      kafka.close()
      test.close()

  it should "start a new trace when the record has no remote context" in:
    val test = TestTelemetry()
    try
      TracingDurableProcessor(success, test.openTelemetry).process(
        RecordEnvelope(
          KafkaSettings.InputTopic,
          0,
          8L,
          Some("ATH"),
          Map("transaction-id" -> "43"),
          "payload",
          instant
        )
      )

      val consume = test.finished.find(_.getName == "clearing.consume").get
      consume.getParentSpanContext.isValid shouldBe false
      consume.getSpanContext.isValid shouldBe true
    finally test.close()

  private val instant = Instant.parse("2026-07-14T12:00:00Z")
  private val success = new DurableProcessing:
    def process(envelope: RecordEnvelope) = DurableRecordOutcome.Published
  private val rejected = ProcessingDecision.Rejected(
    RejectedEvent(Some(42), "TEST", "rejet", "a" * 64, instant)
  )
