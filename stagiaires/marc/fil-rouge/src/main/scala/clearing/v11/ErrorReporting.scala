package clearing.v11

import clearing.model.*

object ErrorAnalysis:
  def lineErrors(errors: List[ClearingError]): List[LineError] =
    errors.collect:
      case error: LineError => error

  def highLevelCount(errors: List[ClearingError]): Int =
    errors.count:
      case _: HighLevelError => true
      case _                 => false

object DetailedErrorReporter:
  def detailedReport(error: ClearingError): String =
    error match
      case highLevel: HighLevelError =>
        highLevel match
          case CorruptedFile(reason) =>
            s"Erreur bloquante — fichier corrompu : $reason"
          case EmptyFile => "Erreur bloquante — fichier vide"
      case line: LineError =>
        line match
          case validation: ValidationError =>
            s"Erreur de validation [${validation.field}] : ${validation.message}"
          case business: BusinessError =>
            s"Erreur métier [${business.internalCode}] : ${business.businessMessage}"
          case ParsingError(lineNumber, failure) =>
            s"Erreur de parsing [ligne $lineNumber] : ${failure.message}"
      case system: SystemError =>
        system match
          case FileReadFailure(path, reason) =>
            s"Erreur système — lecture $path : $reason"
