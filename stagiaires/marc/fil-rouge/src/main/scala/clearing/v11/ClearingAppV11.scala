package clearing.v11

import clearing.model.*
import clearing.v10.NettingCalculatorV10
import scala.io.Source
import scala.util.{Failure, Success, Using}

case class RejectedLine(
  lineNumber: Int,
  raw: String,
  transactionId: Option[Int],
  errors: List[ClearingError]
)

case class V11Result(
  receivedCount: Int,
  successfulTransactions: List[Transaction],
  warnings: List[SuspiciousTransaction],
  rejectedLines: List[RejectedLine],
  fileErrors: List[ClearingError],
  netPositions: Map[String, BigDecimal]
)

object ClearingAppV11:
  private case class ProcessingState(
    seenIds: Set[Int],
    successfulTransactions: List[Transaction],
    warnings: List[SuspiciousTransaction],
    rejectedLines: List[RejectedLine]
  )

  private val initialState = ProcessingState(Set.empty, Nil, Nil, Nil)

  private def displayAmount(amount: BigDecimal): String =
    amount.bigDecimal.stripTrailingZeros().toPlainString

  private def errorLabel(error: ClearingError): String =
    error match
      case _: HighLevelError => "BLOCKING"
      case _: LineError      => "ERROR"
      case _: SystemError    => "SYSTEM"

  def processLines(lines: List[String]): V11Result =
    val finalState = lines.indices.foldLeft(initialState): (state, index) =>
      val raw = lines(index)
      val lineNumber = index + 1

      Transaction.fromCsv(raw) match
        case None =>
          state.copy(
            rejectedLines = RejectedLine(
              lineNumber,
              raw,
              None,
              List(MalformedCsv(raw))
            ) :: state.rejectedLines
          )
        case Some(transaction) =>
          val assessment = AdvancedTransactionValidator.assess(transaction)
          val duplicateErrors = Option.when(
            state.seenIds.contains(transaction.id)
          )(DuplicateTransaction).toList
          val errors = assessment.errors ++ duplicateErrors
          val nextSeenIds = state.seenIds + transaction.id

          if errors.isEmpty then
            state.copy(
              seenIds = nextSeenIds,
              successfulTransactions =
                transaction :: state.successfulTransactions,
              warnings = assessment.warnings.reverse ::: state.warnings
            )
          else
            state.copy(
              seenIds = nextSeenIds,
              rejectedLines = RejectedLine(
                lineNumber,
                raw,
                Some(transaction.id),
                errors
              ) :: state.rejectedLines
            )

    val successfulTransactions =
      finalState.successfulTransactions.reverse
    val fileErrors = Option.when(lines.isEmpty)(EmptyFile).toList

    V11Result(
      receivedCount = lines.size,
      successfulTransactions = successfulTransactions,
      warnings = finalState.warnings.reverse,
      rejectedLines = finalState.rejectedLines.reverse,
      fileErrors = fileErrors,
      netPositions = NettingCalculatorV10.calculate(successfulTransactions)
    )

  def renderReport(result: V11Result): String =
    val fileErrorLines = result.fileErrors.map: error =>
      s"[${errorLabel(error)}] ${DetailedErrorReporter.detailedReport(error)}"

    val warningLines = result.warnings.map: warning =>
      s"  - ${DetailedErrorReporter.detailedReport(warning)}"

    val rejectedLines = result.rejectedLines.map: rejected =>
      val details = rejected.errors
        .map(DetailedErrorReporter.detailedReport)
        .mkString(" | ")
      s"  - Ligne ${rejected.lineNumber} : $details"

    val positionLines = result.netPositions.keys.toList.sorted.map: bank =>
      s"  - $bank : ${displayAmount(result.netPositions(bank))} DH"

    val reportLines = List(
      "=== CLEARING ENGINE v1.1 ===",
      s"[INPUT] ${result.receivedCount} lignes reçues.",
      s"[SUCCESS] ${result.successfulTransactions.size} transactions traitées avec succès.",
      s"[WARNING] ${result.warnings.size} transactions suspectes (Fraud Signal)."
    ) ++ warningLines ++ List(
      s"[ERROR] ${result.rejectedLines.size} lignes ignorées :"
    ) ++ rejectedLines ++ fileErrorLines ++ List(
      "[REPORT] Positions nettes :"
    ) ++ positionLines ++ List(
      s"[REPORT] Solde net global : ${displayAmount(NettingCalculatorV10.globalNet(result.netPositions))} DH"
    )

    reportLines.mkString(System.lineSeparator())

  def run(path: String): V11Result =
    val result = Using(Source.fromFile(path))(_.getLines().toList) match
      case Success(lines) => processLines(lines)
      case Failure(_) =>
        V11Result(
          receivedCount = 0,
          successfulTransactions = Nil,
          warnings = Nil,
          rejectedLines = Nil,
          fileErrors = List(
            FileReadFailure(path, "fichier introuvable ou illisible")
          ),
          netPositions = Map.empty
        )

    println(renderReport(result))
    result

@main def runClearingAppV11(
  inputFile: String = "transactions-v11.csv"
): Unit =
  ClearingAppV11.run(inputFile)
