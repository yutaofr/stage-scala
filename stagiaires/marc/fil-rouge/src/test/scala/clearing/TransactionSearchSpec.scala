package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TransactionSearchSpec extends AnyFlatSpec with Matchers:
  private val transactions = List(
    BigDecimal("10"),
    BigDecimal("120"),
    BigDecimal("80"),
    BigDecimal("200")
  )

  "findFirstAbove" should "retourner le premier montant supérieur au seuil" in:
    TransactionSearch.findFirstAbove(
      transactions,
      BigDecimal("100")
    ) shouldBe Some(BigDecimal("120"))

  it should "retourner None si aucun montant ne convient" in:
    TransactionSearch.findFirstAbove(
      transactions,
      BigDecimal("500")
    ) shouldBe None

  "countAbove" should "compter récursivement les montants au-dessus du seuil" in:
    TransactionSearch.countAbove(
      transactions,
      BigDecimal("100")
    ) shouldBe 2

  "sumAbove" should "additionner récursivement les montants au-dessus du seuil" in:
    TransactionSearch.sumAbove(
      transactions,
      BigDecimal("100")
    ) shouldBe BigDecimal("320")

  it should "retourner zéro pour une liste vide" in:
    TransactionSearch.sumAbove(Nil, BigDecimal("100")) shouldBe BigDecimal(0)
