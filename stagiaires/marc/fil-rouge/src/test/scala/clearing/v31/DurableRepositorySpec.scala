package clearing.v31

import clearing.v30.ValidatedEvent
import java.time.{Instant, LocalDate}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class DurableRepositorySpec extends AnyFlatSpec with Matchers:
  private val occurredAt = Instant.parse("2026-07-14T12:30:00Z")
  private val date = LocalDate.parse("2026-07-14")

  private def await[A](stage: java.util.concurrent.CompletionStage[A]): A =
    stage.toCompletableFuture.get()

  private def state(
    fingerprint: String,
    stage: ProcessingStage
  ): ProcessingState =
    ProcessingState(
      eventKey = "tx:42",
      payloadFingerprint = fingerprint,
      stage = stage,
      decisionKind = "validated",
      topic = "clearing-input",
      partition = 1,
      offset = 17L,
      updatedAt = occurredAt
    )

  private val event = ValidatedEvent(
    transactionId = 42,
    sender = "AWB",
    receiver = "CIH",
    settlementAmount = "100.00",
    fee = "1.00",
    currency = "MAD",
    transactionType = "TRANSFER",
    status = "Pending",
    sourceIbanHash = "a" * 64,
    destinationIbanHash = "b" * 64,
    label = "virement",
    warnings = Nil,
    occurredAt = occurredAt
  )

  "InMemoryDurableRepository" should "conserver plusieurs fingerprints et faire progresser une ligne" in:
    val repository = InMemoryDurableRepository()

    await(repository.markState(state("fp-a", ProcessingStage.Received)))
    await(repository.markState(state("fp-b", ProcessingStage.Projected)))
    await(repository.markState(state("fp-a", ProcessingStage.Completed)))

    await(repository.states("tx:42")).sortBy(_.payloadFingerprint) shouldBe
      List(
        state("fp-a", ProcessingStage.Completed),
        state("fp-b", ProcessingStage.Projected)
      )

  it should "upserter des projections stables lors d'un replay" in:
    val repository = InMemoryDurableRepository()
    val identity = DurableIdentity("tx:42", "fp-a")

    await(repository.saveValidated(identity, event))
    await(repository.saveValidated(identity, event))

    repository.snapshot should matchPattern:
      case DurableSnapshot(1, 2, 2, 1) =>

    await(repository.positionByBank("AWB", date)) shouldBe
      BankPosition("AWB", date, BigDecimal(-100), 1)
    await(repository.positionByBank("CIH", date)) shouldBe
      BankPosition("CIH", date, BigDecimal(100), 1)

  it should "lire les vues prévues sans filtrage transversal" in:
    val repository = InMemoryDurableRepository()
    val second = event.copy(
      transactionId = 43,
      receiver = "BCP",
      settlementAmount = "40.00",
      occurredAt = occurredAt.plusSeconds(1)
    )

    await(repository.saveValidated(DurableIdentity("tx:42", "fp-a"), event))
    await(repository.saveValidated(DurableIdentity("tx:43", "fp-b"), second))

    val history = await(repository.historyByDate(date))
    history.map(_.transactionId) shouldBe List(43, 42)

    val movements = await(repository.movementsByBank("AWB", date, 10))
    movements.map(_.eventKey).toSet shouldBe Set("tx:42", "tx:43")

    await(repository.topPairsByDate(date, 1)) shouldBe List(
      PairSummary("AWB|CIH", BigDecimal(100), 1)
    )

  "HistoryBucket" should "rester dans les seize partitions du schéma" in:
    (1 to 1000).foreach: id =>
      HistoryBucket.forEvent(s"tx:$id") should (be >= 0 and be < 16)
