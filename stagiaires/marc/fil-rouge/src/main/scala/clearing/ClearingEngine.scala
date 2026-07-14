package clearing

object ClearingEngine:
  type Transfer = (String, String, BigDecimal)
  type BankStats = (BigDecimal, BigDecimal, String)

  val sampleBatch: List[Transfer] = List(
    ("ATH", "CIH", BigDecimal("100")),
    ("CIH", "BOA", BigDecimal("40")),
    ("BOA", "ATH", BigDecimal("20")),
    ("ATH", "BOA", BigDecimal("60")),
    ("ATH", "UNKNOWN", BigDecimal("25")),
    ("CIH", "ATH", BigDecimal("-5"))
  )

  def partitionTransactions(
    batch: List[Transfer]
  ): (List[Transfer], List[Transfer]) =
    batch.partition { (sender, receiver, amount) =>
      Validator.isPositiveAmount(amount) &&
      Validator.isKnownBank(sender) &&
      Validator.isKnownBank(receiver)
    }

  def netPositions(transactions: List[Transfer]): Map[String, BigDecimal] =
    transactions
      .flatMap { (sender, receiver, amount) =>
        List(sender -> -amount, receiver -> amount)
      }
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2).sum)
      .toMap

  def bankStats(bank: String, transactions: List[Transfer]): BankStats =
    val amounts = transactions.collect {
      case (sender, receiver, amount) if sender == bank || receiver == bank =>
        amount
    }

    if amounts.isEmpty then
      (BigDecimal(0), BigDecimal("0.00"), "Aucune")
    else
      val maximum = amounts.max
      val average = (amounts.sum / amounts.size)
        .setScale(2, BigDecimal.RoundingMode.HALF_UP)
      val dominantCategory = amounts
        .map(TransactionCategorizer.categorize)
        .groupBy(identity)
        .toList
        .sortBy { (category, values) => (-values.size, category) }
        .head
        ._1
      (maximum, average, dominantCategory)

  def renderReport(batch: List[Transfer]): String =
    val (valid, rejected) = partitionTransactions(batch)
    val positions = netPositions(valid)
    val positionLines = positions.toList.sortBy(_._1).map { (bank, net) =>
      val position = if net >= 0 then "CRÉDITRICE" else "DÉBITRICE"
      val (maximum, average, category) = bankStats(bank, valid)
      s"$bank : $net DH — $position | max=$maximum, moyenne=$average, catégorie=$category"
    }
    val globalNet = positions.values.sum

    (
      List(
        "=== MOTEUR DE COMPENSATION v0.1 ===",
        s"Transactions reçues : ${batch.size}",
        s"Valides : ${valid.size} | Rejetées : ${rejected.size}"
      ) ++ positionLines ++ List(s"Solde net global : $globalNet DH")
    ).mkString("\n")

  def processLines(
    lines: List[String],
    processor: ClearingProcessor
  ): Map[String, BigDecimal] =
    val transactions = CsvParser.parseLines(lines)
    val results = processor.process(transactions)
    processor.report(results)
    results

  def inputFile(arguments: Seq[String]): String =
    arguments.headOption.getOrElse("transactions.csv")

@main def runClearingEngine(files: String*): Unit =
  SimpleClearingProcessor.run(ClearingEngine.inputFile(files))
