package clearing.model

case class Bank(code: String, name: String)

enum TransactionStatus:
  case Pending, Validated, Rejected, Suspicious

enum TransactionType(val code: String):
  case Transfer extends TransactionType("VIR")
  case Withdrawal extends TransactionType("PRE")
  case Check extends TransactionType("CHQ")

object TransactionType:
  def fromCode(raw: String): Option[TransactionType] =
    raw.trim.toUpperCase match
      case "VIR" => Some(TransactionType.Transfer)
      case "PRE" => Some(TransactionType.Withdrawal)
      case "CHQ" => Some(TransactionType.Check)
      case _     => None

case class Transaction(
  id: Int,
  sender: String,
  receiver: String,
  amount: BigDecimal,
  transactionType: TransactionType,
  status: TransactionStatus = TransactionStatus.Pending
):
  def isHighValue: Boolean = amount > BigDecimal("50000")

case class Account(iban: String, bank: Bank, balance: BigDecimal)

sealed trait ClearingError

case class InvalidAmount(amount: BigDecimal) extends ClearingError

case class UnknownBank(code: String) extends ClearingError

case object DuplicateTransaction extends ClearingError

case class ValidationError(field: String, message: String)
    extends ClearingError

case class InvalidTransaction(
  transaction: Transaction,
  errors: List[ClearingError]
)

case class ValidationSummary(
  valid: List[Transaction],
  invalid: List[InvalidTransaction]
)

case class ClearingBatch(
  id: Int,
  transactions: List[Transaction],
  status: TransactionStatus
):
  def totalAmount: BigDecimal =
    transactions.foldLeft(BigDecimal(0))((total, transaction) =>
      total + transaction.amount
    )

case class ClearingResult(
  batch: ClearingBatch,
  netPositions: Map[String, BigDecimal],
  errors: List[ClearingError]
)

case class AppResult(
  result: ClearingResult,
  invalidTransactions: List[InvalidTransaction],
  malformedCount: Int,
  receivedCount: Int
)
