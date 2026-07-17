package clearing.v22

import clearing.model.{Currency, TransactionType}
import clearing.v13.SecurityUtils
import clearing.v22.DomainTypes.*
import java.util.Locale
import scala.util.control.NonFatal

enum OutputFormat:
  case Json, Csv, Xml

object OutputFormat:
  def fromString(raw: String): Option[OutputFormat] =
    raw.trim.toUpperCase(Locale.ROOT) match
      case "JSON" => Some(OutputFormat.Json)
      case "CSV"  => Some(OutputFormat.Csv)
      case "XML"  => Some(OutputFormat.Xml)
      case _      => None

case class V22Command(
  path: String,
  format: OutputFormat,
  simulateHashFailure: Boolean
)

object V22Cli:
  val DefaultPath = "transactions-v22.csv"
  val Usage =
    "Usage : run [--format JSON|CSV|XML] [--simulate-hash-failure] [chemin.csv]"

  private case class ParseState(
    path: Option[String] = None,
    format: OutputFormat = OutputFormat.Json,
    formatSeen: Boolean = false,
    simulateHashFailure: Boolean = false
  )

  def parse(args: List[String]): Either[String, V22Command] =
    def loop(
      remaining: List[String],
      state: ParseState
    ): Either[String, ParseState] = remaining match
      case Nil => Right(state)
      case "--format" :: raw :: tail if !state.formatSeen =>
        OutputFormat.fromString(raw).toRight(Usage).flatMap: format =>
          loop(tail, state.copy(format = format, formatSeen = true))
      case "--simulate-hash-failure" :: tail
          if !state.simulateHashFailure =>
        loop(tail, state.copy(simulateHashFailure = true))
      case path :: tail if !path.startsWith("--") && state.path.isEmpty =>
        loop(tail, state.copy(path = Some(path)))
      case _ => Left(Usage)

    loop(args, ParseState()).map: state =>
      V22Command(
        path = state.path.getOrElse(DefaultPath),
        format = state.format,
        simulateHashFailure = state.simulateHashFailure
      )

object V22Profiles:
  private val ath = BankCode.unsafe("ATH")
  private val cih = BankCode.unsafe("CIH")
  private val boa = BankCode.unsafe("BOA")
  private val sgmb = BankCode.unsafe("SGMB")
  private val bcp = BankCode.unsafe("BCP")

  val clearingMAD = V22Config(
    referenceCurrency = Currency.MAD,
    knownBanks = Set(ath, cih, boa, sgmb, bcp),
    limits = Map(
      TransactionType.Transfer -> Money(BigDecimal("10000")),
      TransactionType.Withdrawal -> Money(BigDecimal("5000")),
      TransactionType.Check -> Money(BigDecimal("20000"))
    ),
    ratesToReference = Map(
      Currency.MAD -> BigDecimal(1),
      Currency.EUR -> BigDecimal("10.80")
    ),
    feeRates = Map(
      ath -> BigDecimal("0.001"),
      cih -> BigDecimal("0.002"),
      boa -> BigDecimal("0.0015"),
      sgmb -> BigDecimal("0.0012"),
      bcp -> BigDecimal("0.0011")
    ),
    labelsByTransactionId = Map(1 -> "Facture fournisseur")
  )

object ClearingAppV22:
  def run(command: V22Command): String = runSafely:
    V22IO.read(command.path).fold(
      renderError,
      content =>
        renderResult(
          TypedRailwayEngine.process(
            V22Profiles.clearingMAD,
            hashBoundary(command.simulateHashFailure)
          )(content),
          command.format
        )
    )

  def runSafely(program: => String): String =
    try program
    catch
      case NonFatal(error) =>
        renderError(
          V22TechnicalError(
            lineNumber = 0,
            transactionId = None,
            operation = "run-application",
            causeType = error.getClass.getSimpleName,
            detail = "erreur inattendue"
          )
        )

  private def renderResult(
    result: ClearingResult,
    format: OutputFormat
  ): String = format match
    case OutputFormat.Json =>
      import JsonSerializers.given
      ExportEngine.`export`(result)
    case OutputFormat.Csv =>
      import CsvSerializers.given
      ExportEngine.`export`(result)
    case OutputFormat.Xml =>
      import XmlSerializers.given
      ExportEngine.`export`(result)

  private def renderError(error: V22Error): String =
    s"REJET : ${error.code} - ${error.message}"

  private def hashBoundary(simulateFailure: Boolean): HashBoundary = iban =>
    if simulateFailure && iban.bankSegment.startsWith("BOA") then
      Left(HashFailure.Simulated)
    else
      SecurityUtils
        .hashIbanTry(iban.value)
        .toEither
        .left
        .map(_ => HashFailure.Unavailable)
        .flatMap: rawHash =>
          IbanHash.from(rawHash).left.map(_ => HashFailure.InvalidDigest)

object V22Reporter:
  def print(report: String): Unit = println(report)

@main def runClearingAppV22(args: String*): Unit =
  val rendered = V22Cli.parse(args.toList).fold(identity, ClearingAppV22.run)
  V22Reporter.print(rendered)
