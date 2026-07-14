package clearing.v20

import clearing.model.Transaction

object DataCleaner:
  val cleanIban: String => String =
    _.filterNot(_.isWhitespace).toUpperCase

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

  private val trim: String => String = _.trim
  private val wrap: String => String = value => s"[$value]"

  val andThenExample: String => String = trim andThen wrap
  val composeExample: String => String = trim compose wrap
