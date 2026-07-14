package clearing.v13

import clearing.model.*
import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CurrencyConversionSpec extends AnyFlatSpec with Matchers:
  private final class CountingProvider(
    available: Map[Currency, BigDecimal]
  ) extends ExchangeRateProvider:
    private val madCalls = AtomicInteger(0)
    private val eurCalls = AtomicInteger(0)
    private val usdCalls = AtomicInteger(0)

    private def counter(currency: Currency): AtomicInteger = currency match
      case Currency.MAD => madCalls
      case Currency.EUR => eurCalls
      case Currency.USD => usdCalls

    def fetchRate(currency: Currency): Option[BigDecimal] =
      counter(currency).incrementAndGet()
      available.get(currency)

    def callCount(currency: Currency): Int = counter(currency).get()

  private def transaction(
    id: Int,
    amount: String,
    currency: Currency,
    status: TransactionStatus = TransactionStatus.Validated
  ): Transaction =
    Transaction(
      id = id,
      sender = "ATH",
      receiver = "CIH",
      amount = BigDecimal(amount),
      transactionType = TransactionType.Transfer,
      status = status,
      currency = currency
    )

  "CurrencyConversion.convertToMad" should "mettre en cache un taux par devise et préserver l'ordre" in:
    val provider = CountingProvider(
      Map(
        Currency.MAD -> BigDecimal("1"),
        Currency.EUR -> BigDecimal("10.80"),
        Currency.USD -> BigDecimal("9.90")
      )
    )
    val transactions = List(
      transaction(1, "100", Currency.MAD),
      transaction(2, "1.234", Currency.EUR),
      transaction(3, "2", Currency.EUR, TransactionStatus.Suspicious),
      transaction(4, "5", Currency.USD)
    )

    val result = CurrencyConversion.convertToMad(transactions, provider)

    result.converted.map(_.id) shouldBe List(1, 2, 3, 4)
    result.converted.map(_.amount) shouldBe List(
      BigDecimal("100.00"),
      BigDecimal("13.33"),
      BigDecimal("21.60"),
      BigDecimal("49.50")
    )
    result.converted.map(_.currency).distinct shouldBe List(Currency.MAD)
    result.converted.map(_.status) shouldBe transactions.map(_.status)
    result.rejected shouldBe empty
    result.rates shouldBe Map(
      Currency.MAD -> BigDecimal("1"),
      Currency.EUR -> BigDecimal("10.80"),
      Currency.USD -> BigDecimal("9.90")
    )
    provider.callCount(Currency.MAD) shouldBe 1
    provider.callCount(Currency.EUR) shouldBe 1
    provider.callCount(Currency.USD) shouldBe 1

  it should "rejeter uniquement les IDs de la devise sans taux" in:
    val provider = CountingProvider(
      Map(
        Currency.MAD -> BigDecimal("1"),
        Currency.EUR -> BigDecimal("10.80")
      )
    )
    val transactions = List(
      transaction(1, "100", Currency.MAD),
      transaction(2, "10", Currency.EUR),
      transaction(3, "20", Currency.USD),
      transaction(4, "30", Currency.USD)
    )

    val result = CurrencyConversion.convertToMad(transactions, provider)

    result.converted.map(_.id) shouldBe List(1, 2)
    result.rejected shouldBe List(
      MissingRate(Currency.USD, List(3, 4))
    )
    result.rates.keySet shouldBe Set(Currency.MAD, Currency.EUR)
    provider.callCount(Currency.USD) shouldBe 1

  it should "isoler les taux nuls ou négatifs comme des taux invalides" in:
    val provider = CountingProvider(
      Map(
        Currency.MAD -> BigDecimal("1"),
        Currency.EUR -> BigDecimal("0"),
        Currency.USD -> BigDecimal("-2")
      )
    )
    val transactions = List(
      transaction(1, "100", Currency.MAD),
      transaction(2, "10", Currency.EUR),
      transaction(3, "20", Currency.USD)
    )

    val result = CurrencyConversion.convertToMad(transactions, provider)

    result.converted.map(_.id) shouldBe List(1)
    result.rejected shouldBe List(
      MissingRate(Currency.EUR, List(2)),
      MissingRate(Currency.USD, List(3))
    )
    result.rates shouldBe Map(Currency.MAD -> BigDecimal("1"))
    provider.callCount(Currency.EUR) shouldBe 1
    provider.callCount(Currency.USD) shouldBe 1

  it should "ne faire aucun appel pour un batch vide" in:
    val provider = CountingProvider(Map.empty)

    CurrencyConversion.convertToMad(Nil, provider) shouldBe
      CurrencyConversionResult(Nil, Nil, Map.empty)
    provider.callCount(Currency.MAD) shouldBe 0
    provider.callCount(Currency.EUR) shouldBe 0
    provider.callCount(Currency.USD) shouldBe 0
