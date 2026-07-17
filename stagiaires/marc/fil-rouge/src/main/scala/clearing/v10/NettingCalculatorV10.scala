package clearing.v10

import clearing.model.Transaction

object NettingCalculatorV10:
  def calculate(
    transactions: List[Transaction]
  ): Map[String, BigDecimal] =
    transactions.foldLeft(Map.empty[String, BigDecimal])(
      (positions, transaction) =>
        val afterDebit = positions.updated(
          transaction.sender,
          positions.getOrElse(transaction.sender, BigDecimal(0)) -
            transaction.amount
        )
        afterDebit.updated(
          transaction.receiver,
          afterDebit.getOrElse(transaction.receiver, BigDecimal(0)) +
            transaction.amount
        )
    )

  def globalNet(positions: Map[String, BigDecimal]): BigDecimal =
    positions.values.foldLeft(BigDecimal(0))(_ + _)
