package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CurrencyConverterSpec extends AnyFlatSpec with Matchers:
  "convert" should "convertir 10000 MAD en EUR avec deux décimales" in:
    CurrencyConverter.convert(
      BigDecimal("10000"),
      CurrencyConverter.MAD_TO_EUR
    ) shouldBe BigDecimal("920.00")

  it should "retourner zéro pour un montant nul" in:
    CurrencyConverter.convert(
      BigDecimal("0"),
      CurrencyConverter.MAD_TO_USD
    ) shouldBe BigDecimal("0.00")

  "formatAmount" should "grouper les milliers et afficher la devise" in:
    CurrencyConverter.formatAmount(
      BigDecimal("1500"),
      "EUR"
    ) shouldBe "1 500.00 EUR"
