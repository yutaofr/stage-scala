package clearing.v21

import clearing.model.*
import scala.math.BigDecimal.RoundingMode

object RailRenderer:
  def renderReport(report: V21Report): String =
    val statistics = report.statistics
    val lineRows = report.lineResults.map: line =>
      s"LIGNE|${line.lineNumber}|${renderLine(line.result)}"
    val positionRows = report.positions.toList.sortBy(_._1).map:
      (bank, amount) => s"POSITION|$bank|${formatAmount(amount)}"
    val feeRows = report.feesByBank.toList.sortBy(_._1).map:
      (bank, amount) => s"FRAIS|$bank|${formatAmount(amount)}"
    val header = List(
      s"V21|REFERENCE|${report.referenceCurrency}",
      s"SUCCES|${report.successes.size}",
      s"REJETS|${report.errors.size}",
      s"STATISTIQUES|parsing=${statistics.parsing}|validation=${statistics.validation}|business=${statistics.business}|technical=${statistics.technical}|warnings=${statistics.warnings}"
    )
    val global = s"GLOBAL|${formatAmount(report.positions.values.sum)}"

    (header ++ lineRows ++ positionRows ++ feeRows :+ global).mkString("\n")

  def renderLine(
    result: Either[ClearingError, RailSuccess]
  ): String =
    result.fold(renderError, renderSuccess)

  def renderSuccess(success: RailSuccess): String =
    val amount = formatAmount(success.prepared.settlementAmount)
    val firstLine =
      s"Transaction OK : $amount ${success.prepared.referenceCurrency}"
    val warningLines = success.warnings.map:
      case LightWarning.MissingLabel(defaultLabel) =>
        s"AVERTISSEMENT : LABEL_MANQUANT -> $defaultLabel"

    (firstLine :: warningLines).mkString("\n")

  def renderError(error: ClearingError): String =
    val (code, reason) = error match
      case ParsingError(_, failure) =>
        (failure.code, failure.message)
      case TransactionValidationError(_, _, reasons) =>
        ("VALIDATION_TRANSACTION", reasons.mkString("+"))
      case validation: ValidationError =>
        ("VALIDATION", validation.message)
      case business: BusinessError =>
        (business.internalCode, business.businessMessage)
      case CorruptedFile(reason) =>
        ("FICHIER_CORROMPU", reason)
      case EmptyFile =>
        ("FICHIER_VIDE", "fichier vide")
      case FileReadFailure(path, reason) =>
        ("TECH_READ", s"lecture $path : $reason")
      case TechnicalError(operation, causeType, detail) =>
        val code = operation.toUpperCase.replace('-', '_')
        (s"TECH_$code", s"$causeType : $detail")

    s"REJET : $code - $reason"

  private def formatAmount(amount: BigDecimal): String =
    amount
      .setScale(2, RoundingMode.HALF_UP)
      .bigDecimal
      .toPlainString
