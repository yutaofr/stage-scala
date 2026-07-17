package clearing.v11

import clearing.model.*

object ErrorQueries:
  def validationMessages(errors: List[ClearingError]): List[String] =
    for case validation: ValidationError <- errors
    yield validation.message

  def businessCodes(errors: List[ClearingError]): List[String] =
    for case business: BusinessError <- errors
    yield business.internalCode

object FeePipeline:
  private val transactions = Map(
    1 -> Transaction(
      1,
      "ATH",
      "CIH",
      BigDecimal("100"),
      TransactionType.Transfer
    )
  )
  private val exchangeRates = Map(
    "MAD" -> BigDecimal("1"),
    "EUR" -> BigDecimal("0.092"),
    "USD" -> BigDecimal("0.099")
  )

  def fetchTransaction(id: Int): Option[Transaction] =
    transactions.get(id)

  def fetchExchangeRate(currency: String): Option[BigDecimal] =
    exchangeRates.get(currency.trim.toUpperCase)

  def calculateFees(amount: BigDecimal): Option[BigDecimal] =
    Option.when(amount >= 0)(
      (amount * BigDecimal("0.01"))
        .setScale(2, BigDecimal.RoundingMode.HALF_UP)
    )

  def finalAmount(id: Int, currency: String): Option[BigDecimal] =
    for
      transaction <- fetchTransaction(id)
      rate <- fetchExchangeRate(currency)
      fees <- calculateFees(transaction.amount)
    yield ((transaction.amount + fees) * rate)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

object CleanTransferPipeline:
  def clean(lines: List[String]): List[Transaction] =
    for
      line <- lines
      transaction <- Transaction.fromCsv(line)
      if transaction.amount > 0
      if transaction.transactionType == TransactionType.Transfer
    yield transaction

object InternationalFeePipeline:
  def adjust(lines: List[String]): List[Transaction] =
    for transaction <- CleanTransferPipeline.clean(lines)
    yield transaction.copy(
      amount = ExchangeFees.amountWithExchangeFee(transaction)
    )

object BatchValidationLab:
  def assessments(
    transactions: List[Transaction]
  ): List[TransactionAssessment] =
    for transaction <- transactions
    yield AdvancedTransactionValidator.assess(transaction)

  def assessmentPairs(
    transactions: List[Transaction]
  ): List[(Transaction, List[ClearingError])] =
    for assessment <- assessments(transactions)
    yield (assessment.transaction, assessment.errors)
