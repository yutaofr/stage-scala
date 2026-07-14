package clearing.model

import java.util.Locale
import scala.util.Try

case class Bank(code: String, name: String)

enum TransactionStatus:
  case Pending, Validated, Rejected, Suspicious

enum TransactionType(val code: String):
  case Transfer extends TransactionType("VIR")
  case Withdrawal extends TransactionType("PRE")
  case Check extends TransactionType("CHQ")

object TransactionType:
  def fromCode(raw: String): Option[TransactionType] =
    raw.trim.toUpperCase(Locale.ROOT) match
      case "VIR" => Some(TransactionType.Transfer)
      case "PRE" => Some(TransactionType.Withdrawal)
      case "CHQ" => Some(TransactionType.Check)
      case _     => None

enum Currency:
  case MAD, EUR, USD

object Currency:
  def fromString(raw: String): Option[Currency] =
    raw.trim.toUpperCase(Locale.ROOT) match
      case "MAD" => Some(Currency.MAD)
      case "EUR" => Some(Currency.EUR)
      case "USD" => Some(Currency.USD)
      case _     => None

case class Iban private (value: String)

object Iban:
  def apply(raw: String): Option[Iban] =
    val normalized = raw.trim.toUpperCase(Locale.ROOT)
    Option.when(normalized.length == 24 && normalized.startsWith("MA"))(
      new Iban(normalized)
    )

  def unapply(raw: String): Option[(String, String, String)] =
    apply(raw).map(iban =>
      (
        iban.value.take(2),
        iban.value.slice(4, 9),
        iban.value.drop(9)
      )
    )

case class Transaction(
  id: Int,
  sender: String,
  receiver: String,
  amount: BigDecimal,
  transactionType: TransactionType,
  status: TransactionStatus = TransactionStatus.Pending,
  sourceIban: String = Transaction.DefaultSourceIban,
  destinationIban: String = Transaction.DefaultDestinationIban,
  currency: Currency = Currency.MAD
):
  def isHighValue: Boolean = amount > BigDecimal("50000")

object Transaction:
  val DefaultSourceIban: String = "MA64ATH00000000000000000"
  val DefaultDestinationIban: String = "MA64CIH00000000000000000"

  def fromCsv(line: String): Option[Transaction] =
    line.split(",", -1).map(_.trim) match
      case Array(
            idRaw,
            sender,
            receiver,
            sourceIban,
            destinationIban,
            amountRaw,
            typeRaw,
            currencyRaw
          ) =>
        for
          id <- Try(idRaw.toInt).toOption
          amount <- Try(BigDecimal(amountRaw)).toOption
          transactionType <- TransactionType.fromCode(typeRaw)
          currency <- Currency.fromString(currencyRaw)
        yield Transaction(
          id = id,
          sender = sender.toUpperCase(Locale.ROOT),
          receiver = receiver.toUpperCase(Locale.ROOT),
          amount = amount,
          transactionType = transactionType,
          status = TransactionStatus.Pending,
          sourceIban = sourceIban.toUpperCase(Locale.ROOT),
          destinationIban = destinationIban.toUpperCase(Locale.ROOT),
          currency = currency
        )
      case _ => None

case class Account(iban: String, bank: Bank, balance: BigDecimal)

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
