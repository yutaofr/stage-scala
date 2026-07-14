package clearing

object ReportGenerator:
  type Pair = (String, String)
  type PairStats = (Int, BigDecimal)

  def bilateralStats(
    transactions: List[TransactionV2.Transaction]
  ): Map[Pair, PairStats] =
    transactions
      .groupBy { case (_, sender, receiver, _, _) => (sender, receiver) }
      .view
      .mapValues { directed =>
        (directed.size, directed.map(_._4).sum)
      }
      .toMap

  def bilateralReport(transactions: List[TransactionV2.Transaction]): Unit =
    bilateralStats(transactions).toList.sortBy(_._1).foreach {
      case ((sender, receiver), (count, total)) =>
        println(s"$sender -> $receiver : $count transaction(s), $total DH")
    }

  def topTransactions(
    transactions: List[TransactionV2.Transaction],
    n: Int
  ): List[TransactionV2.Transaction] =
    transactions.sortBy(transaction => -transaction._4.abs).take(n)

  def bilateralMatrix(
    transactions: List[TransactionV2.Transaction]
  ): Map[Pair, BigDecimal] =
    transactions
      .flatMap { case (_, sender, receiver, amount, _) =>
        List((sender, receiver) -> amount, (receiver, sender) -> -amount)
      }
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2).sum)
      .toMap

  def renderMatrix(
    transactions: List[TransactionV2.Transaction],
    banks: List[String]
  ): String =
    val matrix = bilateralMatrix(transactions)
    val header = ("BANQUE" :: banks).mkString(" | ")
    val rows = for rowBank <- banks yield
      val cells = for columnBank <- banks yield
        if rowBank == columnBank then "-"
        else matrix.getOrElse((rowBank, columnBank), BigDecimal(0)).toString
      (rowBank :: cells).mkString(" | ")
    (header :: rows).mkString("\n")
