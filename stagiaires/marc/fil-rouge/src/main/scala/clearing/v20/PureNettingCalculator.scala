package clearing.v20

import clearing.model.TransactionStatus

object PureNettingCalculator:
  def positions(
    transactions: List[PreparedTransaction]
  ): Map[String, BigDecimal] =
    transactions.iterator
      .filter(_.status == TransactionStatus.Validated)
      .foldLeft(Map.empty[String, BigDecimal]): (positions, transaction) =>
        val afterDebit = positions.updatedWith(transaction.sender):
          case Some(balance) =>
            Some(balance - transaction.settlementAmount)
          case None => Some(-transaction.settlementAmount)

        afterDebit.updatedWith(transaction.receiver):
          case Some(balance) =>
            Some(balance + transaction.settlementAmount)
          case None => Some(transaction.settlementAmount)
