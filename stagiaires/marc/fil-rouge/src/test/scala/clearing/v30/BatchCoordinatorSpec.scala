package clearing.v30

import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class BatchCoordinatorSpec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")

  private def record(partition: Int, offset: Long, id: Int): RecordEnvelope =
    RecordEnvelope(
      topic = KafkaSettings.InputTopic,
      partition = partition,
      offset = offset,
      key = Some("ATH"),
      headers = Map("transaction-id" -> id.toString),
      value = s"event-$id",
      occurredAt = instant
    )

  private def decision(id: Int): ProcessingDecision =
    ProcessingDecision.Validated(
      ValidatedEvent(
        transactionId = id,
        sender = "ATH",
        receiver = "BOA",
        settlementAmount = "10.00",
        fee = "0.01",
        currency = "MAD",
        transactionType = "VIR",
        status = "Validated",
        sourceIbanHash = "a" * 64,
        destinationIbanHash = "b" * 64,
        label = "test",
        warnings = Nil,
        occurredAt = instant
      )
    )

  "BatchCoordinator" should "publier puis marquer avant d'autoriser offset + 1" in:
    val actions = collection.mutable.ListBuffer.empty[String]
    val registry = InMemoryDeduplicationRegistry(
      onMark = id => actions += s"mark:$id"
    )
    val publisher = DecisionPublisher: (_, value) =>
      actions += s"publish:${value.transactionId.get}"
      Right(())
    val coordinator = BatchCoordinator(
      envelope => decision(envelope.headers("transaction-id").toInt),
      publisher,
      registry
    )

    val report = coordinator.process(
      List(record(0, 5L, 1), record(0, 6L, 2), record(1, 9L, 3))
    )

    actions.toList shouldBe List(
      "publish:1",
      "mark:1",
      "publish:2",
      "mark:2",
      "publish:3",
      "mark:3"
    )
    report.committableOffsets shouldBe Map(
      InputPartition(0) -> 7L,
      InputPartition(1) -> 10L
    )
    report shouldBe BatchReport(
      committableOffsets = report.committableOffsets,
      published = 3,
      duplicates = 0,
      failedPartitions = Set.empty
    )

  it should "s'arrêter au premier échec d'une partition sans bloquer les autres" in:
    val published = collection.mutable.ListBuffer.empty[Int]
    val publisher = DecisionPublisher: (_, value) =>
      val id = value.transactionId.get
      published += id
      if id == 2 then Left(PublishingFailure("broker indisponible"))
      else Right(())
    val coordinator = BatchCoordinator(
      envelope => decision(envelope.headers("transaction-id").toInt),
      publisher,
      InMemoryDeduplicationRegistry()
    )

    val report = coordinator.process(
      List(
        record(0, 0L, 1),
        record(0, 1L, 2),
        record(0, 2L, 3),
        record(1, 4L, 4)
      )
    )

    published.toList shouldBe List(1, 2, 4)
    report.committableOffsets shouldBe Map(
      InputPartition(0) -> 1L,
      InputPartition(1) -> 5L
    )
    report.published shouldBe 2
    report.failedPartitions shouldBe Set(InputPartition(0))
    report.retryOffsets shouldBe Map(InputPartition(0) -> 1L)

  it should "ne pas republier un doublon marqué mais faire progresser son offset" in:
    val registry = InMemoryDeduplicationRegistry()
    val duplicate = record(2, 12L, 7)
    registry.markProcessed(7, PayloadFingerprint.sha256(duplicate.value))
    var publishCount = 0
    val coordinator = BatchCoordinator(
      _ => decision(7),
      DecisionPublisher: (_, _) =>
        publishCount += 1
        Right(()),
      registry
    )

    val report = coordinator.process(List(duplicate))

    publishCount shouldBe 0
    report.committableOffsets shouldBe Map(InputPartition(2) -> 13L)
    report.duplicates shouldBe 1

  it should "router le même ID avec un autre payload comme conflit DLQ" in:
    val registry = InMemoryDeduplicationRegistry()
    val decisions = collection.mutable.ListBuffer.empty[ProcessingDecision]
    val coordinator = BatchCoordinator(
      _ => decision(7),
      DecisionPublisher: (_, value) =>
        decisions += value
        Right(()),
      registry
    )
    val original = record(0, 0L, 7)
    val conflict = record(0, 1L, 7).copy(value = "payload-modifié")

    coordinator.process(List(original, conflict)).published shouldBe 2
    decisions.toList match
      case List(
            ProcessingDecision.Validated(_),
            ProcessingDecision.Rejected(rejection)
          ) =>
        rejection.code shouldBe "EVENT_ID_CONFLICT"
        rejection.transactionId shouldBe Some(7)
      case other => fail(s"décisions inattendues : $other")

    val replay = coordinator.process(List(conflict))
    replay.duplicates shouldBe 1
    decisions should have size 2
