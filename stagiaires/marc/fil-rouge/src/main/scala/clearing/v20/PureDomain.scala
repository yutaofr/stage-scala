package clearing.v20

import clearing.model.{Currency, TransactionType}

enum PureValidationError:
  case NonPositiveAmount(transactionId: Int)

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
