package clearing.core

import clearing.model.*

sealed trait ClearingError extends Product with Serializable:
  def code: String
  def message: String

sealed trait HighLevelError extends ClearingError
sealed trait LineError extends ClearingError

object ClearingError:
  final case class InvalidContract(message: String) extends LineError:
    val code = "INVALID_CONTRACT"

  final case class InvalidAmount(amount: BigDecimal) extends LineError:
    val code = "INVALID_AMOUNT"
    val message = s"Le montant $amount doit être strictement positif"

  final case class UnknownBank(bank: BankCode) extends LineError:
    val code = "UNKNOWN_BANK"
    val message = s"Banque inconnue: ${bank.value}"

  final case class SameBank(bank: BankCode) extends LineError:
    val code = "SAME_BANK"
    val message = s"La banque ${bank.value} ne peut pas être source et destination"

  final case class DuplicateTransaction(id: TransactionId) extends LineError:
    val code = "DUPLICATE_TRANSACTION"
    val message = s"Transaction déjà traitée: ${id.value}"

  final case class InfrastructureFailure(message: String) extends HighLevelError:
    val code = "INFRASTRUCTURE_FAILURE"

final case class TransactionValidator(knownBanks: Set[BankCode]):
  import ClearingError.*

  def validate(tx: Transaction): Either[ClearingError, Transaction] =
    for
      _ <- Either.cond(tx.amount.isPositive, (), InvalidAmount(tx.amount.value))
      _ <- Either.cond(knownBanks.contains(tx.sender), (), UnknownBank(tx.sender))
      _ <- Either.cond(knownBanks.contains(tx.receiver), (), UnknownBank(tx.receiver))
      _ <- Either.cond(tx.sender != tx.receiver, (), SameBank(tx.sender))
    yield tx.copy(status = TransactionStatus.Validated)

object PureNettingCalculator:
  def calculateNetPositions(txs: List[Transaction]): Map[BankCode, Money] =
    txs.foldLeft(Map.empty[BankCode, Money]) { (positions, tx) =>
      val debited = positions.getOrElse(tx.sender, Money.zero) - tx.amount
      val credited = positions.getOrElse(tx.receiver, Money.zero) + tx.amount
      positions.updated(tx.sender, debited).updated(tx.receiver, credited)
    }
