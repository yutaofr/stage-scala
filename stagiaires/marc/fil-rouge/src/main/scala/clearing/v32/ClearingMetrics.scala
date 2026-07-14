package clearing.v32

import clearing.v30.RecordEnvelope
import clearing.v31.{DurableProcessing, DurableRecordOutcome}
import io.micrometer.core.instrument.{Counter, Timer}
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.time.Duration
import java.util.concurrent.TimeUnit

final class ClearingMetrics(
  val registry: PrometheusMeterRegistry
):
  private val statuses = List("success", "duplicate", "failure")
  private val counters: Map[String, Counter] = statuses.map: status =>
    status -> Counter
      .builder("clearing.transactions.processed")
      .description("Clearing records processed by bounded outcome")
      .tag("status", status)
      .register(registry)
  .toMap
  private val timers: Map[String, Timer] = statuses.map: status =>
    status -> Timer
      .builder("clearing.processing.duration")
      .description("Clearing record processing duration")
      .tag("status", status)
      .publishPercentileHistogram()
      .serviceLevelObjectives(
        Duration.ofMillis(10),
        Duration.ofMillis(50),
        Duration.ofMillis(100),
        Duration.ofMillis(250),
        Duration.ofMillis(500),
        Duration.ofSeconds(1)
      )
      .register(registry)
  .toMap

  new JvmMemoryMetrics().bindTo(registry)

  def instrument(delegate: DurableProcessing): DurableProcessing =
    new DurableProcessing:
      def process(envelope: RecordEnvelope): DurableRecordOutcome =
        val startedAt = System.nanoTime()
        try
          val outcome = delegate.process(envelope)
          record(status(outcome), startedAt)
          outcome
        catch
          case error: Throwable =>
            record("failure", startedAt)
            throw error

  private def record(status: String, startedAt: Long): Unit =
    counters(status).increment()
    timers(status).record(System.nanoTime() - startedAt, TimeUnit.NANOSECONDS)

  private def status(outcome: DurableRecordOutcome): String =
    outcome match
      case DurableRecordOutcome.Published => "success"
      case DurableRecordOutcome.Duplicate => "duplicate"
      case DurableRecordOutcome.Failed(_) => "failure"
