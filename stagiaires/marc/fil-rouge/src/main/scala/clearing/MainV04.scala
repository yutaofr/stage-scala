package clearing

import scala.io.Source

object MainV04:
  type ProcessingResult = (Map[String, BigDecimal], Int, Int)

  def processLines(
    lines: List[String],
    rules: List[ValidationRules.Rule] = ValidationRules.defaultRules
  ): ProcessingResult =
    val (acceptedTransactions, ignoredCount) =
      TransactionWorkflowV4.collectValid(lines, rules)
    val positions = NettingCalculator.calculate(acceptedTransactions)
    (positions, acceptedTransactions.size, ignoredCount)

  def renderReport(result: ProcessingResult): String =
    val (positions, acceptedCount, ignoredCount) = result
    val positionLines = positions.toList.sortBy(_._1).map { (bank, amount) =>
      val status = if amount >= 0 then "CRÉDITRICE" else "DÉBITRICE"
      s"$bank : $amount DH — $status"
    }
    (
      List(
        "=== CLEARING ENGINE v0.4 ===",
        s"Lignes reçues : ${acceptedCount + ignoredCount}",
        s"Acceptées : $acceptedCount | Ignorées : $ignoredCount"
      ) ++ positionLines ++ List(
        s"Solde net global : ${NettingCalculator.globalNet(positions)} DH"
      )
    ).mkString("\n")

  def run(
    file: String,
    rules: List[ValidationRules.Rule] = ValidationRules.defaultRules
  ): ProcessingResult =
    val source = Source.fromFile(file)
    val lines = try source.getLines().toList
    finally source.close()
    val result = processLines(lines, rules)
    println(renderReport(result))
    result

@main def runMainV04(arguments: String*): Unit =
  MainV04.run(ClearingEngine.inputFile(arguments))
