package clearing.v32

import io.opentelemetry.api.common.{AttributeKey, Attributes}
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.BatchSpanProcessor

final class ClearingTelemetry private (
  val openTelemetry: OpenTelemetrySdk,
  provider: SdkTracerProvider
) extends AutoCloseable:
  def close(): Unit = provider.close()

object ClearingTelemetry:
  def live(serviceName: String, otlpEndpoint: String): ClearingTelemetry =
    val exporter = OtlpGrpcSpanExporter
      .builder()
      .setEndpoint(otlpEndpoint)
      .build()
    val resource = Resource
      .getDefault()
      .merge(
        Resource.create(
          Attributes.of(AttributeKey.stringKey("service.name"), serviceName)
        )
      )
    val provider = SdkTracerProvider
      .builder()
      .setResource(resource)
      .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
      .build()
    val sdk = OpenTelemetrySdk
      .builder()
      .setTracerProvider(provider)
      .setPropagators(
        ContextPropagators.create(W3CTraceContextPropagator.getInstance())
      )
      .build()
    new ClearingTelemetry(sdk, provider)
