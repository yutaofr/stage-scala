package clearing.v32

import io.micrometer.prometheusmetrics.{PrometheusConfig, PrometheusMeterRegistry}
import java.net.{ConnectException, URI}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class MetricsHttpServerSpec extends AnyFlatSpec with Matchers:
  "MetricsHttpServer" should "serve Prometheus metrics and health on an ephemeral port" in:
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    registry.counter("clearing.test.counter").increment(2.0)
    val server = MetricsHttpServer.start(registry, 0)
    val client = HttpClient.newHttpClient()

    try
      val metrics = get(client, server.port, "/metrics")
      metrics.statusCode() shouldBe 200
      metrics.headers().firstValue("content-type").orElse("") should startWith(
        "text/plain"
      )
      metrics.body() should include("clearing_test_counter_total 2.0")

      val health = get(client, server.port, "/health")
      health.statusCode() shouldBe 200
      health.headers().firstValue("content-type").orElse("") should startWith(
        "text/plain"
      )
      health.body() shouldBe "UP\n"
    finally server.close()

    intercept[ConnectException]:
      get(client, server.port, "/health")

  private def get(
    client: HttpClient,
    port: Int,
    path: String
  ): HttpResponse[String] =
    client.send(
      HttpRequest
        .newBuilder(URI.create(s"http://127.0.0.1:$port$path"))
        .GET()
        .build(),
      HttpResponse.BodyHandlers.ofString()
    )
