package clearing.v10

import clearing.model.*

object ErrorReporter:
  def formatError(error: ClearingError): String =
    error match
      case InvalidAmount(amount) => s"Montant invalide : $amount DH"
      case UnknownBank(code)     => s"Banque inconnue : $code"
      case DuplicateTransaction => "Transaction dupliquée"
      case ValidationError(field, message) =>
        s"Validation $field : $message"

  def formatErrors(errors: List[ClearingError]): String =
    errors.map(formatError).mkString("\n")
