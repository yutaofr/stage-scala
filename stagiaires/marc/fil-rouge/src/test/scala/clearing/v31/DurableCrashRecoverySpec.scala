package clearing.v31

import clearing.v30.*
import java.time.Instant
import java.util.concurrent.CompletableFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class DurableCrashRecoverySpec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")
  private val record = RecordEnvelope(
    KafkaSettings.InputTopic,
    0,
    10L,
    Some("AWB"),
    Map("transaction-id" -> "99"),
    "payload",
    instant
  )
  private val decision = ProcessingDecision.Validated(
    ValidatedEvent(
      99,
      "AWB",
      "CIH",
      "20.00",
      "0.20",
      "MAD",
      "TRANSFER",
      "Pending",
      "a" * 64,
      "b" * 64,
      "test",
      Nil,
      instant
    )
  )

  private def await[A](stage: java.util.concurrent.CompletionStage[A]): A =
    stage.toCompletableFuture.get()

  "La reprise durable v3.1" should "repartir de Received après une projection échouée" in:
    val memory = InMemoryDurableRepository()
    var failOnce = true
    val repository = new DelegatingDurableRepository:
      val delegate = memory
      override def saveValidated(
        identity: DurableIdentity,
        event: ValidatedEvent
      ) =
        if failOnce then
          failOnce = false
          CompletableFuture.failedFuture[Unit](
            new RuntimeException("Cassandra indisponible")
          )
        else delegate.saveValidated(identity, event)
    var publications = 0
    val processor = DurableRecordProcessor(
      _ => decision,
      repository,
      DecisionPublisher: (_, _) =>
        publications += 1
        Right(()),
      () => instant
    )

    processor.process(record) should matchPattern:
      case DurableRecordOutcome.Failed(_) =>
    await(memory.states("tx:99")).map(_.stage) shouldBe
      List(ProcessingStage.Received)

    processor.process(record) shouldBe DurableRecordOutcome.Published
    publications shouldBe 1
    memory.snapshot shouldBe DurableSnapshot(1, 2, 2, 1)

  it should "reprendre Projected sans réécrire les projections" in:
    val memory = InMemoryDurableRepository()
    var failPublishOnce = true
    val processor = DurableRecordProcessor(
      _ => decision,
      memory,
      DecisionPublisher: (_, _) =>
        if failPublishOnce then
          failPublishOnce = false
          Left(PublishingFailure("Kafka indisponible"))
        else Right(()),
      () => instant
    )

    processor.process(record) should matchPattern:
      case DurableRecordOutcome.Failed(_) =>
    await(memory.states("tx:99")).map(_.stage) shouldBe
      List(ProcessingStage.Projected)
    val before = memory.snapshot

    processor.process(record) shouldBe DurableRecordOutcome.Published
    memory.snapshot shouldBe before

  it should "republier si le crash suit l'ack mais précède Completed" in:
    val memory = InMemoryDurableRepository()
    var failCompletedOnce = true
    val repository = new DelegatingDurableRepository:
      val delegate = memory
      override def markState(state: ProcessingState) =
        if state.stage == ProcessingStage.Completed && failCompletedOnce then
          failCompletedOnce = false
          CompletableFuture.failedFuture[Unit](
            new RuntimeException("crash avant Completed")
          )
        else delegate.markState(state)
    var publications = 0
    val processor = DurableRecordProcessor(
      _ => decision,
      repository,
      DecisionPublisher: (_, _) =>
        publications += 1
        Right(()),
      () => instant
    )

    processor.process(record) should matchPattern:
      case DurableRecordOutcome.Failed(_) =>
    processor.process(record) shouldBe DurableRecordOutcome.Published
    publications shouldBe 2
    await(memory.states("tx:99")).map(_.stage) shouldBe
      List(ProcessingStage.Completed)

  "DurableBatchCoordinator" should "bloquer seulement la partition échouée" in:
    val memory = InMemoryDurableRepository()
    val processor = DurableRecordProcessor(
      envelope => decision.copyValidatedId(envelope.headers("transaction-id").toInt),
      memory,
      DecisionPublisher: (_, value) =>
        if value.transactionId.contains(2) then
          Left(PublishingFailure("panne"))
        else Right(()),
      () => instant
    )
    val records = List(
      input(0, 0L, 1),
      input(0, 1L, 2),
      input(0, 2L, 3),
      input(1, 4L, 4)
    )

    val report = DurableBatchCoordinator(processor).process(records)

    report.committableOffsets shouldBe Map(
      InputPartition(0) -> 1L,
      InputPartition(1) -> 5L
    )
    report.published shouldBe 2
    report.failedPartitions shouldBe Set(InputPartition(0))
    report.retryOffsets shouldBe Map(InputPartition(0) -> 1L)

  private def input(partition: Int, offset: Long, id: Int): RecordEnvelope =
    record.copy(
      partition = partition,
      offset = offset,
      headers = Map("transaction-id" -> id.toString),
      value = s"payload-$id"
    )

extension (value: ProcessingDecision)
  private def copyValidatedId(id: Int): ProcessingDecision = value match
    case ProcessingDecision.Validated(event) =>
      ProcessingDecision.Validated(event.copy(transactionId = id))
    case rejected => rejected
