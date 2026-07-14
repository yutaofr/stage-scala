package clearing.v31

import clearing.v30.ValidatedEvent
import java.time.{Instant, LocalDate}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ReportingQueriesSpec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")
  private val date = LocalDate.parse("2026-07-14")

  private def await[A](stage: java.util.concurrent.CompletionStage[A]): A =
    stage.toCompletableFuture.get()

  "Les lectures v3.1" should "départager les événements au même timestamp par eventKey" in:
    val repository = InMemoryDurableRepository()
    await(repository.saveValidated(DurableIdentity("tx:43", "fp-43"), event(43)))
    await(repository.saveValidated(DurableIdentity("tx:42", "fp-42"), event(42)))

    await(repository.movementsByBank("AWB", date, 1)).map(_.eventKey) shouldBe
      List("tx:42")
    await(repository.historyByDate(date)).map(_.eventKey) shouldBe
      List("tx:42", "tx:43")

  it should "agréger une paire sans doubler un replay" in:
    val repository = InMemoryDurableRepository()
    val value = event(42)
    await(repository.saveValidated(DurableIdentity("tx:42", "fp-42"), value))
    await(repository.saveValidated(DurableIdentity("tx:42", "fp-42"), value))

    await(repository.topPairsByDate(date, 10)) shouldBe
      List(PairSummary("AWB|CIH", BigDecimal(10), 1))

  it should "reconcilier la position et dater les données du dashboard" in:
    val repository = InMemoryDurableRepository()
    await(
      repository.saveValidated(
        DurableIdentity("tx:42", "fp-42"),
        event(42)
      )
    )

    val movements = await(repository.movementsByBank("AWB", date, 100))
    PositionReconciliation.calculate("AWB", date, movements) shouldBe
      await(repository.positionByBank("AWB", date))

    val dashboard = await(
      DashboardLoader.load(
        repository,
        List("CIH", "AWB", "AWB"),
        date,
        () => instant.plusSeconds(60)
      )
    )
    dashboard.map(_.bank) shouldBe List("AWB", "CIH")
    all(dashboard.map(_.ageSeconds)) shouldBe 60L

  private def event(id: Int): ValidatedEvent =
    ValidatedEvent(
      id,
      "AWB",
      "CIH",
      "10.00",
      "0.10",
      "MAD",
      "TRANSFER",
      "Pending",
      "a" * 64,
      "b" * 64,
      "test",
      Nil,
      instant
    )
