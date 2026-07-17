package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class InterestCalculatorSpec extends AnyFlatSpec with Matchers:
  "interetsComposes" should "conserver le capital pour zéro année" in:
    InterestCalculator.interetsComposes(
      BigDecimal("100.00"),
      BigDecimal("0.05"),
      0
    ) shouldBe BigDecimal("100.00")

  it should "capitaliser le taux à chaque période" in:
    InterestCalculator.interetsComposes(
      BigDecimal("100.00"),
      BigDecimal("0.05"),
      2
    ) shouldBe BigDecimal("110.250000")

  it should "refuser un nombre d'années négatif" in:
    an[IllegalArgumentException] should be thrownBy
      InterestCalculator.interetsComposes(
        BigDecimal("100.00"),
        BigDecimal("0.05"),
        -1
      )
