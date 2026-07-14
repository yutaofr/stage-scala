package clearing.v31

import clearing.v30.ValidatedEvent
import java.time.{Instant, LocalDate}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CassandraRepositoryIntegrationSpec
    extends AnyFlatSpec
    with Matchers:
  private def await[A](stage: java.util.concurrent.CompletionStage[A]): A =
    stage.toCompletableFuture.get()

  "LiveCassandraRepository" should "upserter et relire toutes les projections" in:
    if !sys.env.get("RUN_CASSANDRA_INTEGRATION").contains("true") then
      cancel("activer avec RUN_CASSANDRA_INTEGRATION=true")

    val settings = CassandraSettings.fromEnvironment()
    val session = CassandraSession.open(settings)
    try
      List(
        "processing_state",
        "clearing_history_by_day",
        "transactions_by_bank_day",
        "bank_positions",
        "pair_activity_by_day"
      ).foreach(table => session.execute(s"TRUNCATE $table"))

      val repository = LiveCassandraRepository(
        session,
        CassandraStatements.prepare(session),
        pageSize = 1
      )
      val instant = Instant.parse("2026-07-14T12:30:00Z")
      val date = LocalDate.parse("2026-07-14")
      val identity = DurableIdentity("tx:42", "fp-a")
      val event = ValidatedEvent(
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
        occurredAt = instant
      )
      val completed = ProcessingState(
        "tx:42",
        "fp-a",
        ProcessingStage.Completed,
        "validated",
        "clearing-input",
        0,
        7L,
        instant
      )

      await(repository.markState(completed))
      await(repository.saveValidated(identity, event))
      await(repository.saveValidated(identity, event))
      val second = event.copy(
        transactionId = 43,
        settlementAmount = "40.00",
        occurredAt = instant.plusSeconds(1)
      )
      await(repository.saveValidated(DurableIdentity("tx:43", "fp-b"), second))

      await(repository.states("tx:42")) shouldBe List(completed)
      await(repository.historyByDate(date)).map(_.transactionId) shouldBe
        List(43, 42)
      await(repository.movementsByBank("AWB", date, 10)).map(_.eventKey) shouldBe
        List("tx:43", "tx:42")
      await(repository.positionByBank("AWB", date)) shouldBe
        BankPosition("AWB", date, BigDecimal(-140), 2)
      PositionReconciliation.calculate(
        "AWB",
        date,
        await(repository.movementsByBank("AWB", date, 100))
      ) shouldBe await(repository.positionByBank("AWB", date))
      await(repository.positionByBank("CIH", date)) shouldBe
        BankPosition("CIH", date, BigDecimal(140), 2)
      await(repository.topPairsByDate(date, 10)) shouldBe
        List(PairSummary("AWB|CIH", BigDecimal(140), 2))
    finally session.close()
