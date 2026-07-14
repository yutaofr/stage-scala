package clearing.v12

import clearing.model.*

object BilateralNetting:
  type PairKey = (String, String)

  def indexByPair(
    transactions: List[Transaction]
  ): Map[PairKey, List[Transaction]] =
    transactions
      .filter(_.status == TransactionStatus.Validated)
      .groupBy(transaction => (transaction.sender, transaction.receiver))

  def totalsByPair(
    transactions: List[Transaction]
  ): Map[PairKey, BigDecimal] =
    indexByPair(transactions).view
      .mapValues(_.map(_.amount).sum)
      .toMap

  def namedTotalsByPair(
    transactions: List[Transaction]
  ): Map[BankPair, BigDecimal] =
    totalsByPair(transactions).map:
      case ((sender, receiver), amount) =>
        BankPair(
          BankDirectory.fromCode(sender),
          BankDirectory.fromCode(receiver)
        ) -> amount

  def getNetDuo(
    firstBank: String,
    secondBank: String,
    data: Map[PairKey, BigDecimal]
  ): BigDecimal =
    data.getOrElse((firstBank, secondBank), BigDecimal(0)) -
      data.getOrElse((secondBank, firstBank), BigDecimal(0))

  def settlements(
    transactions: List[Transaction]
  ): List[BilateralSettlement] =
    val totals = totalsByPair(transactions)
    val duos = totals.keys.map:
      case (first, second) if first < second => (first, second)
      case (first, second)                   => (second, first)

    duos.toSet.toList.sorted.flatMap: (first, second) =>
      val net = getNetDuo(first, second, totals)
      if net > 0 then
        Some(
          BilateralSettlement(
            BankDirectory.fromCode(first),
            BankDirectory.fromCode(second),
            net
          )
        )
      else if net < 0 then
        Some(
          BilateralSettlement(
            BankDirectory.fromCode(second),
            BankDirectory.fromCode(first),
            -net
          )
        )
      else None
