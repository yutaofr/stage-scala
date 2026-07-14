package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class OptionToolsSpec extends AnyFlatSpec with Matchers:
  "findBankName" should "retourner Some pour une banque connue" in:
    ReferenceData.findBankName("ATH") shouldBe Some("Attijariwafa Bank")

  it should "retourner None pour une banque inconnue" in:
    ReferenceData.findBankName("UNKNOWN") shouldBe None

  "findUserNameByIban" should "chaîner compte et utilisateur avec flatMap" in:
    OptionTools.findUserNameByIban("MA-ATH-001") shouldBe Some("Amina")

  it should "retourner None si l'IBAN est absent" in:
    OptionTools.findUserNameByIban("MA-ABSENT") shouldBe None

  it should "retourner None si l'utilisateur associé est absent" in:
    OptionTools.findUserNameByIban("MA-GHOST") shouldBe None

  "cleanInput" should "supprimer les espaces d'une valeur présente" in:
    OptionTools.cleanInput("  ath  ") shouldBe Some("ath")

  it should "refuser une chaîne vide ou blanche" in:
    OptionTools.cleanInput("") shouldBe None
    OptionTools.cleanInput("   ") shouldBe None

  "cleanUpper" should "transformer l'Option avec map" in:
    OptionTools.cleanUpper("  ath  ") shouldBe Some("ATH")
    OptionTools.cleanUpper("   ") shouldBe None

  "presentAmounts" should "aplatir les valeurs présentes" in:
    val values = List(Some(BigDecimal("10")), None, Some(BigDecimal("2.50")))
    OptionTools.presentAmounts(values) shouldBe
      List(BigDecimal("10"), BigDecimal("2.50"))
    OptionTools.sumPresent(values) shouldBe BigDecimal("12.50")

  "convert" should "utiliser le taux trouvé dans la Map" in:
    OptionTools.convert(BigDecimal("100"), "EUR") shouldBe
      Some(BigDecimal("1085.00"))

  it should "retourner None pour une devise inconnue" in:
    OptionTools.convert(BigDecimal("100"), "JPY") shouldBe None
