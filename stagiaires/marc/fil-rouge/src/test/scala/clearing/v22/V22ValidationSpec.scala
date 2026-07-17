package clearing.v22

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.DomainTypes.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class V22ValidationSpec extends AnyFlatSpec with Matchers:
  private val ath = BankCode.unsafe("ATH")
  private val cih = BankCode.unsafe("CIH")
  private val boa = BankCode.unsafe("BOA")
  private val athIban = Iban.unsafe("MA64ATH00000000000000000")
  private val cihIban = Iban.unsafe("MA64CIH00000000000000000")

  private val config = V22Config(
    referenceCurrency = Currency.MAD,
    knownBanks = Set(ath, cih),
    limits = Map(
      TransactionType.Transfer -> Money(BigDecimal("10000")),
      TransactionType.Withdrawal -> Money(BigDecimal("5000")),
      TransactionType.Check -> Money(BigDecimal("20000"))
    ),
    ratesToReference = Map(Currency.MAD -> BigDecimal(1)),
    feeRates = Map(ath -> BigDecimal("0.001"), cih -> BigDecimal("0.002")),
    labelsByTransactionId = Map(1 -> "Facture fournisseur")
  )

  private def rail(
    id: Int = 1,
    sender: BankCode = ath,
    receiver: BankCode = cih,
    source: Iban = athIban,
    destination: Iban = cihIban,
    amount: Money = Money(BigDecimal(100)),
    transactionType: TransactionType = TransactionType.Transfer
  ): TypedTransactionLine =
    TypedTransactionLine(
      lineNumber = 3,
      transaction = Transaction(
        id = id,
        sender = sender,
        receiver = receiver,
        sourceIban = source,
        destinationIban = destination,
        amount = amount,
        transactionType = transactionType,
        currency = Currency.MAD
      ),
      label = None,
      warnings = Nil
    )

  "V22Validation.validate" should "valider et marquer une transaction conforme" in:
    V22Validation.validate(config, Set.empty)(rail()).map(
      _.transaction.status
    ) shouldBe Right(TransactionStatus.Validated)

  it should "agréger les règles métier de la même transaction" in:
    val invalid = rail(
      receiver = ath,
      destination = athIban,
      amount = Money.zero
    )

    V22Validation.validate(config, Set.empty)(invalid) shouldBe Left(
      V22ValidationError(
        lineNumber = 3,
        id = 1,
        reasons = List(
          "MONTANT_NON_POSITIF",
          "VIREMENT_INTERNE",
          "IBANS_IDENTIQUES"
        )
      )
    )

  it should "relier chaque segment IBAN au code bancaire opaque" in:
    V22Validation.validate(config, Set.empty)(
      rail(sender = boa, source = athIban)
    ) shouldBe Left(
      V22ValidationError(
        3,
        1,
        List(
          "BANQUE_SOURCE_INCONNUE:BOA",
          "IBAN_SOURCE_BANQUE_INCOHERENTE:BOA"
        )
      )
    )

  it should "refuser une limite absente ou atteinte" in:
    val noTransferLimit = config.copy(
      limits = config.limits - TransactionType.Transfer
    )

    V22Validation.validate(noTransferLimit, Set.empty)(rail()) shouldBe Left(
      V22ValidationError(3, 1, List("LIMITE_MANQUANTE:VIR"))
    )
    V22Validation.validate(config, Set.empty)(
      rail(amount = Money(BigDecimal("10000")))
    ) shouldBe Left(
      V22ValidationError(3, 1, List("LIMITE_DEPASSEE:10000"))
    )

  it should "court-circuiter un identifiant déjà accepté" in:
    V22Validation.validate(config, Set(1))(
      rail(amount = Money.zero)
    ) shouldBe Left(V22DuplicateError(3, 1))
