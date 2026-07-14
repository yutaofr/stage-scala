package clearing.v22

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.DomainTypes.*

case class Bank(code: BankCode, name: String)

case class Transaction(
  id: Int,
  sender: BankCode,
  receiver: BankCode,
  sourceIban: Iban,
  destinationIban: Iban,
  amount: Money,
  transactionType: TransactionType,
  currency: Currency,
  status: TransactionStatus = TransactionStatus.Pending
)

case class NumberedLine(lineNumber: Int, value: String)

case class TypedTransactionLine(
  lineNumber: Int,
  transaction: Transaction,
  label: Option[String],
  warnings: List[V22Warning]
)

enum V22Warning:
  case MissingLabel(defaultLabel: String)

case class PreparedTransaction(
  id: Int,
  sender: BankCode,
  receiver: BankCode,
  settlementAmount: Money,
  transactionType: TransactionType,
  status: TransactionStatus,
  referenceCurrency: Currency,
  fee: Money,
  sourceIbanHash: String,
  destinationIbanHash: String,
  label: String,
  warnings: List[V22Warning]
)

case class Rejection(
  lineNumber: Int,
  transactionId: Option[Int],
  code: String,
  message: String
)

case class ErrorStatistics(
  parsing: Int,
  validation: Int,
  business: Int,
  technical: Int,
  warnings: Int
)

case class ClearingResult(
  referenceCurrency: Currency,
  transactions: List[PreparedTransaction],
  rejections: List[Rejection],
  positions: Map[BankCode, Money],
  feesByBank: Map[BankCode, Money],
  statistics: ErrorStatistics
)

case class V22Config(
  referenceCurrency: Currency,
  knownBanks: Set[BankCode],
  limits: Map[TransactionType, Money],
  ratesToReference: Map[Currency, BigDecimal],
  feeRates: Map[BankCode, BigDecimal],
  labelsByTransactionId: Map[Int, String]
)

type HashBoundary = Iban => Either[V22Error, String]
