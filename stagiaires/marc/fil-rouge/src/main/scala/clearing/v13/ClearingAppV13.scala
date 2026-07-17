package clearing.v13

import clearing.model.*
import clearing.v11.{ClearingAppV11, DetailedErrorReporter, V11Result}
import clearing.v12.BusinessReporter
import java.net.http.HttpClient
import java.time.Clock
import java.util.UUID
import scala.io.Source
import scala.util.{Failure, Success, Using}

enum V13Command:
  case RunFile(path: String, simulateNetworkFailure: Boolean)

object V13Cli:
  val usage: String =
    "Usage : run [chemin.csv] | run --network-failure [chemin.csv]"

  def parse(arguments: List[String]): Option[V13Command] =
    arguments match
      case Nil =>
        Some(V13Command.RunFile("transactions-v13.csv", false))
      case path :: Nil if !path.startsWith("--") =>
        Some(V13Command.RunFile(path, false))
      case "--network-failure" :: Nil =>
        Some(V13Command.RunFile("transactions-v13.csv", true))
      case "--network-failure" :: path :: Nil if !path.startsWith("--") =>
        Some(V13Command.RunFile(path, true))
      case _ => None

case class V13Result(
  validation: V11Result,
  clearing: ConnectedClearingResult
)

object ClearingAppV13:
  private def displayAmount(amount: BigDecimal): String =
    amount.bigDecimal.stripTrailingZeros().toPlainString

  private def fromValidation(
    validation: V11Result,
    provider: ExchangeRateProvider,
    repository: BankRepository,
    clock: Clock,
    uuidSupplier: () => UUID
  ): V13Result =
    val service = ClearingService(SpringTransactionValidator(repository))
    V13Result(
      validation,
      service.process(
        validation.successfulTransactions,
        provider,
        clock,
        uuidSupplier
      )
    )

  def processLines(
    lines: List[String],
    provider: ExchangeRateProvider,
    repository: BankRepository = BankRepository(),
    clock: Clock = Clock.systemUTC(),
    uuidSupplier: () => UUID = () => UUID.randomUUID()
  ): V13Result =
    fromValidation(
      ClearingAppV11.processLines(lines),
      provider,
      repository,
      clock,
      uuidSupplier
    )

  def renderReport(result: V13Result): String =
    val validation = result.validation
    val clearing = result.clearing
    val repositoryAccepted = clearing.acceptedBankTransactions.size

    val rejectedLines = validation.rejectedLines.map: rejected =>
      s"  - Ligne ${rejected.lineNumber} rejetée (${rejected.errors.size} erreur(s))"
    val fileErrors = validation.fileErrors.map: error =>
      s"[FILE ERROR] ${DetailedErrorReporter.detailedReport(error)}"
    val repositoryErrors = clearing.repositoryRejected.map: rejected =>
      s"  - Transaction ${rejected.transaction.id} : banques absentes ${rejected.missingCodes.toList.sorted.mkString(",")}"
    val rateLines = List(Currency.MAD, Currency.EUR, Currency.USD)
      .flatMap(currency =>
        clearing.rates.get(currency).map(rate =>
          s"$currency=${rate.bigDecimal.toPlainString}"
        )
      )
    val missingRateLines = clearing.missingRates.map: missing =>
      s"[FX ERROR] Taux ${missing.currency} indisponible — transactions ${missing.transactionIds.mkString(",")} ignorées"
    val secureLines = clearing.secureBatch.transactions.map: log =>
      s"  - TX ${log.transactionId} | source=${log.sourceIbanHash} | destination=${log.destinationIbanHash} | ${displayAmount(log.amount)} MAD | ${log.formattedAt}"

    (List(
      "=== CLEARING ENGINE v1.3 ===",
      s"[INPUT] ${validation.receivedCount} lignes reçues",
      s"[VALIDATION v1.1] ${validation.successfulTransactions.size} acceptées | ${validation.rejectedLines.size} rejetées | ${validation.warnings.size} avertissement(s)"
    ) ++ rejectedLines ++ fileErrors ++ List(
      s"[REPOSITORY] $repositoryAccepted acceptées | ${clearing.repositoryRejected.size} rejetée(s)"
    ) ++ repositoryErrors ++ List(
      s"[FX] ${rateLines.mkString(", ")}",
      s"[FX] ${clearing.convertedTransactions.size} transaction(s) convertie(s) en MAD"
    ) ++ missingRateLines ++ List(
      s"[SECURE] Batch ${clearing.secureBatch.id} — ${clearing.secureBatch.processedAt}"
    ) ++ secureLines ++ List(
      "[BILATERAL] Règlements nets :",
      BusinessReporter.renderBilateral(clearing.bilateralSettlements),
      "[MULTILATERAL] Rapport de règlement :",
      BusinessReporter.renderSettlement(clearing.positions),
      s"[REPORT] Solde net global : ${displayAmount(clearing.positions.values.sum)} MAD"
    )).mkString(System.lineSeparator())

  def runFile(
    path: String,
    provider: ExchangeRateProvider,
    clock: Clock = Clock.systemUTC(),
    uuidSupplier: () => UUID = () => UUID.randomUUID()
  ): V13Result =
    val validation = Using(Source.fromFile(path))(_.getLines().toList) match
      case Success(lines) => ClearingAppV11.processLines(lines)
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

    val result = fromValidation(
      validation,
      provider,
      BankRepository(),
      clock,
      uuidSupplier
    )
    println(renderReport(result))
    result

  def runDemo(
    path: String,
    simulateNetworkFailure: Boolean
  ): V13Result =
    Using.resource(LocalExchangeRateServer.start()): server =>
      val httpProvider = HttpExchangeRateService(
        server.baseUri,
        HttpClient.newHttpClient()
      )
      val provider =
        if simulateNetworkFailure then
          new ExchangeRateProvider:
            def fetchRate(currency: Currency): Option[BigDecimal] =
              Option.unless(currency == Currency.USD)(currency)
                .flatMap(httpProvider.fetchRate)
        else httpProvider

      runFile(path, provider)

@main def runClearingAppV13(arguments: String*): Unit =
  V13Cli.parse(arguments.toList) match
    case Some(V13Command.RunFile(path, simulateNetworkFailure)) =>
      ClearingAppV13.runDemo(path, simulateNetworkFailure)
    case None => Console.err.println(V13Cli.usage)
