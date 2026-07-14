package clearing.v12

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class MultilateralNettingSpec extends AnyFlatSpec with Matchers:
  private def transaction(
    id: Int,
    sender: String,
    receiver: String,
    amount: String,
    status: TransactionStatus = TransactionStatus.Validated
  ): Transaction =
    Transaction(
      id,
      sender,
      receiver,
      BigDecimal(amount),
      TransactionType.Transfer,
      status
    )

  "MultilateralNetting.computePositions" should "débiter et créditer en un passage" in:
    val positions = MultilateralNetting.computePositions(
      List(
        transaction(1, "ATH", "CIH", "100"),
        transaction(2, "CIH", "BOA", "60")
      )
    )

    positions shouldBe Map(
      "ATH" -> BigDecimal("-100"),
      "CIH" -> BigDecimal("40"),
      "BOA" -> BigDecimal("60")
    )

  it should "ignorer les statuts non validés" in:
    MultilateralNetting.computePositions(
      List(
        transaction(
          1,
          "ATH",
          "CIH",
          "100",
          TransactionStatus.Rejected
        )
      )
    ) shouldBe empty

  it should "produire le même résultat pour List et Vector" in:
    val transactions = S7TransactionGenerator.generateVector(1000)

    MultilateralNetting.computePositions(transactions.toList) shouldBe
      MultilateralNetting.computePositions(transactions)

  it should "retourner une Map vide pour un flux vide" in:
    MultilateralNetting.computePositions(List.empty) shouldBe empty

  "MultilateralNetting.multilateralNetting" should "nommer les banques avec l'ADT Bank" in:
    val positions = MultilateralNetting.multilateralNetting(
      List(transaction(1, "ATH", "CIH", "75"))
    )

    positions shouldBe Map(
      BankDirectory.fromCode("ATH") -> BigDecimal("-75"),
      BankDirectory.fromCode("CIH") -> BigDecimal("75")
    )

  "MultilateralNetting.isBalanced" should "distinguer un système équilibré d'une perte" in:
    MultilateralNetting.isBalanced(
      Map("ATH" -> BigDecimal("-10"), "CIH" -> BigDecimal("10"))
    ) shouldBe true
    MultilateralNetting.isBalanced(
      Map("ATH" -> BigDecimal("-10"), "CIH" -> BigDecimal("9"))
    ) shouldBe false

  "MultilateralNetting.balanceError" should "produire un log seulement en cas de déséquilibre" in:
    MultilateralNetting.balanceError(
      Map("ATH" -> BigDecimal("-10"), "CIH" -> BigDecimal("10"))
    ) shouldBe None
    MultilateralNetting.balanceError(
      Map("ATH" -> BigDecimal("-10"), "CIH" -> BigDecimal("9"))
    ) shouldBe Some("Déséquilibre du netting : -1 DH")

  "MultilateralNetting.settlementOrder" should "trier les débiteurs puis les créditeurs par importance" in:
    val ordered = MultilateralNetting.settlementOrder(
      Map(
        "CIH" -> BigDecimal("150"),
        "BOA" -> BigDecimal("-50"),
        "ATH" -> BigDecimal("-100"),
        "BMCE" -> BigDecimal(0)
      )
    )

    ordered shouldBe List(
      BankPosition(BankDirectory.fromCode("ATH"), BigDecimal("-100")),
      BankPosition(BankDirectory.fromCode("BOA"), BigDecimal("-50")),
      BankPosition(BankDirectory.fromCode("CIH"), BigDecimal("150"))
    )

  it should "préserver la propriété zéro sur cent lots déterministes" in:
    (1 to 100).foreach: multiplier =>
      val transactions = S7TransactionGenerator.generateVector(
        multiplier * 17
      )
      val positions = MultilateralNetting.computePositions(transactions)

      withClue(s"lot $multiplier : $positions"):
        MultilateralNetting.isBalanced(positions) shouldBe true
