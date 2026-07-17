package clearing.v12

import clearing.model.*
import clearing.v11.*
import scala.io.Source
import scala.util.{Failure, Success, Using}

enum V12Command:
  case RunGenerated(size: Int)
  case RunFile(path: String)

object V12Cli:
  val usage: String =
    "Usage : run [chemin.csv] | run --generated <taille-positive-ou-nulle>"

  def parse(arguments: List[String]): Option[V12Command] =
    arguments match
      case Nil => Some(V12Command.RunFile("transactions-v11.csv"))
      case "--generated" :: rawSize :: Nil =>
        rawSize.toIntOption.filter(_ >= 0).map(V12Command.RunGenerated.apply)
      case path :: Nil if path != "--generated" =>
        Some(V12Command.RunFile(path))
      case _ => None

case class V12Result(
  inputDescription: String,
  receivedCount: Int,
  transactions: List[Transaction],
  rejectedLines: List[RejectedLine],
  fileErrors: List[ClearingError],
  validationWarnings: List[SuspiciousTransaction],
  bilateralSettlements: List[BilateralSettlement],
  positions: Map[String, BigDecimal],
  batches: List[BatchNetting],
  fraudAlerts: List[FraudWindowAlert],
  totalVolume: BigDecimal,
  mostActiveBank: Option[BankActivity],
  financialLogs: List[FinancialLog],
  technicalLogs: List[TechnicalLog]
)

