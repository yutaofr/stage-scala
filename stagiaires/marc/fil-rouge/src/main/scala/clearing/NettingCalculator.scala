package clearing

object NettingCalculator:
  def netBalance(transactions: List[BigDecimal]): BigDecimal =
    transactions.foldLeft(BigDecimal(0))(_ + _)

  def sumAndCount(amounts: Iterable[BigDecimal]): (BigDecimal, Int) =
    amounts.foldLeft((BigDecimal(0), 0)) { case ((sum, count), amount) =>
      (sum + amount, count + 1)
    }

  def sumWithReduce(amounts: List[BigDecimal]): Option[BigDecimal] =
    amounts.reduceOption(_ + _)

  def average(amounts: Iterable[BigDecimal]): Option[BigDecimal] =
    sumAndCount(amounts) match
      case (_, 0)         => None
      case (sum, count)   => Some(sum / count)

  def calculate(
    transactions: Iterable[TransactionV2.Transaction]
  ): Map[String, BigDecimal] =
    transactions.foldLeft(Map.empty[String, BigDecimal]) {
      case (positions, (_, sender, receiver, amount, _)) =>
        val afterDebit = positions.updated(
          sender,
          positions.getOrElse(sender, BigDecimal(0)) - amount
        )
        afterDebit.updated(
          receiver,
          afterDebit.getOrElse(receiver, BigDecimal(0)) + amount
        )
    }

  type SenderStats = (BigDecimal, BigDecimal, Int)

  def statisticsBySender(
    transactions: List[TransactionV2.Transaction]
  ): Map[String, SenderStats] =
    transactions
      .groupBy(_._2)
      .view
      .mapValues { sent =>
        val amounts = sent.map(_._4)
        val (total, count) = sumAndCount(amounts)
        (amounts.maxOption.getOrElse(BigDecimal(0)), total, count)
      }
      .toMap

  def globalNet(positions: Map[String, BigDecimal]): BigDecimal =
    positions.values.foldLeft(BigDecimal(0))(_ + _)

  def countBySign(transactions: List[BigDecimal]): (Int, Int) =
    val credits = transactions.count(_ > 0)
    val debits = transactions.count(_ < 0)
    (credits, debits)

  def summary(bankName: String, transactions: List[BigDecimal]): String =
    val net = netBalance(transactions)
    val (credits, debits) = countBySign(transactions)
    val position = if net >= 0 then "CRÉDITRICE" else "DÉBITRICE"
    s"""
       |=== Résumé $bankName ===
       |Nombre de crédits : $credits
       |Nombre de débits  : $debits
       |Solde net          : $net DH
       |Position           : $position
       |""".stripMargin
