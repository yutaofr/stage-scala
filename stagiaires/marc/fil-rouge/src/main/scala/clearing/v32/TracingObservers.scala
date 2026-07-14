package clearing.v32

import clearing.v22.{RailwayStage, RailwayStageObserver}
import clearing.v30.RecordEnvelope
import clearing.v31.{DurableProcessing, DurableRecordOutcome, DurableStageObserver}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.{Span, SpanKind, StatusCode, Tracer}
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import scala.jdk.CollectionConverters.*

final class OpenTelemetryRailwayStageObserver(
  openTelemetry: OpenTelemetry
) extends RailwayStageObserver:
  private val tracer = openTelemetry.getTracer("clearing-engine")

  def around[A](stage: RailwayStage)(operation: => A): A =
    TracingOperation.around(tracer, stage.spanName)(operation)

final class OpenTelemetryDurableStageObserver(
  openTelemetry: OpenTelemetry
) extends DurableStageObserver:
  private val tracer = openTelemetry.getTracer("clearing-engine")

  def aroundPersist[A](operation: => A): A =
    TracingOperation.around(tracer, "persist")(operation)

final class TracingDurableProcessor(
  delegate: DurableProcessing,
  openTelemetry: OpenTelemetry
) extends DurableProcessing:
  private val tracer = openTelemetry.getTracer("clearing-engine")
  private val propagator = openTelemetry.getPropagators.getTextMapPropagator

  def process(envelope: RecordEnvelope): DurableRecordOutcome =
    val parent = propagator.extract(
      Context.root(),
      envelope.headers,
      EnvelopeHeaderGetter
    )
    val span = tracer
      .spanBuilder("clearing.consume")
      .setParent(parent)
      .setSpanKind(SpanKind.CONSUMER)
      .startSpan()
    span.setAttribute("messaging.destination.name", envelope.topic)
    span.setAttribute("messaging.kafka.partition", envelope.partition.toLong)
    span.setAttribute("messaging.kafka.offset", envelope.offset)
    envelope.headers.get("transaction-id").foreach: txId =>
      span.setAttribute("tx.id", txId)

    val scope = span.makeCurrent()
    try
      val outcome = delegate.process(envelope)
      outcome match
        case DurableRecordOutcome.Failed(_) =>
          span.setStatus(StatusCode.ERROR, "processing failed")
        case _ => ()
      outcome
    catch
      case error: Throwable =>
        span.recordException(error)
        span.setStatus(StatusCode.ERROR)
        throw error
    finally
      scope.close()
      span.end()

private object EnvelopeHeaderGetter
    extends TextMapGetter[Map[String, String]]:
  def keys(carrier: Map[String, String]): java.lang.Iterable[String] =
    carrier.keys.asJava

  def get(carrier: Map[String, String], key: String): String =
    if carrier == null then null
    else carrier.getOrElse(key, null)

private object TracingOperation:
  def around[A](tracer: Tracer, name: String)(operation: => A): A =
    val span = tracer.spanBuilder(name).setSpanKind(SpanKind.INTERNAL).startSpan()
    val scope = span.makeCurrent()
    try operation
    catch
      case error: Throwable =>
        span.recordException(error)
        span.setStatus(StatusCode.ERROR)
        throw error
    finally
      scope.close()
      span.end()
