package clearing.v31

import clearing.v30.*
import java.time.{Instant, LocalDate}
import java.util.concurrent.CompletionStage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

private trait DelegatingDurableRepository extends DurableRepository:
  def delegate: DurableRepository

  def states(eventKey: String) = delegate.states(eventKey)
  def markState(state: ProcessingState) = delegate.markState(state)
  def saveValidated(identity: DurableIdentity, event: ValidatedEvent) =
    delegate.saveValidated(identity, event)
  def movementsByBank(bank: String, date: LocalDate, limit: Int) =
    delegate.movementsByBank(bank, date, limit)
  def positionByBank(bank: String, date: LocalDate) =
    delegate.positionByBank(bank, date)
  def historyByDate(date: LocalDate) = delegate.historyByDate(date)
  def topPairsByDate(date: LocalDate, limit: Int) =
    delegate.topPairsByDate(date, limit)

final class DurableRecordProcessorSpec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")

  private def envelope(value: String = "payload-a"): RecordEnvelope =
    RecordEnvelope(
      KafkaSettings.InputTopic,
      0,
      7L,
      Some("AWB"),
      Map("transaction-id" -> "42"),
      value,
      instant
    )

  private def validated(id: Int = 42): ProcessingDecision =
    ProcessingDecision.Validated(
      ValidatedEvent(
        id,
        "AWB",
        "CIH",
        "100.00",
        "1.00",
        "MAD",
        "TRANSFER",
        "Pending",
        "a" * 64,
        "b" * 64,
        "virement",
        Nil,
        instant
      )
    )

  private def await[A](stage: CompletionStage[A]): A =
    stage.toCompletableFuture.get()

  "DurableRecordProcessor" should "terminer seulement après projections et ack" in:
    val actions = collection.mutable.ListBuffer.empty[String]
    val memory = InMemoryDurableRepository()
    val repository = new DelegatingDurableRepository:
      val delegate = memory
      override def states(eventKey: String) =
        actions += "states"
        delegate.states(eventKey)
      override def markState(state: ProcessingState) =
        actions += s"mark:${state.stage}"
        delegate.markState(state)
      override def saveValidated(
        identity: DurableIdentity,
        event: ValidatedEvent
      ) =
        actions += "project"
        delegate.saveValidated(identity, event)
    val publisher = DecisionPublisher: (_, _) =>
      actions += "publish"
      Right(())

    val result = DurableRecordProcessor(
      _ => validated(),
      repository,
      publisher,
      () => instant
    ).process(envelope())

    result shouldBe DurableRecordOutcome.Published
    actions.toList shouldBe List(
      "states",
      "mark:Received",
      "project",
      "mark:Projected",
      "publish",
      "mark:Completed"
    )
    await(memory.states("tx:42")).map(_.stage) shouldBe
      List(ProcessingStage.Completed)

  it should "absorber durablement le même fingerprint Completed" in:
    val repository = InMemoryDurableRepository()
    var publications = 0
    val processor = DurableRecordProcessor(
      _ => validated(),
      repository,
      DecisionPublisher: (_, _) =>
        publications += 1
        Right(()),
      () => instant
    )

    processor.process(envelope()) shouldBe DurableRecordOutcome.Published
    processor.process(envelope()) shouldBe DurableRecordOutcome.Duplicate
    publications shouldBe 1
    repository.snapshot shouldBe DurableSnapshot(1, 2, 2, 1)

  it should "persister un autre fingerprint du même ID comme conflit DLQ" in:
    val repository = InMemoryDurableRepository()
    val published = collection.mutable.ListBuffer.empty[ProcessingDecision]
    val processor = DurableRecordProcessor(
      _ => validated(),
      repository,
      DecisionPublisher: (_, decision) =>
        published += decision
        Right(()),
      () => instant
    )

    processor.process(envelope("payload-a")) shouldBe
      DurableRecordOutcome.Published
    processor.process(envelope("payload-b")) shouldBe
      DurableRecordOutcome.Published
    processor.process(envelope("payload-b")) shouldBe
      DurableRecordOutcome.Duplicate

    published.toList match
      case List(
            ProcessingDecision.Validated(_),
            ProcessingDecision.Rejected(conflict)
          ) =>
        conflict.code shouldBe "EVENT_ID_CONFLICT"
        conflict.transactionId shouldBe Some(42)
      case other => fail(s"publications inattendues: $other")

    await(repository.states("tx:42")) should have size 2
    repository.snapshot shouldBe DurableSnapshot(1, 2, 2, 1)
