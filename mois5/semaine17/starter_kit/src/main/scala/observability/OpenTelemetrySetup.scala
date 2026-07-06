package observability

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.{TextMapGetter, TextMapSetter}
import org.apache.kafka.common.header.Headers
import scala.jdk.CollectionConverters.*

import java.nio.charset.StandardCharsets

object OpenTelemetrySetup:
  private val propagator = GlobalOpenTelemetry.getPropagators.getTextMapPropagator

  val headersSetter: TextMapSetter[Headers] = new TextMapSetter[Headers]:
    def set(carrier: Headers, key: String, value: String): Unit =
      if carrier != null then
        carrier.remove(key)
        carrier.add(key, value.getBytes(StandardCharsets.UTF_8))

  val headersGetter: TextMapGetter[Headers] = new TextMapGetter[Headers]:
    def keys(carrier: Headers): java.lang.Iterable[String] =
      if carrier == null then java.util.Collections.emptyList()
      else carrier.asScala.map(_.key()).asJava

    def get(carrier: Headers, key: String): String =
      if carrier == null then null
      else
        Option(carrier.lastHeader(key))
          .map(h => new String(h.value(), StandardCharsets.UTF_8))
          .orNull

  def injectContext(context: Context, headers: Headers): Unit =
    ??? // TODO : Injecter le contexte OpenTelemetry courant dans les headers Kafka

  def extractContext(headers: Headers): Context =
    ??? // TODO : Extraire le contexte OpenTelemetry parent depuis les headers Kafka
