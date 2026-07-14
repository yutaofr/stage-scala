package clearing.v12

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class FlowSegmentationSpec extends AnyFlatSpec with Matchers:
  private def transaction(
    id: Int,
    amount: String,
    status: TransactionStatus = TransactionStatus.Validated
  ): Transaction =
    Transaction(
      id,
      "ATH",
      "CIH",
      BigDecimal(amount),
      TransactionType.Transfer,
      status
    )

  "FlowSegmentation.partitionByStatus" should "séparer les transactions validées des autres en un passage" in:
    val transactions = List(
      transaction(1, "10"),
      transaction(2, "20", TransactionStatus.Rejected),
      transaction(3, "30", TransactionStatus.Suspicious)
    )
    val (validated, others) =
      FlowSegmentation.partitionByStatus(transactions)

    validated.map(_.id) shouldBe List(1)
    others.map(_.id) shouldBe List(2, 3)

  it should "calculer les deux montants de la partition" in:
    FlowSegmentation.partitionSummary(
      List(
        transaction(1, "10"),
        transaction(2, "20", TransactionStatus.Rejected),
        transaction(3, "30", TransactionStatus.Suspicious)
      )
    ) shouldBe PartitionSummary(
      validatedCount = 1,
      otherCount = 2,
      validatedAmount = BigDecimal("10"),
      otherAmount = BigDecimal("50")
    )

  "FlowSegmentation.processBatches" should "découper par dix et conserver le dernier batch incomplet" in:
    val batches = FlowSegmentation.processBatches(
      S7TransactionGenerator.generateVector(23).toList,
      size = 10
    )

    batches.map(_.number) shouldBe List(1, 2, 3)
    batches.map(_.transactionCount) shouldBe List(10, 10, 3)
    all(batches.map(batch =>
      MultilateralNetting.isBalanced(batch.positions)
    )) shouldBe true

  it should "utiliser mille comme taille par défaut" in:
    FlowSegmentation.processBatches(
      S7TransactionGenerator.generateVector(1001).toList
    ).map(_.transactionCount) shouldBe List(1000, 1)

  it should "refuser une taille de batch non positive" in:
    an[IllegalArgumentException] should be thrownBy:
      FlowSegmentation.processBatches(Nil, size = 0)

  "FlowSegmentation.mergePositions" should "reproduire le netting du flux complet" in:
    val transactions = S7TransactionGenerator.generateVector(235).toList
    val batches = FlowSegmentation.processBatches(transactions, size = 10)

    FlowSegmentation.mergePositions(batches) shouldBe
      MultilateralNetting.computePositions(transactions)

  "FlowSegmentation.fraudWindows" should "trier par ID et signaler seulement les fenêtres au-dessus du seuil" in:
    val transactions = List(
      transaction(6, "1"),
      transaction(5, "100001"),
      transaction(4, "100000"),
      transaction(3, "100000"),
      transaction(2, "100000"),
      transaction(1, "100000")
    )

    FlowSegmentation.fraudWindows(transactions) shouldBe List(
      FraudWindowAlert(
        ids = List(1, 2, 3, 4, 5),
        totalAmount = BigDecimal("500001")
      )
    )

  it should "ne pas alerter au seuil exact" in:
    FlowSegmentation.fraudWindows(
      (1 to 5).toList.map(id => transaction(id, "100000"))
    ) shouldBe empty

  it should "refuser une taille de fenêtre non positive" in:
    an[IllegalArgumentException] should be thrownBy:
      FlowSegmentation.fraudWindows(Nil, size = -1)
