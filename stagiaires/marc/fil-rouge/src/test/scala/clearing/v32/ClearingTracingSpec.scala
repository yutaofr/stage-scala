package clearing.v32

import clearing.v22.{HashBoundary, V22Profiles}
import clearing.v22.DomainTypes.*
import clearing.v30.*
import clearing.v31.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.{SpanKind, StatusCode}
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters.*

final class ClearingTracingSpec extends AnyFlatSpec with Matchers:
  "The v3.2 tracing adapters" should "record the four real stages under one consumer span" in:
    val test = TestTelemetry()
    try
      val railway = OpenTelemetryRailwayStageObserver(test.openTelemetry)
      val persistence = OpenTelemetryDurableStageObserver(test.openTelemetry)
      val decision = V30RecordProcessor(
        V22Profiles.clearingMAD,
        workingHash,
        railway
      )
      val durable = DurableRecordProcessor(
        decision.process,
        InMemoryDurableRepository(),
        DecisionPublisher((_, _) => Right(())),
        () => instant,
        persistence
      )
      val traced = TracingDurableProcessor(durable, test.openTelemetry)

      traced.process(validEnvelope) shouldBe DurableRecordOutcome.Published

      val spans = test.finished
      spans.map(_.getName).toSet shouldBe
        Set("clearing.consume", "parse", "validate", "netting", "persist")
      val consume = named(spans, "clearing.consume")
      consume.getKind shouldBe SpanKind.CONSUMER
      consume.getAttributes.get(AttributeKey.stringKey("tx.id")) shouldBe "1"
      consume.getParentSpanContext.isValid shouldBe false
      List("parse", "validate", "netting", "persist").foreach: name =>
        val child = named(spans, name)
        child.getTraceId shouldBe consume.getTraceId
        child.getParentSpanId shouldBe consume.getSpanId
      spans.map(_.getName).foreach(_ should not include "1")
    finally test.close()

  it should "mark an unexpected processing exception as an error" in:
    val test = TestTelemetry()
    try
      val traced = TracingDurableProcessor(
        new DurableProcessing:
          def process(envelope: RecordEnvelope): DurableRecordOutcome =
            throw new IllegalStateException("boom"),
        test.openTelemetry
      )

      intercept[IllegalStateException]:
        traced.process(validEnvelope)

      val consume = named(test.finished, "clearing.consume")
      consume.getStatus.getStatusCode shouldBe StatusCode.ERROR
      consume.getEvents.asScala.map(_.getName) should contain("exception")
    finally test.close()

  it should "replace an untrusted transaction header before tracing it" in:
    val test = TestTelemetry()
    try
      val sensitive = "MA64011519000001205000534921-client-secret"
      val traced = TracingDurableProcessor(
        _ => DurableRecordOutcome.Duplicate,
        test.openTelemetry
      )

      traced.process(validEnvelope.copy(headers = Map("transaction-id" -> sensitive)))

      val consume = named(test.finished, "clearing.consume")
      consume.getAttributes.get(AttributeKey.stringKey("tx.id")) shouldBe
        "unknown"
      consume.getAttributes.asMap.toString should not include sensitive
    finally test.close()

  private val instant = Instant.parse("2026-07-14T12:00:00Z")
  private val safeHash = IbanHash.from("a" * 64).toOption.get
  private val workingHash: HashBoundary = _ => Right(safeHash)

  private val validEnvelope = RecordEnvelope(
    KafkaSettings.InputTopic,
    0,
    7L,
    Some("ATH"),
    Map("transaction-id" -> "1"),
    EventCodec.encodeInput(
      InputTransactionEvent(
        1,
        "ATH",
        "BOA",
        "MA64ATH00000000000000000",
        "MA64BOA00000000000000000",
        BigDecimal("100.00"),
        "VIR",
        "MAD"
      )
    ),
    instant
  )

  private def named(spans: List[SpanData], name: String): SpanData =
    spans.find(_.getName == name).getOrElse(fail(s"span $name absent"))

private final class TestTelemetry private (
  val openTelemetry: OpenTelemetrySdk,
  provider: SdkTracerProvider,
  exporter: InMemorySpanExporter
) extends AutoCloseable:
  def finished: List[SpanData] =
    provider.forceFlush().join(5, java.util.concurrent.TimeUnit.SECONDS)
    exporter.getFinishedSpanItems.asScala.toList

  def close(): Unit =
    provider.close()
    exporter.close()

private object TestTelemetry:
  def apply(): TestTelemetry =
    val exporter = InMemorySpanExporter.create()
    val provider = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))
      .build()
    val openTelemetry = OpenTelemetrySdk
      .builder()
      .setTracerProvider(provider)
      .setPropagators(
        ContextPropagators.create(W3CTraceContextPropagator.getInstance())
      )
      .build()
    new TestTelemetry(openTelemetry, provider, exporter)
