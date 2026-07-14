package clearing.v21

import clearing.model.*
import scala.math.BigDecimal.RoundingMode

object RailRenderer:
  def renderLine(
    result: Either[ClearingError, RailSuccess]
  ): String =
    result.fold(renderError, renderSuccess)

  def renderSuccess(success: RailSuccess): String =
    val amount = success.prepared.settlementAmount
      .setScale(2, RoundingMode.HALF_UP)
      .bigDecimal
      .toPlainString
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

    s"REJET : $code - $reason"
