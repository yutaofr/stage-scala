package observability

import distributed.kafka.RecordEnvelope
import zio.*
import zio.metrics.*

object ClearingMetrics:
  val processed = Metric.counter("clearing_transactions_processed_total")
  val duration = Metric.histogram(
    "clearing_processing_duration_seconds",
    MetricKeyType.Histogram.Boundaries.linear(0.0, 0.1, 25)
  )

  def observe[R, E, A](effect: ZIO[R, E, A]): ZIO[R, E, A] =
    for
      start <- Clock.nanoTime
      result <- effect
        .tapBoth(
          _ => processed.tagged("status", "failure").increment,
          _ => processed.tagged("status", "success").increment
        )
        .ensuring(
          Clock.nanoTime.flatMap { end =>
            duration.update((end - start).toDouble / 1_000_000_000d)
          }
        )
    yield result

object ObservedProcessing:
  def withTransactionContext[R, E, A](
    transactionId: String,
    record: RecordEnvelope
  )(effect: ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO.logAnnotate("txId", transactionId) {
      ZIO.logAnnotate("topic", record.topic) {
        ZIO.logAnnotate("partition", record.partition.toString) {
          ZIO.logAnnotate("offset", record.offset.toString)(effect)
        }
      }
    }

  def process[R, E, A](
    transactionId: String,
    record: RecordEnvelope
  )(durableProcessing: ZIO[R, E, A]): ZIO[R, E, A] =
    withTransactionContext(transactionId, record) {
      ClearingMetrics.observe {
        ZIO.logInfo("transaction.started") *>
          durableProcessing.tapBoth(
            error => ZIO.logError(s"transaction.failed: $error"),
            _ => ZIO.logInfo("transaction.completed")
          )
      }
    }
