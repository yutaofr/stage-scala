package clearing.v20

import clearing.model.{Currency, Transaction}
import clearing.v13.SecurityUtils
import java.util.Locale

object DataCleaner:
  val cleanIban: String => String =
    _.filterNot(_.isWhitespace).toUpperCase(Locale.ROOT)

  val formatAmount: BigDecimal => BigDecimal =
    _.setScale(2, BigDecimal.RoundingMode.HALF_UP)

  val validateStatus
    : Transaction => Either[PureValidationError, Transaction] = transaction =>
    if transaction.amount > 0 then Right(transaction)
    else Left(PureValidationError.NonPositiveAmount(transaction.id))

  val cleanTransaction: Transaction => Transaction = transaction =>
    transaction.copy(
      amount = formatAmount(transaction.amount),
      sourceIban = cleanIban(transaction.sourceIban),
      destinationIban = cleanIban(transaction.destinationIban)
    )

  def anonymize(
    referenceCurrency: Currency,
    settlementAmount: BigDecimal,
    fee: BigDecimal
  )(
    transaction: Transaction
  ): PreparedTransaction =
    PreparedTransaction(
      id = transaction.id,
      sender = transaction.sender,
      receiver = transaction.receiver,
      settlementAmount = formatAmount(settlementAmount),
      transactionType = transaction.transactionType,
      status = transaction.status,
      referenceCurrency = referenceCurrency,
      fee = formatAmount(fee),
      sourceIbanHash = SecurityUtils.hashIban(
        cleanIban(transaction.sourceIban)
      ),
      destinationIbanHash = SecurityUtils.hashIban(
        cleanIban(transaction.destinationIban)
      )
    )

  private val trim: String => String = _.trim
  private val wrap: String => String = value => s"[$value]"

  val andThenExample: String => String = trim andThen wrap
  val composeExample: String => String = trim compose wrap
