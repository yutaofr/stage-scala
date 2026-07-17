package clearing.v10

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class BatchProcessorSpec extends AnyFlatSpec with Matchers:
  private val validTransactions = List(
    Transaction(1, "ATH", "CIH", BigDecimal("100"), TransactionType.Transfer),
    Transaction(2, "CIH", "BOA", BigDecimal("40"), TransactionType.Withdrawal),
    Transaction(3, "BOA", "ATH", BigDecimal("20"), TransactionType.Check)
  )

  "NettingCalculatorV10" should "débiter et créditer les banques avec des champs nommés" in:
    val positions = NettingCalculatorV10.calculate(validTransactions)

    positions shouldBe Map(
      "ATH" -> BigDecimal("-80"),
      "CIH" -> BigDecimal("60"),
      "BOA" -> BigDecimal("20")
    )
    NettingCalculatorV10.globalNet(positions) shouldBe BigDecimal(0)

  "BatchProcessor.processBatch" should "calculer seulement les transactions valides et conserver les erreurs" in:
    val invalid = validTransactions.head.copy(
      id = 4,
      sender = "ZZZ",
      amount = BigDecimal("-5")
    )
    val batch = ClearingBatch(
      1,
      validTransactions :+ invalid,
      TransactionStatus.Pending
    )

    val result = BatchProcessor.processBatch(batch)

    result.batch shouldBe batch
    result.netPositions shouldBe NettingCalculatorV10.calculate(validTransactions)
    result.errors shouldBe List(
      InvalidAmount(BigDecimal("-5")),
      UnknownBank("ZZZ")
    )

  "BatchProcessor.processValidated" should "réutiliser un résumé sans relancer les règles" in:
    val acceptedByCaller = validTransactions.head.copy(sender = "NEW")
    val batch = ClearingBatch(
      5,
      List(acceptedByCaller),
      TransactionStatus.Pending
    )
    val validation = ValidationSummary(List(acceptedByCaller), Nil)

    val result = BatchProcessor.processValidated(batch, validation)

    result.errors shouldBe empty
    result.netPositions shouldBe Map(
      "NEW" -> BigDecimal("-100"),
      "CIH" -> BigDecimal("100")
    )

  "BatchProcessor.describeResult" should "décrire un succès total" in:
    val batch = ClearingBatch(
      2,
      validTransactions,
      TransactionStatus.Validated
    )
    BatchProcessor.describeResult(BatchProcessor.processBatch(batch)) shouldBe
      "Succès total"

  it should "compter les erreurs d'un échec partiel" in:
    val batch = ClearingBatch(
      3,
      validTransactions :+ validTransactions.head.copy(id = 4, amount = BigDecimal("-1")),
      TransactionStatus.Pending
    )
    BatchProcessor.describeResult(BatchProcessor.processBatch(batch)) shouldBe
      "Échec partiel (1 erreurs)"

  it should "donner la priorité au statut rejeté" in:
    val rejected = ClearingResult(
      ClearingBatch(4, validTransactions, TransactionStatus.Rejected),
      Map.empty,
      Nil
    )
    BatchProcessor.describeResult(rejected) shouldBe "Batch Invalide"
