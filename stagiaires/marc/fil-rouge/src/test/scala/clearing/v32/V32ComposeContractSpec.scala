package clearing.v32

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class V32ComposeContractSpec extends AnyFlatSpec with Matchers:
  private val root = projectRoot()
  private val compose = read(root.resolve("docker/docker-compose-v32.yml"))

  "The v3.2 Compose laboratory" should "pin every infrastructure image" in:
    List(
      "apache/kafka:4.3.0",
      "cassandra:4.1.11",
      "otel/opentelemetry-collector-contrib:0.153.0",
      "jaegertracing/jaeger:2.18.0",
      "prom/prometheus:v3.12.0"
    ).foreach(compose should include(_))
    compose should not include ":latest"

  it should "healthcheck Kafka Cassandra Collector Jaeger and Prometheus" in:
    List("kafka:", "cassandra:", "otel-collector:", "jaeger:", "prometheus:")
      .foreach(compose should include(_))
    compose.sliding("healthcheck:".length).count(_ == "healthcheck:") should be >= 5
    compose should include("service_healthy")

  it should "mount versioned Collector and Prometheus configuration read only" in:
    compose should include(
      "./observability/otel-collector.yml:/etc/otelcol-contrib/config.yaml:ro"
    )
    compose should include(
      "./observability/prometheus.yml:/etc/prometheus/prometheus.yml:ro"
    )
    val collector = read(
      root.resolve("docker/observability/otel-collector.yml")
    )
    collector should include("otlp:")
    collector should include("jaeger:4317")
    val prometheus = read(root.resolve("docker/observability/prometheus.yml"))
    prometheus should include("host.docker.internal:8080")
    prometheus should include("alerting:")

  it should "avoid embedding dashboard credentials" in:
    compose should not include "GF_SECURITY_ADMIN_PASSWORD"
    compose should not include "GF_SECURITY_ADMIN_USER"

  private def projectRoot(): Path =
    Iterator
      .iterate(Paths.get("").toAbsolutePath)(_.getParent)
      .takeWhile(_ != null)
      .find(path => Files.exists(path.resolve("build.sbt")))
      .getOrElse(throw IllegalStateException("racine sbt introuvable"))

  private def read(path: Path): String =
    Files.readString(path, StandardCharsets.UTF_8)
