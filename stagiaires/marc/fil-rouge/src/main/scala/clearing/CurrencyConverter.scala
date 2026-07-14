package clearing

import java.util.Locale

object CurrencyConverter:
  val MAD_TO_EUR: BigDecimal = BigDecimal("0.092")
  val MAD_TO_USD: BigDecimal = BigDecimal("0.099")

  def convert(amount: BigDecimal, rate: BigDecimal): BigDecimal =
    (amount * rate).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def formatAmount(amount: BigDecimal, currency: String): String =
    val formatted = String
      .format(Locale.ROOT, "%,.2f", amount.bigDecimal)
      .replace(',', ' ')
    s"$formatted $currency"

@main def runCurrencyDemo(): Unit =
  val montantMAD = BigDecimal("15000.00")
  println(
    CurrencyConverter.formatAmount(
      CurrencyConverter.convert(montantMAD, CurrencyConverter.MAD_TO_EUR),
      "EUR"
    )
  )
  println(
    CurrencyConverter.formatAmount(
      CurrencyConverter.convert(montantMAD, CurrencyConverter.MAD_TO_USD),
      "USD"
    )
  )
