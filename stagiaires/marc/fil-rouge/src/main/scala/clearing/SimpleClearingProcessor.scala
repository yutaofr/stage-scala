package clearing

import scala.io.Source

object SimpleClearingProcessor extends ClearingProcessor with Logger:
  def validate(tx: TransactionV2.Transaction): Boolean =
    tx match
      case (_, sender, receiver, amount, _) =>
        Validator.isKnownBank(sender) &&
          Validator.isKnownBank(receiver) &&
          Validator.isPositiveAmount(amount)

  def calculate(
    transactions: List[TransactionV2.Transaction]
  ): Map[String, BigDecimal] =
    NettingCalculator.calculate(transactions)

  def report(results: Map[String, BigDecimal]): Unit =
    results.toList.sortBy(_._1).foreach { (bank, net) =>
      val position = if net >= 0 then "CRÉDITRICE" else "DÉBITRICE"
      println(s"$bank : $net DH — $position")
    }
    println(s"Solde net global : ${results.values.sum} DH")

  def run(file: String): Map[String, BigDecimal] =
    log(s"Lecture du fichier : $file")
    val source = Source.fromFile(file)
    val lines = try source.getLines().toList
    finally source.close()
    log(s"Lecture terminée : ${lines.size} ligne(s)")
    val results = ClearingEngine.processLines(lines, this)
    log("Traitement terminé")
    results
