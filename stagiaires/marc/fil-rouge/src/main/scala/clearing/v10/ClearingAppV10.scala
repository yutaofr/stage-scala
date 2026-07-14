package clearing.v10

import clearing.model.*
import scala.io.Source

object ClearingAppV10:
  def processLines(
    lines: List[String],
    batchId: Int = 1
  ): AppResult =
    val parsed = CsvParserV10.parseLines(lines)
    val validation = TransactionValidator.partition(parsed)
    val batch = ClearingBatch(batchId, parsed, TransactionStatus.Pending)
    val result = BatchProcessor.processValidated(batch, validation)
    AppResult(
      result,
      validation.invalid,
      malformedCount = lines.size - parsed.size,
      receivedCount = lines.size
    )

  def renderReport(appResult: AppResult): String =
    val positions = appResult.result.netPositions
    val positionLines = positions.keys.toList.sorted.map(
      bank =>
        val amount = positions(bank)
        val status = if amount >= 0 then "CRÉDITRICE" else "DÉBITRICE"
        s"$bank : $amount DH — $status"
    )
    val errorLines = appResult.invalidTransactions.flatMap(invalid =>
      invalid.errors.map(error =>
        s"Transaction ${invalid.transaction.id} : ${ErrorReporter.formatError(error)}"
      )
    )
    val parsedCount = appResult.result.batch.transactions.size
    val invalidCount = appResult.invalidTransactions.size
    val validCount = parsedCount - invalidCount

    (
      List(
        "=== CLEARING ENGINE v1.0 ===",
        s"Lignes reçues : ${appResult.receivedCount}",
        s"Parsées : $parsedCount | Malformées : ${appResult.malformedCount}",
        s"Valides : $validCount | Invalides : $invalidCount"
      ) ++ positionLines ++ errorLines ++ List(
        s"Résultat : ${BatchProcessor.describeResult(appResult.result)}",
        s"Solde net global : ${NettingCalculatorV10.globalNet(positions)} DH"
      )
    ).mkString("\n")

  def run(file: String): AppResult =
    val source = Source.fromFile(file)
    val lines = try source.getLines().toList
    finally source.close()
    val result = processLines(lines)
    println(renderReport(result))
    result

  def inputFile(arguments: Seq[String]): String =
    arguments.headOption.getOrElse("transactions-v10.csv")

@main def runClearingAppV10(arguments: String*): Unit =
  ClearingAppV10.run(ClearingAppV10.inputFile(arguments))
