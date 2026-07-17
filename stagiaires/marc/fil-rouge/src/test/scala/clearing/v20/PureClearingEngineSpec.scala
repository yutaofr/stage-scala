package clearing.v20

import clearing.model.Currency
import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.util.Locale
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class PureClearingEngineSpec extends AnyFlatSpec with Matchers:
  private val sourceAth = "MA64ATH00000000000000000"
  private val sourceCih = "MA64CIH00000000000000000"
  private val sourceBoa = "MA64BOA00000000000000000"
  private val sourceSgmb = "MA64SGMB0000000000000000"

  private val input = List(
    s"1,ATH,CIH,$sourceAth,$sourceCih,100,VIR,MAD",
    s"2,CIH,ATH,$sourceCih,$sourceAth,10,VIR,EUR",
    s"3,BOA,ATH,$sourceBoa,$sourceAth,5,VIR,USD",
    "not,csv",
    s"5,ATH,CIH,$sourceAth,$sourceCih,-1,VIR,MAD",
    s"6,ATH,CIH,$sourceAth,$sourceCih,1000000,VIR,MAD",
    s"7,SGMB,SGMB,$sourceSgmb,$sourceSgmb,10,VIR,MAD"
  ).mkString("\n")

  "PureClearingEngine.splitLines" should "numéroter les lignes sans perdre leur ordre" in:
    PureClearingEngine.splitLines(input).map(_.lineNumber) shouldBe
      (1 to 7).toList
    PureClearingEngine.splitLines("") shouldBe Nil

  "PureClearingEngine.parse" should "séparer les transactions et le CSV malformé" in:
    val parsed = PureClearingEngine.parse(PureClearingEngine.splitLines(input))

    parsed.transactions.map(_.lineNumber) shouldBe List(1, 2, 3, 5, 6, 7)
    parsed.rejections shouldBe List(
      PureRejection(4, None, "CSV_MALFORME")
    )

  "PureClearingEngine.configure" should "produire le rapport MAD attendu" in:
    val report = PureClearingEngine.configure(PureEngineProfiles.clearingMAD)(input)

    report.referenceCurrency shouldBe Currency.MAD
    report.transactions.map(_.id) shouldBe List(1, 2, 3)
    report.transactions.map(_.settlementAmount) shouldBe
      List(BigDecimal("100.00"), BigDecimal("108.00"), BigDecimal("49.50"))
    report.positions shouldBe Map(
      "ATH" -> BigDecimal("57.50"),
      "BOA" -> BigDecimal("-49.50"),
      "CIH" -> BigDecimal("-8.00")
    )
    report.feesByBank shouldBe Map(
      "ATH" -> BigDecimal("0.10"),
      "BOA" -> BigDecimal("0.07"),
      "CIH" -> BigDecimal("0.22")
    )
    report.positions.values.sum shouldBe BigDecimal("0.00")
    report.rejections.map(_.lineNumber) shouldBe List(4, 5, 6, 7)
    report.rejections.map(_.transactionId) shouldBe
      List(None, Some(5), Some(6), Some(7))

  it should "réutiliser le même coeur avec le profil EUR" in:
    val mad = PureClearingEngine.configure(PureEngineProfiles.clearingMAD)(input)
    val eur = PureClearingEngine.configure(PureEngineProfiles.clearingEUR)(input)

    eur.referenceCurrency shouldBe Currency.EUR
    eur.transactions.map(_.id) shouldBe mad.transactions.map(_.id)
    eur.transactions.map(_.settlementAmount) shouldBe
      List(BigDecimal("9.26"), BigDecimal("10.00"), BigDecimal("4.58"))
    eur.positions shouldBe Map(
      "ATH" -> BigDecimal("5.32"),
      "BOA" -> BigDecimal("-4.58"),
      "CIH" -> BigDecimal("-0.74")
    )
    eur.feesByBank shouldBe Map(
      "ATH" -> BigDecimal("0.01"),
      "BOA" -> BigDecimal("0.01"),
      "CIH" -> BigDecimal("0.02")
    )
    eur.positions.values.sum shouldBe BigDecimal("0.00")

  it should "accumuler un taux manquant dans les rejets, à sa place de ligne" in:
    val withoutUsd = PureEngineProfiles.clearingMAD.copy(
      ratesToReference = PureEngineProfiles.clearingMAD.ratesToReference - Currency.USD
    )
    val report = PureClearingEngine.configure(withoutUsd)(input)

    report.transactions.map(_.id) shouldBe List(1, 2)
    report.rejections.map(_.lineNumber) shouldBe List(3, 4, 5, 6, 7)
    report.rejections.head.reason shouldBe "TAUX_MANQUANT:USD"

  it should "conserver le premier ID et rejeter ses occurrences suivantes" in:
    val duplicateInput = List(
      s"21,ATH,CIH,$sourceAth,$sourceCih,100,VIR,MAD",
      s"21,CIH,ATH,$sourceCih,$sourceAth,25,VIR,MAD"
    ).mkString("\n")

    val report = PureClearingEngine.configure(
      PureEngineProfiles.clearingMAD
    )(duplicateInput)

    report.transactions.map(_.id) shouldBe List(21)
    report.rejections shouldBe List(
      PureRejection(2, Some(21), "REGLES_METIER:TRANSACTION_DUPLIQUEE")
    )

  it should "rejeter un taux de frais absent ou négatif" in:
    val withoutAthFee = PureEngineProfiles.clearingMAD.copy(
      feeRates = PureEngineProfiles.clearingMAD.feeRates - "ATH"
    )
    val withNegativeAthFee = PureEngineProfiles.clearingMAD.copy(
      feeRates = PureEngineProfiles.clearingMAD.feeRates.updated(
        "ATH",
        BigDecimal("-0.001")
      )
    )

    val missingReport = PureClearingEngine.configure(withoutAthFee)(input)
    val negativeReport = PureClearingEngine.configure(withNegativeAthFee)(input)

    missingReport.transactions.map(_.id) shouldBe List(2, 3)
    missingReport.rejections.map(_.lineNumber) shouldBe List(1, 4, 5, 6, 7)
    missingReport.rejections.head.reason shouldBe
      "REGLES_METIER:TAUX_FRAIS_MANQUANT:ATH"
    negativeReport.transactions.map(_.id) shouldBe List(2, 3)
    negativeReport.rejections.head.reason shouldBe
      "REGLES_METIER:TAUX_FRAIS_NEGATIF:ATH"

  it should "rester déterministe quand la locale JVM change" in:
    val lowercaseInput =
      s"31,ath,cih,${sourceAth.toLowerCase(Locale.ROOT)},${sourceCih.toLowerCase(Locale.ROOT)},100,vir,mad"
    val previousLocale = Locale.getDefault

    try
      Locale.setDefault(Locale.forLanguageTag("tr"))
      val report = PureClearingEngine.configure(
        PureEngineProfiles.clearingMAD
      )(lowercaseInput)

      report.transactions.map(_.id) shouldBe List(31)
      report.transactions.map(_.sender) shouldBe List("ATH")
      V20Profile.parse("eur") shouldBe Some(V20Profile.EUR)
    finally Locale.setDefault(previousLocale)

  it should "accumuler les frais de plusieurs transactions de la même banque" in:
    val sameSenderInput = List(
      s"41,ATH,CIH,$sourceAth,$sourceCih,100,VIR,MAD",
      s"42,ATH,CIH,$sourceAth,$sourceCih,50,VIR,MAD"
    ).mkString("\n")

    val report = PureClearingEngine.configure(
      PureEngineProfiles.clearingMAD
    )(sameSenderInput)

    report.feesByBank shouldBe Map("ATH" -> BigDecimal("0.15"))

  it should "cumuler la duplication avec l'erreur déjà présente" in:
    val duplicateFailures = List(
      s"51,ATH,CIH,$sourceAth,$sourceCih,100,VIR,MAD",
      s"51,ATH,CIH,$sourceAth,$sourceCih,-1,VIR,MAD",
      s"51,ATH,ATH,$sourceAth,$sourceAth,10,VIR,MAD",
      s"51,ATH,CIH,$sourceAth,$sourceCih,1000000,VIR,MAD"
    ).mkString("\n")

    val report = PureClearingEngine.configure(
      PureEngineProfiles.clearingMAD
    )(duplicateFailures)

    report.transactions.map(_.id) shouldBe List(51)
    report.rejections.map(_.reason) shouldBe List(
      "REGLES_METIER:MONTANT_NON_POSITIF+TRANSACTION_DUPLIQUEE",
      "REGLES_METIER:CHAMP_INVALIDE:DESTINATIONIBAN+TRANSACTION_DUPLIQUEE",
      "REGLES_METIER:LIMITE_DEPASSEE:1000000+TRANSACTION_DUPLIQUEE"
    )

  it should "échouer fermé lorsqu'une limite de type manque" in:
    val withoutTransferLimit = PureEngineProfiles.clearingMAD.copy(
      limits = PureEngineProfiles.clearingMAD.limits -
        clearing.model.TransactionType.Transfer
    )
    val oneTransfer =
      s"61,ATH,CIH,$sourceAth,$sourceCih,100,VIR,MAD"

    val report = PureClearingEngine.configure(withoutTransferLimit)(oneTransfer)

    report.transactions shouldBe empty
    report.rejections shouldBe List(
      PureRejection(1, Some(61), "REGLES_METIER:LIMITE_MANQUANTE:VIR")
    )

  it should "être exactement la composition de ses fonctions d'étape" in:
    val config = PureEngineProfiles.clearingMAD
    val manual = PureClearingEngine.calculate(
      PureClearingEngine.prepare(config)(
        PureClearingEngine.validate(config)(
          PureClearingEngine.parse(PureClearingEngine.splitLines(input))
        )
      )
    )

    PureClearingEngine.configure(config)(input) shouldBe manual

  "PureClearingRenderer.render" should "trier les maps et ne jamais révéler un IBAN" in:
    val report = PureClearingEngine.configure(PureEngineProfiles.clearingMAD)(input)
    val rendered = PureClearingRenderer.render(report)

    rendered should include("REFERENCE|MAD")
    rendered.indexOf("POSITION|ATH") should be < rendered.indexOf("POSITION|BOA")
    rendered.indexOf("POSITION|BOA") should be < rendered.indexOf("POSITION|CIH")
    rendered should include("TRANSACTION|1|")
    rendered should not include sourceAth
    rendered should not include sourceCih
    rendered should not include sourceBoa

  it should "produire mille fois le même objet et les mêmes octets sans afficher" in:
    val output = new ByteArrayOutputStream()
    val reports = Console.withOut(
      new PrintStream(output, true, StandardCharsets.UTF_8)
    ):
      List.fill(1000)(
        PureClearingEngine.configure(PureEngineProfiles.clearingMAD)(input)
      )
    val firstReport = reports.head
    val firstBytes = PureClearingRenderer
      .render(firstReport)
      .getBytes(StandardCharsets.UTF_8)

    reports should contain only firstReport
    reports.foreach: report =>
      PureClearingRenderer
        .render(report)
        .getBytes(StandardCharsets.UTF_8) shouldBe firstBytes
    output.toString(StandardCharsets.UTF_8) shouldBe empty
