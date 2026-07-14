package clearing.v22

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.DomainTypes.*
import java.time.LocalDateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class SerializerSpec extends AnyFlatSpec with Matchers:
  private val sourceIban = Iban.unsafe("MA64ATH00000000000000000")
  private val destinationIban = Iban.unsafe("MA64CIH00000000000000000")
  private val bank = Bank(BankCode.unsafe("ATH"), "Atlas Transfer Hub")
  private val transaction = Transaction(
    id = 1,
    sender = BankCode.unsafe("ATH"),
    receiver = BankCode.unsafe("CIH"),
    sourceIban = sourceIban,
    destinationIban = destinationIban,
    amount = Money(BigDecimal(100)),
    transactionType = TransactionType.Transfer,
    currency = Currency.MAD,
    status = TransactionStatus.Validated
  )
  private val expectedCsv = "1,ATH,CIH,100.00,VIR,MAD,Validated"
  private val expectedJson =
    """{"id":1,"sender":"ATH","receiver":"CIH","amount":"100.00","type":"VIR","currency":"MAD","status":"Validated"}"""

  "ManualSerializers" should "sérialiser explicitement le domaine et un type Java" in:
    ManualSerializers.transactionCsv.toText(transaction) shouldBe expectedCsv
    ManualSerializers.bankCsv.toText(bank) shouldBe
      "ATH,Atlas Transfer Hub"
    ExportEngine.exportWith(
      transaction,
      ManualSerializers.transactionJson
    ) shouldBe expectedJson
    ManualSerializers.localDateTime.toText(
      LocalDateTime.of(2026, 7, 14, 9, 30)
    ) shouldBe "2026-07-14T09:30:00"

  it should "ne jamais exporter les IBAN bruts d'une transaction" in:
    val rendered = List(
      ManualSerializers.transactionCsv.toText(transaction),
      ManualSerializers.transactionJson.toText(transaction)
    ).mkString

    rendered should not include sourceIban.value
    rendered should not include destinationIban.value

  "ExportEngine.`export`" should "choisir le CSV avec un given local" in:
    import CsvSerializers.given

    ExportEngine.`export`(transaction) shouldBe expectedCsv
    ExportEngine.`export`(bank) shouldBe "ATH,Atlas Transfer Hub"
    ExportEngine.exportBatch(
      List(transaction, transaction.copy(id = 2))
    ) shouldBe s"$expectedCsv\n${expectedCsv.replace("1,", "2,")}"

  it should "choisir le JSON sans changer la fonction générique" in:
    import JsonSerializers.given

    ExportEngine.`export`(transaction) shouldBe expectedJson
    ExportEngine.`export`(bank) shouldBe
      """{"code":"ATH","name":"Atlas Transfer Hub"}"""
    ExportEngine.exportBatch(
      List(transaction, transaction.copy(id = 2))
    ) shouldBe s"$expectedJson\n${expectedJson.replace("\"id\":1", "\"id\":2")}"

  "ClearingResult serializers" should "supporter les deux contextes" in:
    val result = ClearingResult(
      referenceCurrency = Currency.MAD,
      transactions = Nil,
      rejections = Nil,
      positions = Map(BankCode.unsafe("ATH") -> Money(BigDecimal("10.00"))),
      feesByBank = Map.empty,
      statistics = ErrorStatistics(0, 0, 0, 0, 0)
    )

    val csv =
      import CsvSerializers.given
      ExportEngine.`export`(result)
    val json =
      import JsonSerializers.given
      ExportEngine.`export`(result)

    csv should include("POSITION,ATH,10.00")
    json should include("\"positions\":{\"ATH\":\"10.00\"}")
