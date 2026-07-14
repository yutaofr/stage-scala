package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class BasicsSpec extends AnyFlatSpec with Matchers:
  "Le solde total" should "être la somme des trois transactions" in:
    Basics.soldeTotal shouldBe
      Basics.transaction1 + Basics.transaction2 + Basics.transaction3

  "position" should "indiquer une position créditrice pour un solde positif" in:
    Basics.position(BigDecimal("10.00")) shouldBe "CRÉDITRICE"

  it should "indiquer une position débitrice pour un solde négatif" in:
    Basics.position(BigDecimal("-0.01")) shouldBe "DÉBITRICE"

  "resume" should "présenter le nombre, le solde et la position" in:
    Basics.resume(List(BigDecimal("100"), BigDecimal("-250"))) shouldBe
      "Nombre de transactions : 2\n" +
        "Solde total : -150 DH\n" +
        "Position : DÉBITRICE"

  "PrecisionDemo" should "montrer la différence entre Double et BigDecimal" in:
    PrecisionDemo.doubleSum should not be 0.3
    PrecisionDemo.decimalSum shouldBe BigDecimal("0.3")