object ClearingAppV12:
  private def displayAmount(amount: BigDecimal): String =
    amount.bigDecimal.stripTrailingZeros().toPlainString

  private def processValidated(
    inputDescription: String,
    receivedCount: Int,
    transactions: List[Transaction],
    rejectedLines: List[RejectedLine],
    fileErrors: List[ClearingError],
    validationWarnings: List[SuspiciousTransaction],
    batchSize: Int,
    windowSize: Int,
    windowThreshold: BigDecimal
  ): V12Result =
    val bilateral = BilateralNetting.settlements(transactions)
    val positions = MultilateralNetting.computePositions(transactions)
    val batches = FlowSegmentation.processBatches(transactions, batchSize)
    val alerts = FlowSegmentation.fraudWindows(
      transactions,
      windowSize,
      windowThreshold
    )
    val volume = BusinessReporter.totalVolume(transactions)
    val activeBank = BusinessReporter.mostActiveBank(transactions)
    val allLogs: List[EngineLog] = List(
      FinancialLog(s"Volume total : ${displayAmount(volume)} DH"),
      FinancialLog(
        s"Solde net global : ${displayAmount(positions.values.sum)} DH"
      ),
      TechnicalLog(s"${batches.size} batches calculés"),
      TechnicalLog(s"${alerts.size} fenêtres suspectes")
    )
    val logs = BusinessReporter.partitionLogs(allLogs)

    V12Result(
      inputDescription = inputDescription,
      receivedCount = receivedCount,
      transactions = transactions,
      rejectedLines = rejectedLines,
      fileErrors = fileErrors,
      validationWarnings = validationWarnings,
      bilateralSettlements = bilateral,
      positions = positions,
      batches = batches,
      fraudAlerts = alerts,
      totalVolume = volume,
      mostActiveBank = activeBank,
      financialLogs = logs.financial,
      technicalLogs = logs.technical
    )

  private def fromV11(
    result: V11Result,
    inputDescription: String,
    batchSize: Int,
    windowSize: Int,
    windowThreshold: BigDecimal
  ): V12Result =
    processValidated(
      inputDescription = inputDescription,
      receivedCount = result.receivedCount,
      transactions = result.successfulTransactions,
      rejectedLines = result.rejectedLines,
      fileErrors = result.fileErrors,
      validationWarnings = result.warnings,
      batchSize = batchSize,
      windowSize = windowSize,
      windowThreshold = windowThreshold
    )

  def processLines(
    lines: List[String],
    batchSize: Int = 1000,
    windowSize: Int = 5,
    windowThreshold: BigDecimal = BigDecimal("500000")
  ): V12Result =
    fromV11(
      ClearingAppV11.processLines(lines),
      s"${lines.size} lignes reçues.",
      batchSize,
      windowSize,
      windowThreshold
    )

  def processGenerated(
    size: Int,
    batchSize: Int = 1000,
    windowSize: Int = 5,
    windowThreshold: BigDecimal = BigDecimal("500000")
  ): V12Result =
    val transactions = S7TransactionGenerator.generateVector(size).toList
    processValidated(
      inputDescription = s"$size transactions générées.",
      receivedCount = size,
      transactions = transactions,
      rejectedLines = Nil,
      fileErrors = Nil,
      validationWarnings = Nil,
      batchSize = batchSize,
      windowSize = windowSize,
      windowThreshold = windowThreshold
    )

  def renderReport(result: V12Result): String =
    val activeBank = result.mostActiveBank
      .map(activity =>
        s"${activity.bank.code} (${activity.participationCount} participations)"
      )
      .getOrElse("aucune")
    val allBatchesBalanced = result.batches.forall(batch =>
      MultilateralNetting.isBalanced(batch.positions)
    )
    val alertLines = result.fraudAlerts.map(alert =>
      s"- IDs ${alert.ids.mkString(",")} : ${displayAmount(alert.totalAmount)} DH"
    )
    val fileErrorLines = result.fileErrors.map(error =>
      DetailedErrorReporter.detailedReport(error)
    )
    val financialLogLines = result.financialLogs.map(log =>
      s"- ${log.message}"
    )
    val technicalLogLines = result.technicalLogs.map(log =>
      s"- ${log.message}"
    )

    (List(
      "=== CLEARING ENGINE v1.2 ===",
      s"[INPUT] ${result.inputDescription}",
      s"[VALIDATION] ${result.transactions.size} validées | ${result.rejectedLines.size} rejetées | ${result.validationWarnings.size} avertissements",
      s"[BUSINESS] Volume total échangé : ${displayAmount(result.totalVolume)} DH",
      s"[BUSINESS] Banque la plus active : $activeBank",
      "[BILATERAL] Règlements nets :",
      BusinessReporter.renderBilateral(result.bilateralSettlements),
      "[MULTILATERAL] Rapport de règlement :",
      BusinessReporter.renderSettlement(result.positions),
      s"[BATCH] ${result.batches.size} batches — tous équilibrés : $allBatchesBalanced",
      s"[FRAUD WINDOW] ${result.fraudAlerts.size} alertes :"
    ) ++ alertLines ++ fileErrorLines ++ List(
      "[FINANCIAL LOGS]"
    ) ++ financialLogLines ++ List(
      "[TECHNICAL LOGS]"
    ) ++ technicalLogLines ++ List(
      s"[REPORT] Solde net global : ${displayAmount(result.positions.values.sum)} DH"
    )).mkString(System.lineSeparator())

  def runGenerated(size: Int): V12Result =
    val result = processGenerated(size)
    println(renderReport(result))
    result

  def runFile(path: String): V12Result =
    val result = Using(Source.fromFile(path))(_.getLines().toList) match
      case Success(lines) => processLines(lines)
      case Failure(_) =>
        fromV11(
          V11Result(
            receivedCount = 0,
            successfulTransactions = Nil,
            warnings = Nil,
            rejectedLines = Nil,
            fileErrors = List(
              FileReadFailure(path, "fichier introuvable ou illisible")
            ),
            netPositions = Map.empty
          ),
          "0 ligne reçue.",
          batchSize = 1000,
          windowSize = 5,
          windowThreshold = BigDecimal("500000")
        )

    println(renderReport(result))
    result

@main def runClearingAppV12(arguments: String*): Unit =
  V12Cli.parse(arguments.toList) match
    case Some(V12Command.RunGenerated(size)) =>
      ClearingAppV12.runGenerated(size)
    case Some(V12Command.RunFile(path)) =>
      ClearingAppV12.runFile(path)
    case None =>
      Console.err.println(V12Cli.usage)
