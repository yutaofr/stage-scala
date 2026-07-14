package clearing.v32

import io.circe.Json
import io.circe.parser.parse
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class GrafanaProvisioningSpec extends AnyFlatSpec with Matchers:
  private val root = projectRoot()
  private val grafana = root.resolve("docker/observability/grafana")

  "Grafana provisioning" should "bind a stable Prometheus datasource UID" in:
    val datasource = read(
      grafana.resolve("provisioning/datasources/prometheus.yml")
    )
    datasource should include("uid: prometheus-clearing")
    datasource should include("type: prometheus")
    datasource should include("url: http://prometheus:9090")
    datasource should include("access: proxy")
    datasource should include("editable: false")

  it should "load the versioned dashboard directory without UI setup" in:
    val provider = read(
      grafana.resolve("provisioning/dashboards/clearing.yml")
    )
    provider should include("path: /var/lib/grafana/dashboards")
    provider should include("disableDeletion: true")
    provider should include("editable: false")

  "The Clearing Engine dashboard" should "contain the five required panels with units" in:
    val json = dashboard
    val panels = json.hcursor.downField("panels").as[List[Json]].toOption.get
    panels.map(_.hcursor.get[String]("title").toOption.get).toSet shouldBe Set(
      "Throughput",
      "Error rate",
      "Processing p99",
      "Kafka lag",
      "JVM memory"
    )
    panels.foreach: panel =>
      panel.hcursor.downField("fieldConfig").downField("defaults")
        .get[String]("unit").isRight shouldBe true
    expression(panels, "Throughput") should include(
      "rate(clearing_transactions_processed_total"
    )
    expression(panels, "Error rate") should include("status=\"failure\"")
    expression(panels, "Processing p99") should include("histogram_quantile(0.99")
    expression(panels, "Kafka lag") should include("clearing_kafka_lag_records")
    expression(panels, "JVM memory") should include("jvm_memory_used_bytes")

  it should "offer only the bounded status variable with a correct All query" in:
    val json = dashboard
    val variables = json.hcursor
      .downField("templating")
      .downField("list")
      .as[List[Json]]
      .toOption
      .get
    variables should have size 1
    val status = variables.head.hcursor
    status.get[String]("name") shouldBe Right("status")
    status.get[String]("query") shouldBe Right(
      "label_values(clearing_transactions_processed_total, status)"
    )
    status.get[Boolean]("includeAll") shouldBe Right(true)
    status.get[String]("allValue") shouldBe Right(".*")
    val expressions = json.hcursor
      .downField("panels")
      .as[List[Json]]
      .toOption
      .get
      .flatMap(targetExpressions)
    expressions.exists(_.contains("status=~\"$status\"")) shouldBe true
    json.noSpaces should not include "$bank"

  it should "be mounted read only by the pinned Grafana service" in:
    val compose = read(root.resolve("docker/docker-compose-v32.yml"))
    compose should include("grafana/grafana:13.0.2")
    compose should include(
      "./observability/grafana/provisioning:/etc/grafana/provisioning:ro"
    )
    compose should include(
      "./observability/grafana/dashboards:/var/lib/grafana/dashboards:ro"
    )
    compose should include("grafana-data:/var/lib/grafana")

  private def dashboard: Json =
    parse(read(grafana.resolve("dashboards/clearing-engine-v32.json")))
      .fold(error => fail(error.message), identity)

  private def expression(panels: List[Json], title: String): String =
    panels
      .find(_.hcursor.get[String]("title") == Right(title))
      .flatMap(targetExpressions(_).headOption)
      .getOrElse(fail(s"panel $title absent"))

  private def targetExpressions(panel: Json): List[String] =
    panel.hcursor
      .downField("targets")
      .as[List[Json]]
      .toOption
      .getOrElse(Nil)
      .flatMap(_.hcursor.get[String]("expr").toOption)

  private def projectRoot(): Path =
    Iterator
      .iterate(Paths.get("").toAbsolutePath)(_.getParent)
      .takeWhile(_ != null)
      .find(path => Files.exists(path.resolve("build.sbt")))
      .getOrElse(throw IllegalStateException("racine sbt introuvable"))

  private def read(path: Path): String =
    Files.readString(path, StandardCharsets.UTF_8)
