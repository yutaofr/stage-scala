package clearing.v32

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class V32RuntimeQualificationSpec extends AnyFlatSpec with Matchers:
  private val root = projectRoot()

  "The v3.2 runtime gate" should "start clean and always remove containers and volumes" in:
    val script = gate
    script should include("set -euo pipefail")
    script should include("docker compose")
    script should include("down --volumes --remove-orphans")
    script should include("trap cleanup EXIT")

  it should "keep the recovered stack only when evidence capture is explicitly enabled" in:
    val script = gate
    script should include("KEEP_STACK")
    script should include("V32_RUNTIME_KEEP_STACK")
    script should include("start_app")

  it should "verify structured logs metrics traces dashboard and alert lifecycle" in:
    val script = gate
    List(
      "logback-json.xml",
      "jq -e",
      "clearing_transactions_processed_total",
      "clearing_processing_duration_seconds_bucket",
      "clearing_kafka_lag_records",
      "/api/v1/targets",
      "health=\"up\"",
      "health=\"down\"",
      "localhost:16686/api/traces",
      "clearing.consume",
      "localhost:3000/api/datasources/uid/prometheus-clearing",
      "localhost:3000/api/dashboards/uid/clearing-engine-v32",
      "localhost:9093/api/v2/alerts",
      "ALERT_BASELINE",
      "webhook_status_after",
      "alertmanager_absent",
      "pending",
      "firing",
      "resolved",
      "notifications.jsonl",
      "V32_RUNTIME_OK"
    ).foreach(script should include(_))

  it should "reconcile counts and reject sensitive values" in:
    val script = gate
    List(
      "EXPECTED_RECORDS",
      "METRIC_TOTAL",
      "METRIC_LAG",
      "clearing-input",
      "SENSITIVE_VALUES",
      "SENSITIVE_VALUE_COUNT",
      "grep -F -f",
      "sensitive data found"
    ).foreach(script should include(_))
    script should not include "MA64ATH00000000000000000"
    script should not include "MA64BOA00000000000000000"

  it should "leave delivery documents ready for the measured result" in:
    val readme = read(root.resolve("README.md"))
    val retro = read(root.resolve("retro_s17.md"))
    val tracking = read(root.getParent.resolve("suivi/semaine-17.md"))
    readme should include("v3.2")
    readme should include("verify-v32-runtime.sh")
    retro should include("Semaine 17")
    retro should include("preuves")
    tracking should include("Semaine 17")
    tracking should include("v3.2")

  private def gate: String = read(root.resolve("scripts/verify-v32-runtime.sh"))

  private def projectRoot(): Path =
    Iterator
      .iterate(Paths.get("").toAbsolutePath)(_.getParent)
      .takeWhile(_ != null)
      .find(path => Files.exists(path.resolve("build.sbt")))
      .getOrElse(throw IllegalStateException("racine sbt introuvable"))

  private def read(path: Path): String =
    Files.readString(path, StandardCharsets.UTF_8)
