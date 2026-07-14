package clearing.v31

import clearing.v30.*
import java.time.Instant
import java.util.concurrent.{CompletionException, CompletionStage, ExecutionException}
import scala.util.control.NonFatal

enum DurableRecordOutcome:
  case Published
  case Duplicate
  case Failed(reason: String)

trait DurableProcessing:
  def process(envelope: RecordEnvelope): DurableRecordOutcome

trait DurableStageObserver:
  def aroundPersist[A](operation: => A): A

object DurableStageObserver:
  val noop: DurableStageObserver = new DurableStageObserver:
    def aroundPersist[A](operation: => A): A = operation

final class DurableRecordProcessor(
  decide: RecordEnvelope => ProcessingDecision,
  repository: DurableRepository,
  publisher: DecisionPublisher,
  now: () => Instant = () => Instant.now(),
  observer: DurableStageObserver = DurableStageObserver.noop
) extends DurableProcessing:
  def process(envelope: RecordEnvelope): DurableRecordOutcome =
    try processUnsafe(envelope)
    catch
      case NonFatal(error) =>
        DurableRecordOutcome.Failed(rootMessage(error))

  private def processUnsafe(
    envelope: RecordEnvelope
  ): DurableRecordOutcome =
    val original = decide(envelope)
    observer.aroundPersist(persistUnsafe(envelope, original))

  private def persistUnsafe(
    envelope: RecordEnvelope,
    original: ProcessingDecision
  ): DurableRecordOutcome =
    val fingerprint = PayloadFingerprint.sha256(envelope.value)
    val eventKey = original.transactionId
      .map(id => s"tx:$id")
      .getOrElse(
        s"invalid:${envelope.topic}:${envelope.partition}:${envelope.offset}"
      )
    val identity = DurableIdentity(eventKey, fingerprint)
    val states = await(repository.states(eventKey))
    val matching = states.find(_.payloadFingerprint == fingerprint)

    matching match
      case Some(state) if state.stage == ProcessingStage.Completed =>
        DurableRecordOutcome.Duplicate
      case _ =>
        val isConflict =
          matching.exists(_.decisionKind == "conflict") ||
            (matching.isEmpty && original.transactionId.nonEmpty && states.nonEmpty)
        val decision =
          if isConflict then conflictDecision(original, fingerprint, envelope)
          else original
        val kind = if isConflict then "conflict" else decisionKind(decision)

        if matching.isEmpty then
          await(
            repository.markState(
              processingState(
                identity,
                ProcessingStage.Received,
                kind,
                envelope
              )
            )
          )

        val stage = matching.map(_.stage).getOrElse(ProcessingStage.Received)
        if stage == ProcessingStage.Received then
          decision match
            case ProcessingDecision.Validated(event) =>
              await(repository.saveValidated(identity, event))
            case ProcessingDecision.Rejected(_) => ()
          await(
            repository.markState(
              processingState(
                identity,
                ProcessingStage.Projected,
                kind,
                envelope
              )
            )
          )

        publisher.publish(envelope.key, decision) match
          case Left(failure) => DurableRecordOutcome.Failed(failure.reason)
          case Right(_) =>
            await(
              repository.markState(
                processingState(
                  identity,
                  ProcessingStage.Completed,
                  kind,
                  envelope
                )
              )
            )
            DurableRecordOutcome.Published

  private def processingState(
    identity: DurableIdentity,
    stage: ProcessingStage,
    kind: String,
    envelope: RecordEnvelope
  ): ProcessingState =
    ProcessingState(
      identity.eventKey,
      identity.payloadFingerprint,
      stage,
      kind,
      envelope.topic,
      envelope.partition,
      envelope.offset,
      now()
    )

  private def decisionKind(decision: ProcessingDecision): String =
    decision match
      case ProcessingDecision.Validated(_) => "validated"
      case ProcessingDecision.Rejected(_)  => "rejected"

  private def conflictDecision(
    original: ProcessingDecision,
    fingerprint: String,
    envelope: RecordEnvelope
  ): ProcessingDecision =
    ProcessingDecision.Rejected(
      RejectedEvent(
        original.transactionId,
        "EVENT_ID_CONFLICT",
        "identifiant réutilisé avec un payload différent",
        fingerprint,
        envelope.occurredAt
      )
    )

  private def await[A](stage: CompletionStage[A]): A =
    stage.toCompletableFuture.get()

  private def rootMessage(error: Throwable): String =
    val root = error match
      case wrapper: ExecutionException if wrapper.getCause != null =>
        wrapper.getCause
      case wrapper: CompletionException if wrapper.getCause != null =>
        wrapper.getCause
      case other => other
    Option(root.getMessage).getOrElse(root.getClass.getSimpleName)
