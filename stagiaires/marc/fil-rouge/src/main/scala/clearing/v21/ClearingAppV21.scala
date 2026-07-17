package clearing.v21

import clearing.model.*
import clearing.v13.SecurityUtils
import scala.util.control.NonFatal

case class V21Command(path: String, simulateHashFailure: Boolean)

object V21Cli:
  val DefaultPath = "transactions-v21.csv"
  val Usage =
    "Usage : run [--simulate-hash-failure] [chemin.csv]"

  def parse(args: List[String]): Either[String, V21Command] = args match
    case Nil => Right(V21Command(DefaultPath, simulateHashFailure = false))
    case "--simulate-hash-failure" :: Nil =>
      Right(V21Command(DefaultPath, simulateHashFailure = true))
    case "--simulate-hash-failure" :: path :: Nil
        if !path.startsWith("--") =>
      Right(V21Command(path, simulateHashFailure = true))
    case path :: Nil if !path.startsWith("--") =>
      Right(V21Command(path, simulateHashFailure = false))
    case _ => Left(Usage)

object V21Profiles:
  val clearingMAD = V21Config(
    referenceCurrency = Currency.MAD,
    knownBanks = Set("ATH", "CIH", "BOA", "SGMB", "BCP"),
    limits = Map(
      TransactionType.Transfer -> BigDecimal("10000"),
      TransactionType.Withdrawal -> BigDecimal("5000"),
      TransactionType.Check -> BigDecimal("20000")
    ),
    ratesToReference = Map(
      Currency.MAD -> BigDecimal("1"),
      Currency.EUR -> BigDecimal("10.80")
    ),
    feeRates = Map(
      "ATH" -> BigDecimal("0.001"),
      "CIH" -> BigDecimal("0.002"),
      "BOA" -> BigDecimal("0.0015"),
      "SGMB" -> BigDecimal("0.0012"),
      "BCP" -> BigDecimal("0.0011")
    ),
    labelsByTransactionId = Map(1 -> "Facture fournisseur")
  )

object ClearingAppV21:
  def run(command: V21Command): String = runSafely:
    V21IO.read(command.path).fold(
      RailRenderer.renderError,
      content =>
        RailRenderer.renderReport(
          RailwayEngine.process(
            V21Profiles.clearingMAD,
            hashBoundary(command.simulateHashFailure)
          )(content)
        )
    )

  def runSafely(program: => String): String =
    try program
    catch
      case NonFatal(error) =>
        RailRenderer.renderError(
          TechnicalError.fromThrowable(
            operation = "run-application",
            detail = "erreur inattendue"
          )(error)
        )

  private def hashBoundary(simulateFailure: Boolean): HashBoundary = iban =>
    if simulateFailure && iban.slice(4, 7) == "BOA" then
      Left(
        TechnicalError(
          operation = "hash-iban",
          causeType = "SimulatedHashFailure",
          detail = "panne de hash simulée"
        )
      )
    else
      SecurityUtils.hashIbanTry(iban).toEither.left.map:
        TechnicalError.fromThrowable(
          operation = "hash-iban",
          detail = "hachage impossible"
        )

object V21Reporter:
  def print(report: String): Unit = println(report)

@main def runClearingAppV21(args: String*): Unit =
  val rendered = V21Cli.parse(args.toList).fold(identity, ClearingAppV21.run)
  V21Reporter.print(rendered)
