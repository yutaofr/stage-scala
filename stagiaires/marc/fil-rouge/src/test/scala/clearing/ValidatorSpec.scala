package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ValidatorSpec extends AnyFlatSpec with Matchers:
  private val validIban = "MA" + "1" * 22

  "isPositiveAmount" should "accepter uniquement un montant strictement positif" in:
    Validator.isPositiveAmount(BigDecimal("0.01")) shouldBe true
    Validator.isPositiveAmount(BigDecimal("0")) shouldBe false
    Validator.isPositiveAmount(BigDecimal("-0.01")) shouldBe false

  "isValidIban" should "accepter deux lettres suivies de vingt-deux chiffres" in:
    Validator.isValidIban(validIban) shouldBe true

  it should "refuser une longueur ou un format incorrect" in:
    Validator.isValidIban("MA123") shouldBe false
    Validator.isValidIban("M1" + "1" * 22) shouldBe false

  "isKnownBank" should "utiliser la liste par défaut ou la liste fournie" in:
    Validator.isKnownBank("ATH") shouldBe true
    Validator.isKnownBank("XYZ") shouldBe false
    Validator.isKnownBank("XYZ", List("XYZ")) shouldBe true

  "validateTransaction" should "retourner OK pour une transaction valide" in:
    Validator.validateTransaction(
      validIban,
      "ATH",
      BigDecimal("100")
    ) shouldBe "OK"

  it should "retourner le premier message d'erreur pertinent" in:
    Validator.validateTransaction(validIban, "ATH", BigDecimal("0")) shouldBe
      "Montant invalide"
    Validator.validateTransaction("MA123", "ATH", BigDecimal("100")) shouldBe
      "IBAN invalide"
    Validator.validateTransaction(validIban, "XYZ", BigDecimal("100")) shouldBe
      "Banque inconnue"

  it should "prioriser IBAN puis banque puis montant avec un tuple matché" in:
    Validator.validateTransaction("invalide", "XYZ", BigDecimal("0")) shouldBe
      "IBAN invalide"
