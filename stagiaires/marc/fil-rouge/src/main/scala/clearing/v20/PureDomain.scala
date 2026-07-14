package clearing.v20

import clearing.model.{Currency, Transaction, TransactionType}

case class RawTransaction(lineNumber: Int, value: String)

case class NumberedTransaction(lineNumber: Int, transaction: Transaction)

enum PureValidationError:
  case NonPositiveAmount(transactionId: Int)
  case BusinessRules(transactionId: Int, messages: List[String])
  case LimitExceeded(transactionId: Int, limit: BigDecimal)
  case MissingRate(transactionId: Int, currency: Currency)

case class PureRejection(
  lineNumber: Int,
  transactionId: Option[Int],
  reason: String
)

case class ParsedBatch(
  transactions: List[NumberedTransaction],
  rejections: List[PureRejection]
)

case class ValidatedBatch(
  transactions: List[NumberedTransaction],
  rejections: List[PureRejection]
)

case class PureEngineConfig(
  referenceCurrency: Currency,
  limits: Map[TransactionType, BigDecimal],
  feeRates: Map[String, BigDecimal],
  ratesToReference: Map[Currency, BigDecimal]
)

case class PreparedTransaction(
  id: Int,
  sender: String,
  receiver: String,
  settlementAmount: BigDecimal,
  transactionType: TransactionType,
  status: clearing.model.TransactionStatus,
  referenceCurrency: Currency,
  fee: BigDecimal,
  sourceIbanHash: String,
  destinationIbanHash: String
)

case class PreparedBatch(
  referenceCurrency: Currency,
  transactions: List[PreparedTransaction],
  rejections: List[PureRejection],
  trace: List[String]
)

case class PureClearingReport(
  referenceCurrency: Currency,
  transactions: List[PreparedTransaction],
  rejections: List[PureRejection],
  positions: Map[String, BigDecimal],
  feesByBank: Map[String, BigDecimal],
  trace: List[String]
)
