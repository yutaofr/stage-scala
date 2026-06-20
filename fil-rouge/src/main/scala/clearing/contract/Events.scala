package clearing.contract

import clearing.core.*
import clearing.core.ClearingError.InvalidContract
import clearing.model.*
import zio.json.*

final case class TransactionSubmittedV1(
  id: String,
  sender: String,
  receiver: String,
  amount: BigDecimal,
  status: String = "Pending",
  transactionType: String = "Transfer"
) derives JsonCodec:

  def toDomain(knownBanks: Set[BankCode]): Either[ClearingError, Transaction] =
    for
      txId <- TransactionId.from(id).left.map(InvalidContract.apply)
      from <- BankCode.from(sender).left.map(InvalidContract.apply)
      to <- BankCode.from(receiver).left.map(InvalidContract.apply)
      money <- Money.positive(amount).left.map(InvalidContract.apply)
      parsedStatus <- parseStatus(status)
      parsedType <- parseType(transactionType)
      tx = Transaction(txId, from, to, money, parsedStatus, parsedType)
      valid <- TransactionValidator(knownBanks).validate(tx)
    yield valid

  private def parseStatus(value: String): Either[ClearingError, TransactionStatus] =
    TransactionStatus.values
      .find(_.toString.equalsIgnoreCase(value))
      .toRight(InvalidContract(s"statut inconnu: $value"))

  private def parseType(value: String): Either[ClearingError, TransactionType] =
    TransactionType.values
      .find(_.toString.equalsIgnoreCase(value))
      .toRight(InvalidContract(s"type de transaction inconnu: $value"))

object TransactionSubmittedV1:
  def fromDomain(tx: Transaction): TransactionSubmittedV1 =
    TransactionSubmittedV1(
      id = tx.id.value,
      sender = tx.sender.value,
      receiver = tx.receiver.value,
      amount = tx.amount.value,
      status = tx.status.toString,
      transactionType = tx.transactionType.toString
    )

final case class TransactionValidatedV1(
  id: String,
  sender: String,
  receiver: String,
  amount: BigDecimal,
  status: String = "Validated"
) derives JsonCodec

object TransactionValidatedV1:
  def fromDomain(tx: Transaction): TransactionValidatedV1 =
    TransactionValidatedV1(
      tx.id.value,
      tx.sender.value,
      tx.receiver.value,
      tx.amount.value
    )

final case class TransactionRejectedV1(
  id: Option[String],
  errorCode: String,
  message: String,
  originalPayload: String
) derives JsonCodec

object ContractCodec:
  def encode(event: TransactionSubmittedV1): String = event.toJson

  def decode(payload: String): Either[String, TransactionSubmittedV1] =
    payload.fromJson[TransactionSubmittedV1]
