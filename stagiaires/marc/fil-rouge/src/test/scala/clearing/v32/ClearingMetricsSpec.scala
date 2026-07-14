package clearing.v32

import clearing.v30.RecordEnvelope
import clearing.v31.{DurableProcessing, DurableRecordOutcome}
import io.micrometer.prometheusmetrics.{PrometheusConfig, PrometheusMeterRegistry}
import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ClearingMetricsSpec extends AnyFlatSpec with Matchers:
  "ClearingMetrics" should "count and time the three bounded outcomes" in:
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val metrics = ClearingMetrics(registry)
    val outcomes = collection.mutable.Queue(
      DurableRecordOutcome.Published,
      DurableRecordOutcome.Duplicate,
      DurableRecordOutcome.Failed("cassandra unavailable")
    )
    val processor = metrics.instrument(
      new DurableProcessing:
        def process(envelope: RecordEnvelope): DurableRecordOutcome =
          outcomes.dequeue()
    )

    List("41", "42", "43").foreach: txId =>
      processor.process(envelope(txId))

    List("success", "duplicate", "failure").foreach: status =>
      registry
        .find("clearing.transactions.processed")
        .tag("status", status)
        .counter()
        .count() shouldBe 1.0
      registry
        .find("clearing.processing.duration")
        .tag("status", status)
        .timer()
        .count() shouldBe 1L

    val scrape = registry.scrape()
    scrape should include("clearing_transactions_processed_total")
    scrape should include("clearing_processing_duration_seconds_bucket")
    Set("success", "duplicate", "failure").foreach: status =>
      scrape should include(s"status=\"$status\"")
    scrape should not include "txId"
    scrape should not include "offset="

  it should "record an unexpected exception as failure and rethrow it" in:
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val metrics = ClearingMetrics(registry)
    val processor = metrics.instrument(
      new DurableProcessing:
        def process(envelope: RecordEnvelope): DurableRecordOutcome =
          throw new IllegalStateException("boom")
    )

    intercept[IllegalStateException]:
      processor.process(envelope("42"))

    registry
      .find("clearing.transactions.processed")
      .tag("status", "failure")
      .counter()
      .count() shouldBe 1.0
    registry
      .find("clearing.processing.duration")
      .tag("status", "failure")
      .timer()
      .count() shouldBe 1L

  it should "bind the JVM memory gauges used by the dashboard" in:
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    ClearingMetrics(registry)

    registry.find("jvm.memory.used").gauges() should not be empty

  private def envelope(txId: String): RecordEnvelope =
    RecordEnvelope(
      "clearing-input",
      0,
      7L,
      Some("AWB"),
      Map("transaction-id" -> txId),
      "payload",
      Instant.parse("2026-07-14T12:00:00Z")
    )
