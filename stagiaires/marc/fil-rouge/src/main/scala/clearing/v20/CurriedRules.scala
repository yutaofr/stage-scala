package clearing.v20

import clearing.model.*

object CurriedRules:
  def checkLimit(
    limit: BigDecimal
  )(
    transaction: Transaction
  ): Boolean =
    transaction.amount < limit

  def applyPreciseFee(
    rate: BigDecimal
  )(
    amount: BigDecimal
  ): BigDecimal =
    (amount * (BigDecimal(1) + rate))
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def applyFee(rate: Double)(amount: BigDecimal): BigDecimal =
    applyPreciseFee(BigDecimal.valueOf(rate))(amount)

  def logWithBank(bankName: String)(message: String): String =
    s"[$bankName] $message"

  def feeFunctions(
    feeRates: Map[String, BigDecimal]
  ): Map[String, BigDecimal => BigDecimal] =
    feeRates.view.mapValues(applyPreciseFee).toMap

object PureEngineProfiles:
  private val commonLimits = Map(
    TransactionType.Transfer -> BigDecimal("1000000"),
    TransactionType.Withdrawal -> BigDecimal("50000"),
    TransactionType.Check -> BigDecimal("200000")
  )

  private val commonFeeRates = Map(
    "ATH" -> BigDecimal("0.0010"),
    "CIH" -> BigDecimal("0.0020"),
    "BOA" -> BigDecimal("0.0015"),
    "BMCE" -> BigDecimal("0.0012"),
    "SGMB" -> BigDecimal("0.0018")
  )

  val clearingMAD: PureEngineConfig = PureEngineConfig(
    referenceCurrency = Currency.MAD,
    limits = commonLimits,
    feeRates = commonFeeRates,
    ratesToReference = Map(
      Currency.MAD -> BigDecimal("1"),
      Currency.EUR -> BigDecimal("10.80"),
      Currency.USD -> BigDecimal("9.90")
    )
  )

  val clearingEUR: PureEngineConfig = PureEngineConfig(
    referenceCurrency = Currency.EUR,
    limits = commonLimits,
    feeRates = commonFeeRates,
    ratesToReference = Map(
      Currency.MAD -> BigDecimal("0.0925925926"),
      Currency.EUR -> BigDecimal("1"),
      Currency.USD -> BigDecimal("0.9166666667")
    )
  )
