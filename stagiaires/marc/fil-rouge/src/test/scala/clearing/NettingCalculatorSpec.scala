package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class NettingCalculatorSpec extends AnyFlatSpec with Matchers:
  "netBalance" should "retourner zéro pour une liste vide" in:
    NettingCalculator.netBalance(Nil) shouldBe BigDecimal(0)

  it should "retourner la seule valeur d'une liste unitaire" in:
    NettingCalculator.netBalance(List(BigDecimal("42.50"))) shouldBe
      BigDecimal("42.50")

  it should "additionner les crédits et les débits" in:
    NettingCalculator.netBalance(
      List(BigDecimal("100"), BigDecimal("-40"), BigDecimal("15"))
    ) shouldBe BigDecimal("75")

  "countBySign" should "compter les crédits et débits sans compter zéro" in:
    NettingCalculator.countBySign(
      List(BigDecimal("100"), BigDecimal("-40"), BigDecimal("0"))
    ) shouldBe (1, 1)

  "summary" should "inclure les compteurs, le solde et la position" in:
    val summary = NettingCalculator.summary(
      "ATH",
      List(BigDecimal("100"), BigDecimal("-40"))
    )
    summary should include("Résumé ATH")
    summary should include("Nombre de crédits : 1")
    summary should include("Solde net          : 60 DH")
    summary should include("Position           : CRÉDITRICE")
