package clearing.v13

import clearing.model.*

case class MissingRate(
  currency: Currency,
  transactionIds: List[Int]
)

case class CurrencyConversionResult(
  converted: List[Transaction],
  rejected: List[MissingRate],
  rates: Map[Currency, BigDecimal]
)

object CurrencyConversion:
  def convertToMad(
    transactions: List[Transaction],
    provider: ExchangeRateProvider
  ): CurrencyConversionResult =
    val currencies = transactions.map(_.currency).distinct
    val rates = currencies.flatMap: currency =>
      provider.fetchRate(currency)
        .filter(_ > 0)
        .map(currency -> _)
    .toMap

    val converted = transactions.flatMap: transaction =>
      rates.get(transaction.currency).map: rate =>
        transaction.copy(
          amount = (transaction.amount * rate).setScale(
            2,
            BigDecimal.RoundingMode.HALF_UP
          ),
          currency = Currency.MAD
        )

    val rejected = currencies.filterNot(rates.contains).map: currency =>
      MissingRate(
        currency,
        transactions.collect:
          case transaction if transaction.currency == currency =>
            transaction.id
      )

    CurrencyConversionResult(converted, rejected, rates)
