package clearing.v12

import clearing.model.*

object MultilateralNetting:
  private def computeFromIterator(
    transactions: Iterator[Transaction]
  ): Map[String, BigDecimal] =
    transactions
      .filter(_.status == TransactionStatus.Validated)
      .foldLeft(Map.empty[String, BigDecimal]): (positions, transaction) =>
        val afterDebit = positions.updatedWith(transaction.sender):
          case Some(balance) => Some(balance - transaction.amount)
          case None          => Some(-transaction.amount)

        afterDebit.updatedWith(transaction.receiver):
          case Some(balance) => Some(balance + transaction.amount)
          case None          => Some(transaction.amount)

  def computePositions(
    transactions: List[Transaction]
  ): Map[String, BigDecimal] =
    computeFromIterator(transactions.iterator)

  def computePositions(
    transactions: Vector[Transaction]
  ): Map[String, BigDecimal] =
    computeFromIterator(transactions.iterator)

  def multilateralNetting(
    transactions: List[Transaction]
  ): Map[Bank, BigDecimal] =
    computePositions(transactions).map: (code, balance) =>
      BankDirectory.fromCode(code) -> balance

  def isBalanced(positions: Map[String, BigDecimal]): Boolean =
    positions.values.sum == BigDecimal(0)

  def balanceError(
    positions: Map[String, BigDecimal]
  ): Option[String] =
    val difference = positions.values.sum
    Option.unless(difference == 0)(
      s"Déséquilibre du netting : $difference DH"
    )

  def settlementOrder(
    positions: Map[String, BigDecimal]
  ): List[BankPosition] =
    val nonZero = positions.toList.collect:
      case (code, balance) if balance != 0 =>
        BankPosition(BankDirectory.fromCode(code), balance)
    val (debtors, creditors) = nonZero.partition(_.balance < 0)

    debtors.sortBy(position => (position.balance, position.bank.code)) ++
      creditors.sortBy(position => (-position.balance, position.bank.code))
