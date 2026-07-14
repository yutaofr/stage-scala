package clearing.v32

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class AlertingContractSpec extends AnyFlatSpec with Matchers:
  private val root = projectRoot()
  private val observability = root.resolve("docker/observability")

  "The v3.2 SLO document" should "define two complete 30-day objectives" in:
    val slo = read(root.resolve("slo-v32.md"))
    List(
      "SLI",
      "population",
      "target",
      "30 jours",
      "source",
      "budget d'erreur",
      "99,5 %",
      "99 %",
      "500 ms",
      "sans trafic",
      "maintenance",
      "données invalides"
    ).foreach(slo should include(_))

  "Prometheus rules" should "guard ratios and annotate every sustained alert" in:
    val rules = read(observability.resolve("clearing-rules.yml"))
    List(
      "alert: EngineDown",
      "alert: ClearingHighFailureRate",
      "alert: ClearingProcessingLatencyHigh"
    ).foreach(rules should include(_))
    rules should include("clamp_min")
    rules.sliding("for:".length).count(_ == "for:") shouldBe 3
    rules.sliding("severity:".length).count(_ == "severity:") shouldBe 3
    rules.sliding("summary:".length).count(_ == "summary:") shouldBe 3
    rules.sliding("description:".length).count(_ == "description:") shouldBe 3

  "Alertmanager" should "route firing and resolved alerts to a separate webhook" in:
    val config = read(observability.resolve("alertmanager.yml"))
    config should include("url: http://webhook:8081/alerts")
    config should include("send_resolved: true")
    config should include("receiver: clearing-webhook")
    val webhook = read(observability.resolve("webhook/app.py"))
    webhook should include("/data/notifications.jsonl")
    webhook should include("/health")
    webhook should include("/alerts")

  it should "mount all configs read only and persist webhook evidence" in:
    val compose = read(root.resolve("docker/docker-compose-v32.yml"))
    compose should include("prom/alertmanager:v0.32.1")
    compose should include("python:3.13.5-alpine3.22")
    compose should include(
      "./observability/clearing-rules.yml:/etc/prometheus/clearing-rules.yml:ro"
    )
    compose should include(
      "./observability/alertmanager.yml:/etc/alertmanager/alertmanager.yml:ro"
    )
    compose should include(
      "./observability/webhook/app.py:/app/app.py:ro"
    )
    compose should include("webhook-data:/data")

  private def projectRoot(): Path =
    Iterator
      .iterate(Paths.get("").toAbsolutePath)(_.getParent)
      .takeWhile(_ != null)
      .find(path => Files.exists(path.resolve("build.sbt")))
      .getOrElse(throw IllegalStateException("racine sbt introuvable"))

  private def read(path: Path): String =
    Files.readString(path, StandardCharsets.UTF_8)
