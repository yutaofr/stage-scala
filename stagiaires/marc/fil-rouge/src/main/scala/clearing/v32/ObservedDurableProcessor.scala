package clearing.v32

import clearing.v30.RecordEnvelope
import clearing.v31.{DurableProcessing, DurableRecordOutcome}
import org.slf4j.{Logger, LoggerFactory, MDC}

final class ObservedDurableProcessor(
  delegate: DurableProcessing,
  logger: Logger = LoggerFactory.getLogger(classOf[ObservedDurableProcessor])
) extends DurableProcessing:
  def process(envelope: RecordEnvelope): DurableRecordOutcome =
    val previous = Option(MDC.getCopyOfContextMap)
    setCorrelation(envelope)
    try
      logger.info("clearing record started")
      val outcome = delegate.process(envelope)
      logOutcome(outcome)
      outcome
    catch
      case error: Throwable =>
        logger.error("clearing record failed unexpectedly", error)
        throw error
    finally restore(previous)

  private def setCorrelation(envelope: RecordEnvelope): Unit =
    MDC.put("txId", CorrelationFields.transactionId(envelope))
    MDC.put("topic", envelope.topic)
    MDC.put("partition", envelope.partition.toString)
    MDC.put("offset", envelope.offset.toString)

  private def logOutcome(outcome: DurableRecordOutcome): Unit =
    outcome match
      case DurableRecordOutcome.Published =>
        logger.info("clearing record completed status=success")
      case DurableRecordOutcome.Duplicate =>
        logger.info("clearing record completed status=duplicate")
      case DurableRecordOutcome.Failed(_) =>
        logger.warn("clearing record completed status=failure")

  private def restore(previous: Option[java.util.Map[String, String]]): Unit =
    previous match
      case Some(values) => MDC.setContextMap(values)
      case None         => MDC.clear()

private[v32] object CorrelationFields:
  def transactionId(envelope: RecordEnvelope): String =
    envelope.headers
      .get("transaction-id")
      .flatMap(_.toIntOption)
      .filter(_ > 0)
      .map(_.toString)
      .getOrElse("unknown")
