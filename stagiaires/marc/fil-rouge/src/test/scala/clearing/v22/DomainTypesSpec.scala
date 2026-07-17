package clearing.v22

import clearing.v22.DomainTypes.*
import java.util.Locale
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class DomainTypesSpec extends AnyFlatSpec with Matchers:
  "BankCode.from" should "normaliser un code bancaire sûr" in:
    BankCode.from(" ath ").map(_.value) shouldBe Right("ATH")
    BankCode.from("sgmb").map(_.value) shouldBe Right("SGMB")

  it should "refuser un code vide, numérique ou trop long" in:
    BankCode.from("") shouldBe Left("code bancaire invalide")
    BankCode.from("A1H") shouldBe Left("code bancaire invalide")
    BankCode.from("ABCDE") shouldBe Left("code bancaire invalide")

  "Iban.from" should "normaliser un IBAN marocain de vingt-quatre caractères" in:
    val result = Iban.from(" ma64ath00000000000000000 ")

    result.map(iban => (iban.value, iban.country, iban.bankSegment)) shouldBe
      Right(("MA64ATH00000000000000000", "MA", "ATH00"))

  it should "refuser le pays, la longueur et les caractères invalides" in:
    Iban.from("FR64ATH00000000000000000") shouldBe Left("IBAN invalide")
    Iban.from("MA64ATH") shouldBe Left("IBAN invalide")
    Iban.from("MA64ATH0000000000000000-") shouldBe Left("IBAN invalide")

  "IbanHash.from" should "accepter seulement un SHA-256 hexadécimal sûr" in:
    val valid = "a" * 64
    val rawIban = "MA64ATH00000000000000000"

    IbanHash.from(valid).map(_.value) shouldBe Right(valid)
    IbanHash.from(rawIban) shouldBe Left("hash IBAN invalide")
    IbanHash.from("A" * 64) shouldBe Left("hash IBAN invalide")
    IbanHash.from("a" * 63) shouldBe Left("hash IBAN invalide")

  "Money" should "préserver la précision et fournir les opérations métier" in:
    val first = Money(BigDecimal("10.125"))
    val second = Money(BigDecimal("2.125"))

    Money.parse(" 10.125 ").map(_.value) shouldBe Right(BigDecimal("10.125"))
    Money.parse("dix") shouldBe Left("montant illisible")
    (first + second).value shouldBe BigDecimal("12.250")
    (first - second).value shouldBe BigDecimal("8.000")
    (first * BigDecimal("2")).value shouldBe BigDecimal("20.250")
    Money(BigDecimal("-3.50")).abs.value shouldBe BigDecimal("3.50")
    first.isPositive shouldBe true
    Money.zero.isPositive shouldBe false

  it should "activer sum et max avec Numeric" in:
    val amounts = List(
      Money(BigDecimal(100)),
      Money(BigDecimal(300)),
      Money(BigDecimal(200))
    )

    amounts.sum.value shouldBe BigDecimal(600)
    amounts.max.value shouldBe BigDecimal(300)

  it should "formater les milliers sans dépendre de la locale JVM" in:
    val previous = Locale.getDefault

    try
      Locale.setDefault(Locale.forLanguageTag("tr"))
      Money(BigDecimal("5000")).format shouldBe "5 000.00 DH"
      Money(BigDecimal("-1234567.895")).format shouldBe
        "-1 234 567.90 DH"
    finally Locale.setDefault(previous)

  "unsafe" should "échouer immédiatement sur une constante invalide" in:
    an[IllegalArgumentException] should be thrownBy BankCode.unsafe("A1")
    an[IllegalArgumentException] should be thrownBy Iban.unsafe("MA64")
