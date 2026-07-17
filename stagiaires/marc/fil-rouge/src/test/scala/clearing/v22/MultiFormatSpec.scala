package clearing.v22

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.DomainTypes.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class MultiFormatSpec extends AnyFlatSpec with Matchers:
  private val ath = BankCode.unsafe("ATH")
  private val cih = BankCode.unsafe("CIH")
  private val rawIban = "MA64ATH00000000000000000"
  private val sourceHash = IbanHash.from("a" * 64).toOption.get
  private val destinationHash = IbanHash.from("b" * 64).toOption.get

  private val result = ClearingResult(
    referenceCurrency = Currency.MAD,
    transactions = List(
      PreparedTransaction(
        id = 1,
        sender = ath,
        receiver = cih,
        settlementAmount = Money(BigDecimal("100.00")),
        transactionType = TransactionType.Transfer,
        status = TransactionStatus.Validated,
        referenceCurrency = Currency.MAD,
        fee = Money(BigDecimal("0.10")),
        sourceIbanHash = sourceHash,
        destinationIbanHash = destinationHash,
        label = "Facture \"A&B\", urgente\nà traiter",
        warnings = List(V22Warning.MissingLabel("NON RENSEIGNE"))
      )
    ),
    rejections = List(
      Rejection(2, Some(2), "VALIDATION", "mauvais <champ>, \"x\"")
    ),
    positions = Map(
      ath -> Money(BigDecimal("-100.00")),
      cih -> Money(BigDecimal("100.00"))
    ),
    feesByBank = Map(ath -> Money(BigDecimal("0.10"))),
    statistics = ErrorStatistics(0, 1, 0, 0, 1)
  )

  "ClearingResult multi-format" should "rendre un JSON déterministe et échappé" in:
    import JsonSerializers.given

    val first = ExportEngine.`export`(result)
    val second = ExportEngine.`export`(result)

    first shouldBe second
    first should startWith("{\"referenceCurrency\":\"MAD\"")
    first should include("\"successes\":1")
    first should include("\"rejections\":1")
    first should include("Facture \\\"A&B\\\", urgente\\nà traiter")
    first should include("\"warnings\":[\"LABEL_MANQUANT\"]")
    first should include("\"global\":\"0.00\"")
    first should not include rawIban

  it should "rendre un CSV valide avec une ligne globale" in:
    import CsvSerializers.given

    val rendered = ExportEngine.`export`(result)

    rendered should startWith("RESULT,MAD,1,1")
    rendered should include("\"Facture \"\"A&B\"\", urgente\nà traiter\"")
    rendered should include("REJECTION,2,2,VALIDATION")
    rendered should include("POSITION,ATH,-100.00")
    rendered should include("POSITION,CIH,100.00")
    rendered should include("GLOBAL,0.00")
    rendered should not include rawIban

  it should "rendre un XML déterministe avec les caractères réservés échappés" in:
    import XmlSerializers.given

    val rendered = ExportEngine.`export`(result)

    rendered should startWith(
      "<clearingResult referenceCurrency=\"MAD\" successes=\"1\" rejections=\"1\">"
    )
    rendered should include("Facture &quot;A&amp;B&quot;, urgente")
    rendered should include("mauvais &lt;champ&gt;, &quot;x&quot;")
    rendered should include("<warning>LABEL_MANQUANT</warning>")
    rendered should include("<global>0.00</global>")
    rendered should not include rawIban

  "MultiExportDemo" should "construire trois transactions une fois et changer seulement le contexte" in:
    val transactions = MultiExportDemo.sampleTransactions
    val rendered = MultiExportDemo.renderAll(transactions)

    transactions should have size 3
    rendered.keySet shouldBe Set(
      OutputFormat.Json,
      OutputFormat.Csv,
      OutputFormat.Xml
    )
    rendered(OutputFormat.Json).linesIterator should have size 3
    rendered(OutputFormat.Csv).linesIterator should have size 3
    rendered(OutputFormat.Xml).linesIterator should have size 3
    rendered.values.mkString should not include rawIban

  "ExportEngine XML" should "exposer les deux adaptateurs nommés par le TP" in:
    import XmlSerializers.given

    val transaction = MultiExportDemo.sampleTransactions.head
    ExportEngine.exportItemXml(transaction) should include("<sender>ATH</sender>")
    ExportEngine.exportBatchXml(List(transaction)).linesIterator should have size 1
