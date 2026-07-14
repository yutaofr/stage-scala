package clearing.model

sealed trait ClearingError

sealed trait HighLevelError extends ClearingError

sealed trait LineError extends ClearingError

sealed trait SystemError extends ClearingError

enum ParsingFailure:
  case ColumnCount(actual: Int)
  case InvalidId
  case InvalidAmount
  case InvalidTransactionType
  case InvalidCurrency

  def code: String = this match
    case ParsingFailure.ColumnCount(_)         => "PARSE_COLUMNS"
    case ParsingFailure.InvalidId              => "PARSE_ID"
    case ParsingFailure.InvalidAmount          => "PARSE_AMOUNT"
    case ParsingFailure.InvalidTransactionType => "PARSE_TYPE"
    case ParsingFailure.InvalidCurrency        => "PARSE_CURRENCY"

  def message: String = this match
    case ParsingFailure.ColumnCount(actual) =>
      s"8 colonnes attendues, $actual reçues"
    case ParsingFailure.InvalidId     => "identifiant illisible"
    case ParsingFailure.InvalidAmount => "montant illisible"
    case ParsingFailure.InvalidTransactionType =>
      "type de transaction inconnu"
    case ParsingFailure.InvalidCurrency => "devise inconnue"

case class ParsingError(lineNumber: Int, failure: ParsingFailure)
    extends LineError

case class TransactionValidationError(
  lineNumber: Int,
  transactionId: Option[Int],
  reasons: List[String]
) extends ValidationError:
  val field: String = "transaction"
  val message: String = reasons.mkString("+")

case class TechnicalError(
  operation: String,
  causeType: String,
  detail: String
) extends SystemError

object TechnicalError:
  def fromThrowable(
    operation: String,
    detail: String
  )(
    error: Throwable
  ): TechnicalError =
    TechnicalError(
      operation = operation,
      causeType = error.getClass.getSimpleName,
      detail = detail
    )

sealed trait ValidationError extends LineError:
  def field: String
  def message: String

sealed trait BusinessError extends LineError:
  def internalCode: String
  def businessMessage: String

case class CorruptedFile(reason: String) extends HighLevelError

case object EmptyFile extends HighLevelError

case class FileReadFailure(path: String, reason: String) extends SystemError

case class InvalidAmount(amount: BigDecimal) extends ValidationError:
  val field: String = "amount"
  val message: String = s"$amount DH est invalide"

case class UnknownBank(code: String) extends ValidationError:
  val field: String = "bank"
  val message: String = s"$code est inconnue"

case class InvalidIban(value: String) extends ValidationError:
  val field: String = "iban"
  val message: String = s"$value est invalide"

case class FieldValidationError(field: String, message: String)
    extends ValidationError

case class MalformedCsv(raw: String) extends ValidationError:
  val field: String = "csv"
  val message: String = s"format invalide : $raw"

case object DuplicateTransaction extends BusinessError:
  val internalCode: String = "DUPL"
  val businessMessage: String = "transaction dupliquée"

case class SuspiciousTransaction(transactionId: Int, reason: String)
    extends BusinessError:
  val internalCode: String = "FRAUD"
  val businessMessage: String = s"transaction $transactionId — $reason"

enum Iso20022Code(val description: String):
  case AC01 extends Iso20022Code("numéro de compte incorrect")
  case AC04 extends Iso20022Code("compte clôturé")
  case AC06 extends Iso20022Code("compte bloqué")
  case AG01 extends Iso20022Code("transaction interdite")
  case AM04 extends Iso20022Code("solde insuffisant")
  case AM05 extends Iso20022Code("opération dupliquée")
  case FF01 extends Iso20022Code("format de fichier invalide")
  case MD01 extends Iso20022Code("mandat absent")
  case RC01 extends Iso20022Code("identifiant bancaire invalide")
  case RR01 extends Iso20022Code("information réglementaire manquante")

case class Iso20022Rejection(code: Iso20022Code, transactionId: Int)
    extends BusinessError:
  val internalCode: String = code.toString
  val businessMessage: String =
    s"transaction $transactionId — ${code.description}"
