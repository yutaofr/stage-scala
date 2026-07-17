package clearing.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class DomainSpec extends AnyFlatSpec with Matchers:
  private val transfer = Transaction(
    id = 1,
    sender = "ATH",
    receiver = "CIH",
    amount = BigDecimal("50000"),
    transactionType = TransactionType.Transfer,
    status = TransactionStatus.Pending
  )

  "Bank" should "fournir l'égalité structurelle et copy" in:
    val bank = Bank("ATH", "Attijariwafa Bank")
    bank shouldBe Bank("ATH", "Attijariwafa Bank")
    bank.copy(name = "Attijariwafa") shouldBe Bank("ATH", "Attijariwafa")

  "Transaction" should "exposer des champs nommés et une copie typée" in:
    transfer.sender shouldBe "ATH"
    transfer.receiver shouldBe "CIH"
    transfer.copy(status = TransactionStatus.Validated).status shouldBe
      TransactionStatus.Validated

  it should "considérer seulement un montant strictement supérieur à 50000 comme élevé" in:
    transfer.isHighValue shouldBe false
    transfer.copy(amount = BigDecimal("50000.01")).isHighValue shouldBe true

  "Account" should "lier un IBAN à une banque et un solde" in:
    Account("MA6400000000000000000001", Bank("ATH", "Attijariwafa"), BigDecimal("42"))
      .balance shouldBe BigDecimal("42")

  "TransactionStatus" should "énumérer tous les états traités en semaine 5" in:
    TransactionStatus.values.toList shouldBe List(
      TransactionStatus.Pending,
      TransactionStatus.Validated,
      TransactionStatus.Rejected,
      TransactionStatus.Suspicious
    )

  it should "empêcher la construction avec un statut String arbitraire" in:
    assertDoesNotCompile(
      """
        clearing.model.Transaction(
          1,
          "ATH",
          "CIH",
          BigDecimal("10"),
          clearing.model.TransactionType.Transfer,
          "Pending"
        )
      """
    )

  "TransactionType" should "traduire uniquement les trois codes du contrat v1" in:
    TransactionType.fromCode("VIR") shouldBe Some(TransactionType.Transfer)
    TransactionType.fromCode("pre") shouldBe Some(TransactionType.Withdrawal)
    TransactionType.fromCode(" CHQ ") shouldBe Some(TransactionType.Check)
    TransactionType.fromCode("CB") shouldBe None
    TransactionType.Check.code shouldBe "CHQ"

  "ClearingBatch" should "calculer le montant total depuis les transactions nommées" in:
    val batch = ClearingBatch(
      1,
      List(transfer, transfer.copy(id = 2, amount = BigDecimal("25.50"))),
      TransactionStatus.Pending
    )
    batch.totalAmount shouldBe BigDecimal("50025.50")

  "Les résultats nommés" should "conserver les transactions et leurs erreurs" in:
    val invalid = InvalidTransaction(
      transfer,
      List(InvalidAmount(transfer.amount), UnknownBank("ZZZ"))
    )
    val summary = ValidationSummary(Nil, List(invalid))
    val batch = ClearingBatch(2, List(transfer), TransactionStatus.Rejected)
    val result = ClearingResult(batch, Map.empty, invalid.errors)
    val appResult = AppResult(result, summary.invalid, malformedCount = 2, receivedCount = 3)

    summary.valid shouldBe empty
    appResult.result.errors shouldBe invalid.errors
    appResult.invalidTransactions should contain only invalid
