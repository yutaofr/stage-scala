package observability

import io.micrometer.core.instrument.{Metrics, Timer}

object ClearingMetrics:
  val processedSuccess = Metrics.counter("clearing_transactions_processed_total", "status", "success")
  val processedFailure = Metrics.counter("clearing_transactions_processed_total", "status", "failure")
  val timer = Timer.builder("clearing_processing_duration_seconds")
    .description("Temps passé dans la logique de netting")
    .register(Metrics.globalRegistry)

  def observe[A](block: => A): A =
    ??? // TODO : Démarrer un Timer sample, exécuter block, incrémenter processedSuccess ou processedFailure en cas d'exception, arrêter le Timer et retourner/propager le résultat
