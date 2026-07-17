package clearing.v22

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.DomainTypes.*
import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TypedRailwayEngineSpec extends AnyFlatSpec with Matchers:
  private val ath = BankCode.unsafe("ATH")
  private val cih = BankCode.unsafe("CIH")
  private val boa = BankCode.unsafe("BOA")
  private val athIban = Iban.unsafe("MA64ATH00000000000000000")
  private val cihIban = Iban.unsafe("MA64CIH00000000000000000")
  private val boaIban = Iban.unsafe("MA64BOA00000000000000000")

  private val config = V22Config(
    referenceCurrency = Currency.MAD,
    knownBanks = Set(ath, cih, boa),
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
      boa -> BigDecimal("0.0015")
    ),
    labelsByTransactionId = Map(1 -> "Facture fournisseur")
  )

  private val stableHash: HashBoundary = iban =>
    Right(hashFor(iban))

  private def hashFor(iban: Iban): IbanHash =
    val digit = iban.bankSegment.head.toLower.toString
    IbanHash.from(digit * 64).toOption.get

  private def csv(
    id: String = "1",
    sender: String = "ATH",
    receiver: String = "CIH",
    sourceIban: String = athIban.value,
    destinationIban: String = cihIban.value,
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

  "TypedRailwayEngine.processLine" should "composer tout le rail avec les types opaques" in:
    val result = TypedRailwayEngine.processLine(config, Set.empty, stableHash)(
      NumberedLine(1, csv())
    )

    val projected = result.map: transaction =>
      (
        transaction.id,
        transaction.sender.value,
        transaction.receiver.value,
        transaction.settlementAmount.value,
        transaction.fee.value,
        transaction.status,
        transaction.sourceIbanHash.value,
        transaction.destinationIbanHash.value,
        transaction.label,
        transaction.warnings
      )

    projected shouldBe Right(
      (
        1,
        "ATH",
        "CIH",
        BigDecimal("100.00"),
        BigDecimal("0.10"),
        TransactionStatus.Validated,
        "a" * 64,
        "c" * 64,
        "Facture fournisseur",
        Nil
      )
    )

  it should "court-circuiter parsing, validation, change et frais avant le hash" in:
    val calls = new AtomicInteger(0)
    val countingHash: HashBoundary = iban =>
      calls.incrementAndGet()
      Right(hashFor(iban))
    val noAthFee = config.copy(feeRates = config.feeRates - ath)

    val results = List(
      TypedRailwayEngine.processLine(config, Set.empty, countingHash)(
        NumberedLine(1, "pas,assez,de,colonnes")
      ),
      TypedRailwayEngine.processLine(config, Set.empty, countingHash)(
        NumberedLine(
          2,
          csv(receiver = "ATH", destinationIban = athIban.value)
        )
      ),
      TypedRailwayEngine.processLine(config, Set.empty, countingHash)(
        NumberedLine(
          3,
          csv(
            sender = "BOA",
            sourceIban = boaIban.value,
            currency = "USD"
          )
        )
      ),
      TypedRailwayEngine.processLine(noAthFee, Set.empty, countingHash)(
        NumberedLine(4, csv())
      )
    )

    all(results.map(_.isLeft)) shouldBe true
    results(2) shouldBe Left(
      V22ConfigurationError(3, 1, "FX_RATE_MISSING", "taux USD absent")
    )
    results(3) shouldBe Left(
      V22ConfigurationError(4, 1, "FEE_RATE_MISSING", "frais ATH absents")
    )
    calls.get shouldBe 0

  it should "enrichir une panne du premier hash et arrêter le second" in:
    val calls = new AtomicInteger(0)
    val failingHash: HashBoundary = _ =>
      calls.incrementAndGet()
      Left(HashFailure.Unavailable)

    TypedRailwayEngine.processLine(config, Set.empty, failingHash)(
      NumberedLine(8, csv())
    ) shouldBe Left(
      V22TechnicalError(
        8,
        Some(1),
        "hash-iban",
        "HashProviderFailure",
        "hachage impossible"
      )
    )
    calls.get shouldBe 1

  "TypedRailwayEngine.process" should "netter seulement les succès et garder l'ordre des rejets" in:
    val input = List(
      csv(),
      csv(
        id = "2",
        sender = "CIH",
        receiver = "ATH",
        sourceIban = cihIban.value,
        destinationIban = athIban.value,
        amount = "10",
        currency = "EUR"
      ),
      csv(id = "abc", amount = "cent"),
      csv(id = "4", receiver = "ATH", destinationIban = athIban.value),
      csv(id = "1", amount = "50"),
      csv(
        id = "6",
        sender = "BOA",
        receiver = "ATH",
        sourceIban = boaIban.value,
        destinationIban = athIban.value,
        amount = "5",
        currency = "USD"
      ),
      csv(
        id = "7",
        sender = "CIH",
        receiver = "BOA",
        sourceIban = cihIban.value,
        destinationIban = boaIban.value,
        amount = "20"
      )
    ).mkString("\n")
    val selectiveHash: HashBoundary = iban =>
      if iban == boaIban then
        Left(HashFailure.Unavailable)
      else stableHash(iban)

    val result = TypedRailwayEngine.process(config, selectiveHash)(input)

    result.transactions.map(_.id) shouldBe List(1, 2)
    result.transactions.map(_.referenceCurrency) shouldBe
      List(Currency.MAD, Currency.MAD)
    result.rejections.map(_.lineNumber) shouldBe List(3, 4, 5, 6, 7)
    result.rejections.map(_.code) shouldBe List(
      "PARSE_ID",
      "VALIDATION_TRANSACTION",
      "AM05",
      "FX_RATE_MISSING",
      "TECH_HASH_IBAN"
    )
    result.positions shouldBe Map(
      ath -> Money(BigDecimal("8.00")),
      cih -> Money(BigDecimal("-8.00"))
    )
    result.positions.values.sum shouldBe Money.zero
    result.feesByBank shouldBe Map(
      ath -> Money(BigDecimal("0.10")),
      cih -> Money(BigDecimal("0.22"))
    )
    result.statistics shouldBe ErrorStatistics(1, 1, 2, 1, 1)
    result.transactions(1).warnings shouldBe
      List(V22Warning.MissingLabel("NON RENSEIGNE"))
    result.toString should not include athIban.value
    result.toString should not include cihIban.value
    result.toString should not include boaIban.value

  it should "rester déterministe et gérer un batch vide" in:
    val reports = List.fill(1000)(
      TypedRailwayEngine.process(config, stableHash)(csv())
    )

    reports should contain only reports.head
    TypedRailwayEngine.process(config, stableHash)("") shouldBe
      ClearingResult(
        Currency.MAD,
        Nil,
        Nil,
        Map.empty,
        Map.empty,
        ErrorStatistics(0, 0, 0, 0, 0)
      )
