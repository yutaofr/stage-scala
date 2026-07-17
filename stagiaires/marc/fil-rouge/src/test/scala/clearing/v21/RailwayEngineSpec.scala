package clearing.v21

import clearing.model.*
import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class RailwayEngineSpec extends AnyFlatSpec with Matchers:
  private val athIban = "MA64ATH00000000000000000"
  private val cihIban = "MA64CIH00000000000000000"
  private val boaIban = "MA64BOA00000000000000000"

  private val config = V21Config(
    referenceCurrency = Currency.MAD,
    knownBanks = Set("ATH", "CIH", "BOA"),
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
      "BOA" -> BigDecimal("0.0015")
    ),
    labelsByTransactionId = Map(1 -> "Facture fournisseur")
  )

  private val stableHash: HashBoundary = iban =>
    Right(s"HASH-${iban.slice(4, 7)}")

  private def csv(
    id: String = "1",
    sender: String = "ATH",
    receiver: String = "CIH",
    sourceIban: String = athIban,
    destinationIban: String = cihIban,
    amount: String = "100",
    currency: String = "MAD"
  ): String =
    List(
      id,
      sender,
      receiver,
      sourceIban,
      destinationIban,
      amount,
      "VIR",
      currency
    ).mkString(",")

  "RailwayEngine.processLine" should "composer toutes les étapes dans un for-yield" in:
    val result = RailwayEngine.processLine(config, Set.empty, stableHash)(
      NumberedLine(1, csv())
    )

    result.map(success =>
      (
        success.lineNumber,
        success.prepared.id,
        success.prepared.settlementAmount,
        success.prepared.fee,
        success.prepared.sourceIbanHash,
        success.prepared.destinationIbanHash,
        success.label,
        success.warnings
      )
    ) shouldBe Right(
      (
        1,
        1,
        BigDecimal("100.00"),
        BigDecimal("0.10"),
        "HASH-ATH",
        "HASH-CIH",
        "Facture fournisseur",
        Nil
      )
    )

  it should "court-circuiter avant le hash sur parsing, validation, forex et frais" in:
    val hashCalls = new AtomicInteger(0)
    val countingHash: HashBoundary = iban =>
      hashCalls.incrementAndGet()
      Right(iban)
    val noUsd = config
    val noAthFee = config.copy(feeRates = config.feeRates - "ATH")

    val results = List(
      RailwayEngine.processLine(config, Set.empty, countingHash)(
        NumberedLine(1, "pas,assez,de,colonnes")
      ),
      RailwayEngine.processLine(config, Set.empty, countingHash)(
        NumberedLine(2, csv(receiver = "ATH", destinationIban = athIban))
      ),
      RailwayEngine.processLine(noUsd, Set.empty, countingHash)(
        NumberedLine(
          3,
          csv(
            sender = "BOA",
            sourceIban = boaIban,
            currency = "USD"
          )
        )
      ),
      RailwayEngine.processLine(noAthFee, Set.empty, countingHash)(
        NumberedLine(4, csv())
      )
    )

    all(results.map(_.isLeft)) shouldBe true
    results(2) shouldBe Left(
      ConfigurationError(3, 1, "FX_RATE_MISSING", "taux USD absent")
    )
    results(3) shouldBe Left(
      ConfigurationError(4, 1, "FEE_RATE_MISSING", "frais ATH absents")
    )
    hashCalls.get() shouldBe 0

  it should "transformer une panne de hash en erreur technique et arrêter le second hash" in:
    val hashCalls = new AtomicInteger(0)
    val failingHash: HashBoundary = _ =>
      hashCalls.incrementAndGet()
      Left(TechnicalError("hash-iban", "ProviderException", "indisponible"))

    RailwayEngine.processLine(config, Set.empty, failingHash)(
      NumberedLine(1, csv())
    ) shouldBe Left(
      TechnicalError("hash-iban", "ProviderException", "indisponible")
    )
    hashCalls.get() shouldBe 1

  "RailwayEngine.process" should "partitionner une fois et netter seulement les succès" in:
    val input = List(
      csv(),
      csv(
        id = "2",
        sender = "CIH",
        receiver = "ATH",
        sourceIban = cihIban,
        destinationIban = athIban,
        amount = "10",
        currency = "EUR"
      ),
      csv(id = "abc", amount = "cent"),
      csv(id = "4", receiver = "ATH", destinationIban = athIban),
      csv(id = "1", amount = "50"),
      csv(
        id = "6",
        sender = "BOA",
        receiver = "ATH",
        sourceIban = boaIban,
        destinationIban = athIban,
        amount = "5",
        currency = "USD"
      ),
      csv(
        id = "7",
        sender = "CIH",
        receiver = "BOA",
        sourceIban = cihIban,
        destinationIban = boaIban,
        amount = "20"
      )
    ).mkString("\n")
    val selectiveHash: HashBoundary = iban =>
      if iban == boaIban then
        Left(TechnicalError("hash-iban", "ProviderException", "indisponible"))
      else stableHash(iban)

    val report = RailwayEngine.process(config, selectiveHash)(input)

    report.lineResults.map(_.lineNumber) shouldBe (1 to 7).toList
    report.successes.map(_.prepared.id) shouldBe List(1, 2)
    report.successes.map(_.prepared.referenceCurrency) shouldBe
      List(Currency.MAD, Currency.MAD)
    report.errors should have size 5
    report.statistics shouldBe ErrorStatistics(
      parsing = 1,
      validation = 1,
      business = 2,
      technical = 1,
      warnings = 1
    )
    report.positions shouldBe Map(
      "ATH" -> BigDecimal("8.00"),
      "CIH" -> BigDecimal("-8.00")
    )
    report.positions.values.sum shouldBe BigDecimal("0.00")
    report.feesByBank shouldBe Map(
      "ATH" -> BigDecimal("0.10"),
      "CIH" -> BigDecimal("0.22")
    )
    report.successes(1).warnings shouldBe
      List(LightWarning.MissingLabel("NON RENSEIGNE"))
    report.toString should not include athIban
    report.toString should not include cihIban
    report.toString should not include boaIban

  it should "conserver le premier identifiant accepté et l'ordre des rails" in:
    val input = List(csv(), csv(id = "1", amount = "50")).mkString("\n")

    val report = RailwayEngine.process(config, stableHash)(input)

    report.lineResults.map(_.result.isRight) shouldBe List(true, false)
    report.errors shouldBe List(
      Iso20022Rejection(Iso20022Code.AM05, transactionId = 1)
    )

  it should "être déterministe et gérer un batch vide" in:
    val input = csv()
    val reports = List.fill(1000)(
      RailwayEngine.process(config, stableHash)(input)
    )

    reports should contain only reports.head
    RailwayEngine.process(config, stableHash)("") shouldBe V21Report(
      referenceCurrency = Currency.MAD,
      lineResults = Nil,
      successes = Nil,
      errors = Nil,
      positions = Map.empty,
      feesByBank = Map.empty,
      statistics = ErrorStatistics(0, 0, 0, 0, 0)
    )
