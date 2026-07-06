package observability

import distributed.kafka.RecordEnvelope
import org.slf4j.MDC

object ObservedProcessing:
  def withTransactionContext[A](
    txId: String,
    record: RecordEnvelope
  )(block: => A): A =
    ??? // TODO : Ajouter le txId, le topic, la partition et l'offset dans le MDC SLF4J, exécuter block, puis effacer le MDC dans un bloc finally

  def process[A](
    txId: String,
    record: RecordEnvelope
  )(durableProcessing: => A): A =
    ??? // TODO : Appeler withTransactionContext, logguer le début ("transaction.started"), exécuter durableProcessing, logguer le succès ("transaction.completed") ou logguer l'erreur en cas d'exception et la propager
