package clearing.v30

import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CrashReplaySpec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")
  private val envelope = RecordEnvelope(
    KafkaSettings.InputTopic,
    0,
    10L,
    Some("ATH"),
    Map("transaction-id" -> "99"),
    "payload",
    instant
  )
  private val decision = ProcessingDecision.Rejected(
    RejectedEvent(Some(99), "TEST", "rejet", "a" * 64, instant)
  )

  "La fenêtre de crash v3.0" should "ne jamais marquer après une publication échouée" in:
    val registry = InMemoryDeduplicationRegistry()
    val coordinator = BatchCoordinator(
      _ => decision,
      DecisionPublisher((_, _) => Left(PublishingFailure("indisponible"))),
      registry
    )

    val report = coordinator.process(List(envelope))

    registry.contains(99) shouldBe false
    report.committableOffsets shouldBe empty
    report.failedPartitions shouldBe Set(InputPartition(0))

  it should "absorber un replay avant commit tant que le même cache vit" in:
    val registry = InMemoryDeduplicationRegistry()
    var publications = 0
    val coordinator = BatchCoordinator(
      _ => decision,
      DecisionPublisher: (_, _) =>
        publications += 1
        Right(()),
      registry
    )

    val first = coordinator.process(List(envelope))
    val replay = coordinator.process(List(envelope))

    first.committableOffsets shouldBe Map(InputPartition(0) -> 11L)
    replay.committableOffsets shouldBe Map(InputPartition(0) -> 11L)
    replay.duplicates shouldBe 1
    publications shouldBe 1

  it should "republier après un redémarrage qui perd le cache mémoire" in:
    var publications = 0
    def coordinator(registry: DeduplicationRegistry): BatchCoordinator =
      BatchCoordinator(
        _ => decision,
        DecisionPublisher: (_, _) =>
          publications += 1
          Right(()),
        registry
      )

    coordinator(InMemoryDeduplicationRegistry()).process(List(envelope))
    coordinator(InMemoryDeduplicationRegistry()).process(List(envelope))

    publications shouldBe 2

  it should "republier si le processus tombe entre l'ack et le marquage" in:
    final class CrashBetweenAckAndMark extends RuntimeException
    val crashingRegistry = new DeduplicationRegistry:
      def status(
        transactionId: Int,
        payloadFingerprint: String
      ): DeduplicationStatus = DeduplicationStatus.New
      def markProcessed(
        transactionId: Int,
        payloadFingerprint: String
      ): Unit =
        throw new CrashBetweenAckAndMark
    var publications = 0
    def publisher = DecisionPublisher: (_, _) =>
      publications += 1
      Right(())

    an[CrashBetweenAckAndMark] should be thrownBy
      BatchCoordinator(_ => decision, publisher, crashingRegistry)
        .process(List(envelope))

    BatchCoordinator(
      _ => decision,
      publisher,
      InMemoryDeduplicationRegistry()
    ).process(List(envelope))

    publications shouldBe 2
