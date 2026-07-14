package clearing.v21

import clearing.model.{ClearingError, Currency, TechnicalError, Transaction, TransactionType}
import clearing.v20.PreparedTransaction

case class NumberedLine(lineNumber: Int, value: String)

enum LightWarning:
  case MissingLabel(defaultLabel: String)

case class RailTransaction(
  lineNumber: Int,
  transaction: Transaction,
  warnings: List[LightWarning],
  label: Option[String] = None
)

case class V21Config(
  referenceCurrency: Currency,
  knownBanks: Set[String],
  limits: Map[TransactionType, BigDecimal],
  ratesToReference: Map[Currency, BigDecimal],
  feeRates: Map[String, BigDecimal],
  labelsByTransactionId: Map[Int, String]
)

case class RailSuccess(
  lineNumber: Int,
  prepared: PreparedTransaction,
  label: String,
  warnings: List[LightWarning]
)

type HashBoundary = String => Either[TechnicalError, String]

case class LineResult(
  lineNumber: Int,
  result: Either[ClearingError, RailSuccess]
)

case class ErrorStatistics(
  parsing: Int,
  validation: Int,
  business: Int,
  technical: Int,
  warnings: Int
)

case class V21Report(
  referenceCurrency: Currency,
  lineResults: List[LineResult],
  successes: List[RailSuccess],
  errors: List[ClearingError],
  positions: Map[String, BigDecimal],
  feesByBank: Map[String, BigDecimal],
  statistics: ErrorStatistics
)
