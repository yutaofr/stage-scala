package clearing.v10

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TransactionValidatorSpec extends AnyFlatSpec with Matchers:
  private val valid = Transaction(
    1,
    "ATH",
    "CIH",
    BigDecimal("100"),
    TransactionType.Transfer,
    TransactionStatus.Pending
  )

  "TransactionValidator.validate" should "retourner une liste vide pour une transaction valide" in:
    TransactionValidator.validate(valid) shouldBe empty

  it should "accumuler les erreurs de montant et de banques" in:
    val invalid = valid.copy(
      sender = "ZZZ",
      receiver = "YYY",
      amount = BigDecimal("-10")
    )

    TransactionValidator.validate(invalid) shouldBe List(
      InvalidAmount(BigDecimal("-10")),
      UnknownBank("ZZZ"),
      UnknownBank("YYY")
    )

  it should "représenter un virement interne par une erreur de validation" in:
    TransactionValidator.validate(valid.copy(receiver = "ATH")) shouldBe List(
      ValidationError("receiver", "doit être différente de la banque source")
    )

  "TransactionValidator.partition" should "conserver la première occurrence et rejeter les doublons suivants" in:
    val duplicate = valid.copy(amount = BigDecimal("25"))
    val another = valid.copy(id = 2, sender = "CIH", receiver = "BOA")

    val summary = TransactionValidator.partition(List(valid, duplicate, another))

    summary.valid shouldBe List(valid, another)
    summary.invalid shouldBe List(
      InvalidTransaction(duplicate, List(DuplicateTransaction))
    )

  it should "conserver les erreurs de règle sur une transaction dupliquée" in:
    val duplicateInvalid = valid.copy(amount = BigDecimal("-1"))

    TransactionValidator.partition(List(valid, duplicateInvalid)).invalid shouldBe
      List(
        InvalidTransaction(
          duplicateInvalid,
          List(InvalidAmount(BigDecimal("-1")), DuplicateTransaction)
        )
      )

  "ErrorReporter.formatError" should "déstructurer chaque cas du trait scellé" in:
    ErrorReporter.formatError(InvalidAmount(BigDecimal("-10"))) shouldBe
      "Montant invalide : -10 DH"
    ErrorReporter.formatError(UnknownBank("ZZZ")) shouldBe
      "Banque inconnue : ZZZ"
    ErrorReporter.formatError(DuplicateTransaction) shouldBe
      "Transaction dupliquée"
    ErrorReporter.formatError(ValidationError("receiver", "doit différer")) shouldBe
      "Validation receiver : doit différer"

  "ErrorReporter.formatErrors" should "rendre une ligne par erreur" in:
    ErrorReporter.formatErrors(
      List(UnknownBank("ZZZ"), DuplicateTransaction)
    ) shouldBe "Banque inconnue : ZZZ\nTransaction dupliquée"
