package clearing.v11

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class AdvancedValidationSpec extends AnyFlatSpec with Matchers:
  private val athIban = "MA64ATH00000000000000000"
  private val cihIban = "MA64CIH00000000000000000"

  private def transaction(
    amount: BigDecimal = BigDecimal("100"),
    sourceIban: String = athIban,
    destinationIban: String = cihIban,
    sender: String = "ATH",
    receiver: String = "CIH"
  ): Transaction =
    Transaction(
      1,
      sender,
      receiver,
      amount,
      TransactionType.Transfer,
      sourceIban = sourceIban,
      destinationIban = destinationIban
    )

  "AdvancedTransactionValidator.validate" should "distinguer montant nul et négatif avec des gardes" in:
    AdvancedTransactionValidator.validate(transaction(amount = 0)) should contain(
      FieldValidationError("amount", "doit être strictement positif")
    )
    AdvancedTransactionValidator.validate(transaction(amount = -1)) should contain(
      InvalidAmount(BigDecimal("-1"))
    )

  it should "détecter des IBAN identiques" in:
    AdvancedTransactionValidator.validate(
      transaction(destinationIban = athIban)
    ) should contain(
      FieldValidationError(
        "destinationIban",
        "doit être différent de l'IBAN source"
      )
    )

  it should "accumuler deux IBAN invalides et deux banques inconnues" in:
    val errors = AdvancedTransactionValidator.validate(
      transaction(
        sourceIban = "XX64ATH00000000000000000",
        destinationIban = "FR64CIH00000000000000000",
        sender = "XXX",
        receiver = "YYY"
      )
    )

    errors should contain allOf (
      InvalidIban("XX64ATH00000000000000000"),
      InvalidIban("FR64CIH00000000000000000"),
      UnknownBank("XXX"),
      UnknownBank("YYY")
    )

  it should "utiliser l'extracteur pour relier l'IBAN à la banque" in:
    AdvancedTransactionValidator.validate(
      transaction(sender = "CIH", sourceIban = athIban)
    ) should contain(
      FieldValidationError(
        "sourceIban",
        "ne correspond pas à la banque CIH"
      )
    )

  "TransactionClassifier" should "exprimer les trois gardes demandées" in:
    TransactionClassifier.describe(transaction(amount = 0)) shouldBe "MONTANT_NUL"
    TransactionClassifier.describe(
      transaction(destinationIban = athIban)
    ) shouldBe "IBAN_IDENTIQUES"
    TransactionClassifier.describe(transaction(amount = BigDecimal("9999.99"))) shouldBe
      "MONTANT_SUSPECT"

  "FraudDetector" should "extraire la raison d'un gros montant" in:
    transaction(amount = BigDecimal("1000000.01")) match
      case FraudDetector(reason) =>
        reason shouldBe "montant supérieur à 1000000 DH"
      case _ => fail("Le gros montant aurait dû être extrait")

  it should "extraire un IBAN source sur liste noire" in:
    transaction(sourceIban = "XX64ATH00000000000000000") match
      case FraudDetector(reason) =>
        reason shouldBe "IBAN source sur liste noire XX"
      case _ => fail("L'IBAN XX aurait dû être extrait")

  "InternationalTx" should "détecter un IBAN non marocain et appliquer deux pour cent" in:
    val international = transaction(
      amount = BigDecimal("100"),
      sourceIban = "FR64ATH00000000000000000"
    )

    InternationalTx.unapply(international) shouldBe true
    ExchangeFees.amountWithExchangeFee(international) shouldBe BigDecimal("102.00")
    ExchangeFees.amountWithExchangeFee(transaction()) shouldBe BigDecimal("100")

  "AdvancedTransactionValidator.assess" should "séparer les avertissements des erreurs bloquantes" in:
    val suspicious = AdvancedTransactionValidator.assess(
      transaction(amount = BigDecimal("9999.99"))
    )

    suspicious.errors shouldBe empty
    suspicious.warnings shouldBe List(
      SuspiciousTransaction(1, "montant exact de 9999.99 DH")
    )
