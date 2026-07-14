package clearing.v21

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class RailValidationSpec extends AnyFlatSpec with Matchers:
  private val sourceIban = "MA64ATH00000000000000000"
  private val destinationIban = "MA64CIH00000000000000000"
  private val config = V21Config(
    referenceCurrency = Currency.MAD,
    knownBanks = Set("ATH", "CIH", "BOA"),
    limits = Map(
      TransactionType.Transfer -> BigDecimal("10000"),
      TransactionType.Withdrawal -> BigDecimal("5000"),
      TransactionType.Check -> BigDecimal("20000")
    ),
    ratesToReference = Map(Currency.MAD -> BigDecimal(1)),
    feeRates = Map("ATH" -> BigDecimal("0.001")),
    labelsByTransactionId = Map(1 -> "Virement fournisseur")
  )

  private def rail(
    id: Int = 1,
    sender: String = "ATH",
    receiver: String = "CIH",
    amount: BigDecimal = BigDecimal("100"),
    source: String = sourceIban,
    destination: String = destinationIban
  ): RailTransaction =
    RailTransaction(
      lineNumber = 3,
      transaction = Transaction(
        id = id,
        sender = sender,
        receiver = receiver,
        amount = amount,
        transactionType = TransactionType.Transfer,
        sourceIban = source,
        destinationIban = destination,
        currency = Currency.MAD
      ),
      warnings = Nil
    )

  "RailValidation.validate" should "valider et marquer une transaction conforme" in:
    val input = rail()

    RailValidation.validate(config, Set.empty)(input) shouldBe Right(
      input.copy(
        transaction = input.transaction.copy(
          status = TransactionStatus.Validated
        )
      )
    )

  it should "accumuler les violations métier de la même ligne" in:
    val invalid = rail(
      sender = "ZZZ",
      receiver = "ZZZ",
      amount = BigDecimal(0),
      source = "FR001",
      destination = "FR002"
    )

    RailValidation.validate(config, Set.empty)(invalid) shouldBe Left(
      TransactionValidationError(
        lineNumber = 3,
        transactionId = Some(1),
        reasons = List(
          "MONTANT_NON_POSITIF",
          "VIREMENT_INTERNE",
          "BANQUE_SOURCE_INCONNUE:ZZZ",
          "BANQUE_DESTINATION_INCONNUE:ZZZ",
          "IBAN_SOURCE_INVALIDE",
          "IBAN_DESTINATION_INVALIDE"
        )
      )
    )

  it should "refuser une limite absente ou atteinte" in:
    val withoutTransferLimit = config.copy(
      limits = config.limits - TransactionType.Transfer
    )

    RailValidation.validate(withoutTransferLimit, Set.empty)(rail()) shouldBe
      Left(
        TransactionValidationError(
          3,
          Some(1),
          List("LIMITE_MANQUANTE:VIR")
        )
      )
    RailValidation.validate(config, Set.empty)(
      rail(amount = BigDecimal("10000"))
    ) shouldBe Left(
      TransactionValidationError(
        3,
        Some(1),
        List("LIMITE_DEPASSEE:10000")
      )
    )

  it should "relier chaque segment bancaire IBAN à la banque déclarée" in:
    val incoherent = rail(
      source = destinationIban,
      destination = sourceIban
    )

    RailValidation.validate(config, Set.empty)(incoherent) shouldBe Left(
      TransactionValidationError(
        3,
        Some(1),
        List(
          "IBAN_SOURCE_BANQUE_INCOHERENTE:ATH",
          "IBAN_DESTINATION_BANQUE_INCOHERENTE:CIH"
        )
      )
    )

  it should "refuser deux IBAN identiques en plus du virement interne" in:
    val sameIban = rail(
      receiver = "ATH",
      destination = sourceIban
    )

    RailValidation.validate(config, Set.empty)(sameIban) shouldBe Left(
      TransactionValidationError(
        3,
        Some(1),
        List("VIREMENT_INTERNE", "IBANS_IDENTIQUES")
      )
    )

  it should "court-circuiter un identifiant déjà accepté par une erreur métier" in:
    RailValidation.validate(config, Set(1))(rail()) shouldBe Left(
      Iso20022Rejection(Iso20022Code.AM05, transactionId = 1)
    )
