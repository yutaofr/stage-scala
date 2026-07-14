package clearing

import scala.io.Source

object MainV03:
  private def parseLines(
    lines: List[String]
  ): List[TransactionV2.Transaction] =
    lines.flatMap { line =>
      Transaction(line).orElse {
        Console.err.println(s"[ERROR] Ligne ignorée par v0.3 : \"$line\"")
        None
      }
    }

  private def validTransactions(
    lines: List[String]
  ): List[TransactionV2.Transaction] =
    TransactionPipelineV3.cleanBatches(List(parseLines(lines)))

  def processLines(lines: List[String]): Map[String, BigDecimal] =
    NettingCalculator.calculate(validTransactions(lines))

  def renderReport(
    positions: Map[String, BigDecimal],
    validCount: Int
  ): String =
    val positionLines = positions.toList.sortBy(_._1).map { (bank, amount) =>
      val status = if amount >= 0 then "CRÉDITRICE" else "DÉBITRICE"
      s"$bank : $amount DH — $status"
    }
    (
      List(
        "=== CLEARING ENGINE v0.3 ===",
        s"Transactions valides : $validCount"
      ) ++ positionLines ++ List(
        s"Solde net global : ${NettingCalculator.globalNet(positions)} DH"
      )
    ).mkString("\n")

  def run(file: String): Map[String, BigDecimal] =
    val source = Source.fromFile(file)
    val lines = try source.getLines().toList
    finally source.close()
    val transactions = validTransactions(lines)
    val positions = NettingCalculator.calculate(transactions)
    println(renderReport(positions, transactions.size))
    positions

@main def runMainV03(arguments: String*): Unit =
  MainV03.run(ClearingEngine.inputFile(arguments))
