package clearing.v23

import clearing.v22.PreparedTransaction
import clearing.v22.DomainTypes.*

object V23Netting:
  def positions(
    transactions: List[PreparedTransaction]
  ): Map[BankCode, Money] =
    transactions.foldLeft(Map.empty[BankCode, Money]):
      case (positions, transaction) =>
        val afterDebit = adjust(
          positions,
          transaction.sender,
          Money.zero - transaction.settlementAmount
        )
        adjust(
          afterDebit,
          transaction.receiver,
          transaction.settlementAmount
        )

  private def adjust(
    positions: Map[BankCode, Money],
    bank: BankCode,
    delta: Money
  ): Map[BankCode, Money] =
    positions.updated(
      bank,
      positions.getOrElse(bank, Money.zero) + delta
    )
