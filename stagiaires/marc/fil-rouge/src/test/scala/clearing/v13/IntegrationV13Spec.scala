package clearing.v13

import clearing.model.*
import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import java.time.{Clock, Instant, ZoneOffset}
import java.util.UUID
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV13Spec extends AnyFlatSpec with Matchers:
  private val fixedClock =
    Clock.fixed(Instant.parse("2026-07-14T08:30:00Z"), ZoneOffset.UTC)
  private val fixedUuid = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")

  private def resourcePath(name: String): String =
    val resource = Option(getClass.getResource(s"/$name"))
      .getOrElse(fail(s"Ressource $name absente"))
    Paths.get(resource.toURI).toString

  private def resourceLines(name: String): List[String] =
    val source = scala.io.Source.fromFile(resourcePath(name))
    try source.getLines().toList
    finally source.close()

  private def provider(
    rates: Map[Currency, BigDecimal]
  ): ExchangeRateProvider = new ExchangeRateProvider:
    def fetchRate(currency: Currency): Option[BigDecimal] = rates.get(currency)

  private def repositoryWithoutSgmb: BankRepository =
    BankRepository(
      java.util.List.of(
        Bank("ATH", "Attijariwafa Bank"),
        Bank("CIH", "CIH Bank"),
        Bank("BOA", "Bank of Africa"),
        Bank("BMCE", "BMCE Capital")
      )
    )

  "V13Cli.parse" should "accepter le fichier par défaut, un chemin et le mode panne" in:
    V13Cli.parse(Nil) shouldBe Some(
      V13Command.RunFile("transactions-v13.csv", simulateNetworkFailure = false)
    )
    V13Cli.parse(List("flux.csv")) shouldBe Some(
      V13Command.RunFile("flux.csv", simulateNetworkFailure = false)
    )
    V13Cli.parse(List("--network-failure")) shouldBe Some(
      V13Command.RunFile("transactions-v13.csv", simulateNetworkFailure = true)
    )
    V13Cli.parse(List("--network-failure", "flux.csv")) shouldBe Some(
      V13Command.RunFile("flux.csv", simulateNetworkFailure = true)
    )

  it should "refuser les arguments ambigus" in:
    List(
      List("--network-failure", "a.csv", "b.csv"),
      List("--unknown"),
      List("a.csv", "b.csv")
    ).foreach(arguments => V13Cli.parse(arguments) shouldBe None)

  "ClearingAppV13.processLines" should "connecter validation, repository, taux, sécurité et netting" in:
    val result = ClearingAppV13.processLines(
      resourceLines("transactions-s8.csv"),
      provider(LocalExchangeRateServer.DefaultRates),
      repositoryWithoutSgmb,
      fixedClock,
      () => fixedUuid
    )

    result.validation.receivedCount shouldBe 7
    result.validation.successfulTransactions.map(_.id) shouldBe
      List(1, 2, 3, 4, 7)
    result.validation.rejectedLines.map(_.lineNumber) shouldBe List(5, 6)
    result.validation.warnings.map(_.transactionId) shouldBe List(7)
    result.clearing.repositoryRejected.map(_.transaction.id) shouldBe List(4)
    result.clearing.convertedTransactions.map(_.id) shouldBe List(1, 2, 3, 7)
    result.clearing.rates shouldBe LocalExchangeRateServer.DefaultRates
    result.clearing.secureBatch.id shouldBe fixedUuid
    result.clearing.secureBatch.processedAt.toString shouldBe
      "2026-07-14T09:30+01:00[Africa/Casablanca]"
    result.clearing.secureBatch.transactions.head shouldBe SecureTransactionLog(
      transactionId = 1,
      sourceIbanHash =
        "f528910ebd6e1661f465f3538e4e0b3f2e203e8d29fe4412e6c1c350f7fdecfc",
      destinationIbanHash =
        "605fe8aa83ca713a218d84632d580832f1e36ce2da779f81199e372cbc5359a9",
      amount = BigDecimal("100.00"),
      formattedAt = "14/07/2026 09:30:00"
    )
    result.clearing.positions.values.sum shouldBe BigDecimal(0)
    result.clearing.bilateralSettlements should have size 2

    val report = ClearingAppV13.renderReport(result)
    report should include("=== CLEARING ENGINE v1.3 ===")
    report should include("[VALIDATION v1.1] 5 acceptées | 2 rejetées")
    report should include("[REPOSITORY] 4 acceptées | 1 rejetée")
    report should include("[FX] MAD=1, EUR=10.80, USD=9.90")
    report should include("[SECURE] Batch aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
    report should include(result.clearing.secureBatch.transactions.head.sourceIbanHash)
    report should include("[REPORT] Solde net global : 0 MAD")
    report should not include "MA64ATH00000000000000000"
    report should not include "MA64CIH00000000000000000"

  it should "isoler une panne USD sans interrompre MAD et EUR" in:
    val ratesWithoutUsd = LocalExchangeRateServer.DefaultRates - Currency.USD
    val result = ClearingAppV13.processLines(
      resourceLines("transactions-s8.csv"),
      provider(ratesWithoutUsd),
      repositoryWithoutSgmb,
      fixedClock,
      () => fixedUuid
    )

    result.clearing.convertedTransactions.map(_.id) shouldBe List(1, 2, 7)
    result.clearing.missingRates shouldBe List(
      MissingRate(Currency.USD, List(3))
    )
    result.clearing.positions.values.sum shouldBe BigDecimal(0)
    ClearingAppV13.renderReport(result) should include(
      "[FX ERROR] Taux USD indisponible — transactions 3 ignorées"
    )

  "ClearingAppV13.runFile" should "transformer un chemin illisible en résultat explicite" in:
    val output = ByteArrayOutputStream()
    val result = Console.withOut(output):
      ClearingAppV13.runFile(
        "/fichier/s8/introuvable.csv",
        provider(LocalExchangeRateServer.DefaultRates),
        fixedClock,
        () => fixedUuid
      )

    result.validation.fileErrors shouldBe List(
      FileReadFailure(
        "/fichier/s8/introuvable.csv",
        "fichier introuvable ou illisible"
      )
    )
    result.clearing.convertedTransactions shouldBe empty
    output.toString("UTF-8") should include("Erreur système")

  "runClearingAppV13" should "afficher l'usage sans traiter une commande invalide" in:
    val standardOutput = ByteArrayOutputStream()
    val errorOutput = ByteArrayOutputStream()

    Console.withOut(standardOutput):
      Console.withErr(errorOutput):
        runClearingAppV13("--unknown")

    standardOutput.toString("UTF-8") shouldBe empty
    errorOutput.toString("UTF-8") should include(V13Cli.usage)
