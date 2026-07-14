package clearing.v22

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.DateSyntax.*
import clearing.v22.DomainTypes.{isPositive as _, *}
import clearing.v22.PrimitiveSyntax.*
import clearing.v22.SerializationSyntax.*
import clearing.v22.TransactionSyntax.*
import java.time.LocalDateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class SyntaxSpec extends AnyFlatSpec with Matchers:
  private val transaction = Transaction(
    id = 1,
    sender = BankCode.unsafe("ATH"),
    receiver = BankCode.unsafe("CIH"),
    sourceIban = Iban.unsafe("MA64ATH00000000000000000"),
    destinationIban = Iban.unsafe("MA64CIH00000000000000000"),
    amount = Money(BigDecimal(100)),
    transactionType = TransactionType.Transfer,
    currency = Currency.MAD,
    status = TransactionStatus.Validated
  )

  "PrimitiveSyntax" should "enrichir BigDecimal et String sans modifier leurs classes" in:
    BigDecimal("0.01").isPositive shouldBe true
    BigDecimal(0).isPositive shouldBe false
    "ATH".isValidBankCode shouldBe true
    "ath".isValidBankCode shouldBe false
    "SGMB".isValidBankCode shouldBe false

  "DateSyntax" should "produire le préfixe court demandé par le TP" in:
    val now = LocalDateTime.of(2026, 7, 14, 9, 30)

    now.toSimpleFormat shouldBe "14/07"
    s"log_${now.toSimpleFormat}.txt" shouldBe "log_14/07.txt"

  "TransactionSyntax" should "rendre le domaine lisible" in:
    transaction.isValid shouldBe true
    transaction.toSummary shouldBe "#1 ATH -> CIH : 100.00 DH"
    transaction.copy(amount = Money.zero).isValid shouldBe false
    transaction.copy(receiver = transaction.sender).isValid shouldBe false
    transaction
      .copy(destinationIban = transaction.sourceIban)
      .isValid shouldBe false

  "SerializationSyntax" should "déléguer chaque format à sa type class" in:
    import CsvSerializers.given
    import JsonSerializers.given
    import XmlSerializers.given

    transaction.toCsv shouldBe
      "1,ATH,CIH,100.00,VIR,MAD,Validated"
    transaction.toJson shouldBe
      """{"id":1,"sender":"ATH","receiver":"CIH","amount":"100.00","type":"VIR","currency":"MAD","status":"Validated"}"""
    transaction.toXml should include("<sender>ATH</sender>")
    transaction.toXml should include("<receiver>CIH</receiver>")

  it should "fonctionner dans map pour plusieurs types du domaine" in:
    import JsonSerializers.given

    val banks = List(
      Bank(BankCode.unsafe("ATH"), "Atlas Transfer Hub"),
      Bank(BankCode.unsafe("CIH"), "Crédit Immobilier")
    )

    List(transaction, transaction.copy(id = 2)).map(_.toJson) should have size 2
    banks.map(_.toJson) shouldBe List(
      """{"code":"ATH","name":"Atlas Transfer Hub"}""",
      """{"code":"CIH","name":"Crédit Immobilier"}"""
    )
