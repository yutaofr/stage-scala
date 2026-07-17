package clearing

object TransactionRouter:
  def route(tx: TransactionV2.Transaction): String =
    tx match
      case (_, sender, receiver, _, _) if sender == receiver =>
        "VIREMENT_INTERNE"
      case (_, _, _, amount, "VIR") if amount > BigDecimal("50000") =>
        "AUDIT_REQUIS"
      case (_, _, _, _, "PRE") =>
        "PRELEVEMENT"
      case _ =>
        "TRAITEMENT_STANDARD"

  def describeValue(value: Any): String =
    value match
      case number: Int        => s"Entier: $number"
      case text: String       => s"Texte: $text"
      case amount: BigDecimal => s"Montant: $amount"
      case _                  => "Type inconnu"

  def extractStats(
    transactions: List[TransactionV2.Transaction]
  ): (Int, BigDecimal, Map[String, Int]) =
    val total = transactions.map { case (_, _, _, amount, _) => amount }.sum
    val byType = transactions
      .groupBy { case (_, _, _, _, transactionType) => transactionType }
      .view
      .mapValues(_.size)
      .toMap
    (transactions.size, total, byType)

  def statsReport(transactions: List[TransactionV2.Transaction]): String =
    val (count, total, byType) = extractStats(transactions)
    val types = byType.toList.sortBy(_._1).map { (name, size) =>
      s"$name=$size"
    }.mkString(", ")
    s"Transactions : $count\nMontant total : $total DH\nTypes : $types"
