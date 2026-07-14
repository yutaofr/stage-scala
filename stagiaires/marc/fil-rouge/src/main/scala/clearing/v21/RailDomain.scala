package clearing.v21

import clearing.model.{Currency, Transaction, TransactionType}

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
