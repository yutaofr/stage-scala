package clearing.v22

import java.util.Locale

enum V22ErrorCategory:
  case Parsing, Validation, Business, Technical

sealed trait V22Error:
  def lineNumber: Int
  def transactionId: Option[Int]
  def code: String
  def message: String
  def category: V22ErrorCategory

  final def toRejection: Rejection =
    Rejection(lineNumber, transactionId, code, message)

enum V22ParsingFailure:
  case ColumnCount(actual: Int)
  case InvalidId
  case InvalidSender
  case InvalidReceiver
  case InvalidSourceIban
  case InvalidDestinationIban
  case InvalidAmount
  case InvalidTransactionType
  case InvalidCurrency

  def code: String = this match
    case ColumnCount(_)         => "PARSE_COLUMNS"
    case InvalidId              => "PARSE_ID"
    case InvalidSender          => "PARSE_SENDER"
    case InvalidReceiver        => "PARSE_RECEIVER"
    case InvalidSourceIban      => "PARSE_SOURCE_IBAN"
    case InvalidDestinationIban => "PARSE_DESTINATION_IBAN"
    case InvalidAmount          => "PARSE_AMOUNT"
    case InvalidTransactionType => "PARSE_TYPE"
    case InvalidCurrency        => "PARSE_CURRENCY"

  def message: String = this match
    case ColumnCount(actual) => s"huit colonnes attendues, $actual reçues"
    case InvalidId           => "identifiant illisible"
    case InvalidSender       => "banque source invalide"
    case InvalidReceiver     => "banque destination invalide"
    case InvalidSourceIban   => "IBAN source invalide"
    case InvalidDestinationIban => "IBAN destination invalide"
    case InvalidAmount          => "montant illisible"
    case InvalidTransactionType => "type de transaction inconnu"
    case InvalidCurrency        => "devise inconnue"

case class V22ParsingError(
  lineNumber: Int,
  failure: V22ParsingFailure
) extends V22Error:
  val transactionId = None
  def code: String = failure.code
  def message: String = failure.message
  val category = V22ErrorCategory.Parsing

case class V22ValidationError(
  lineNumber: Int,
  id: Int,
  reasons: List[String]
) extends V22Error:
  def transactionId: Option[Int] = Some(id)
  val code = "VALIDATION_TRANSACTION"
  def message: String = reasons.mkString("+")
  val category = V22ErrorCategory.Validation

case class V22DuplicateError(lineNumber: Int, id: Int) extends V22Error:
  def transactionId: Option[Int] = Some(id)
  val code = "AM05"
  def message: String = s"transaction $id — opération dupliquée"
  val category = V22ErrorCategory.Business

case class V22ConfigurationError(
  lineNumber: Int,
  id: Int,
  code: String,
  message: String
) extends V22Error:
  def transactionId: Option[Int] = Some(id)
  val category = V22ErrorCategory.Business

case class V22TechnicalError(
  lineNumber: Int,
  transactionId: Option[Int],
  operation: String,
  causeType: String,
  detail: String
) extends V22Error:
  def code: String =
    s"TECH_${operation.toUpperCase(Locale.ROOT).replace('-', '_')}"
  def message: String = s"$causeType : $detail"
  val category = V22ErrorCategory.Technical
