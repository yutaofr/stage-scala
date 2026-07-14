package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class NettingV03Spec extends AnyFlatSpec with Matchers:
  private val transactions: List[TransactionV2.Transaction] = List(
    (1, "ATH", "CIH", BigDecimal("100"), "VIR"),
    (2, "CIH", "ATH", BigDecimal("40"), "VIR"),
    (3, "ATH", "BOA", BigDecimal("50"), "PRE")
  )

  "sumAndCount" should "calculer la somme et le compteur en un passage" in:
    NettingCalculator.sumAndCount(
      List(BigDecimal("1500.50"), BigDecimal("2000"), BigDecimal("500.75"))
    ) shouldBe (BigDecimal("4001.25"), 3)

  "sumWithReduce" should "agréger une liste non vide et sécuriser la liste vide" in:
    NettingCalculator.sumWithReduce(
      List(BigDecimal("10"), BigDecimal("2.50"))
    ) shouldBe Some(BigDecimal("12.50"))
    NettingCalculator.sumWithReduce(Nil) shouldBe None

  "average" should "retourner une moyenne pour une liste non vide" in:
    NettingCalculator.average(
      List(BigDecimal("10"), BigDecimal("20"), BigDecimal("30"))
    ) shouldBe Some(BigDecimal("20"))

  it should "retourner None pour une liste vide" in:
    NettingCalculator.average(Nil) shouldBe None

  "calculate" should "débiter les émetteurs et créditer les bénéficiaires" in:
    NettingCalculator.calculate(transactions) shouldBe Map(
      "ATH" -> BigDecimal("-110"),
      "CIH" -> BigDecimal("60"),
      "BOA" -> BigDecimal("50")
    )

  it should "retourner une Map vide pour une liste vide" in:
    NettingCalculator.calculate(Nil) shouldBe Map.empty

  "statisticsBySender" should "produire max, total et compteur par banque" in:
    NettingCalculator.statisticsBySender(transactions) shouldBe Map(
      "ATH" -> (BigDecimal("100"), BigDecimal("150"), 2),
      "CIH" -> (BigDecimal("40"), BigDecimal("40"), 1)
    )

  "globalNet" should "vérifier l'invariant fondamental de compensation" in:
    NettingCalculator.globalNet(NettingCalculator.calculate(transactions)) shouldBe
      BigDecimal(0)
