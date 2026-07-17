package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class BankingRecursionSpec extends AnyFlatSpec with Matchers:
  "calculerSoldeHistorique" should "retourner le solde après chaque transaction" in:
    BankingRecursion.calculerSoldeHistorique(
      List(
        BigDecimal("100"),
        BigDecimal("-50"),
        BigDecimal("200"),
        BigDecimal("-100")
      )
    ) shouldBe List(
      BigDecimal("100"),
      BigDecimal("50"),
      BigDecimal("250"),
      BigDecimal("150")
    )

  it should "retourner une liste vide pour une entrée vide" in:
    BankingRecursion.calculerSoldeHistorique(Nil) shouldBe Nil

  it should "traiter cent mille montants sans déborder la pile" in:
    val history = BankingRecursion.calculerSoldeHistorique(
      List.fill(100_000)(BigDecimal(1))
    )
    history should have size 100_000
    history.lastOption shouldBe Some(BigDecimal("100000"))

  "trouverMomentIncident" should "retourner le premier index sous moins cinq cents" in:
    BankingRecursion.trouverMomentIncident(
      List(BigDecimal("-100"), BigDecimal("-401"), BigDecimal("1000")),
      BigDecimal(0)
    ) shouldBe Some(1)

  it should "considérer exactement moins cinq cents comme autorisé" in:
    BankingRecursion.trouverMomentIncident(
      List(BigDecimal("-500")),
      BigDecimal(0)
    ) shouldBe None

  it should "retourner None lorsqu'aucun incident ne survient" in:
    BankingRecursion.trouverMomentIncident(
      List(BigDecimal("100"), BigDecimal("-200"), BigDecimal("50")),
      BigDecimal(0)
    ) shouldBe None

  "signatureExiste" should "s'arrêter sur une signature présente" in:
    BankingRecursion.signatureExiste(
      List("sig-1", "sig-2", "sig-3"),
      "sig-2"
    ) shouldBe true

  it should "retourner faux pour une signature absente" in:
    BankingRecursion.signatureExiste(
      List("sig-1", "sig-2"),
      "sig-9"
    ) shouldBe false

  "fibonacci" should "calculer la suite avec deux accumulateurs" in:
    BankingRecursion.fibonacci(0) shouldBe BigInt(0)
    BankingRecursion.fibonacci(1) shouldBe BigInt(1)
    BankingRecursion.fibonacci(10) shouldBe BigInt(55)

  it should "refuser un index négatif" in:
    an[IllegalArgumentException] should be thrownBy
      BankingRecursion.fibonacci(-1)
