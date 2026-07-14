package clearing.v20

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class PureDomainSpec extends AnyFlatSpec with Matchers:
  private val sourceIban = "MA64ATH00000000000000000"
  private val destinationIban = "MA64CIH00000000000000000"

  private val transaction = Transaction(
    id = 17,
    sender = "ATH",
    receiver = "CIH",
    amount = BigDecimal("100.00"),
    transactionType = TransactionType.Transfer,
    status = TransactionStatus.Validated,
    sourceIban = " ma64 ath 00000000000000000 ",
    destinationIban = " ma64 cih 00000000000000000 ",
    currency = Currency.MAD
  )

  "Les types de lot purs" should "conserver l'ordre des lignes et des rejets" in:
    val first = NumberedTransaction(1, transaction)
    val second = NumberedTransaction(3, transaction.copy(id = 18))
    val malformed = PureRejection(2, None, "CSV_MALFORME")

    val parsed = ParsedBatch(List(first, second), List(malformed))
    val validated = ValidatedBatch(
      List(second),
      parsed.rejections :+ PureRejection(1, Some(17), "REGLES_METIER")
    )

    parsed.transactions.map(_.lineNumber) shouldBe List(1, 3)
    validated.rejections.map(_.lineNumber) shouldBe List(2, 1)

  "PureValidationError" should "nommer chaque échec attendu du coeur" in:
    val failures: List[PureValidationError] = List(
      PureValidationError.NonPositiveAmount(17),
      PureValidationError.BusinessRules(17, List("iban")),
      PureValidationError.LimitExceeded(17, BigDecimal("1000000")),
      PureValidationError.MissingRate(17, Currency.USD)
    )

    failures should have size 4

  "DataCleaner.anonymize" should "nettoyer et hacher les IBAN sans les conserver" in:
    val prepared = DataCleaner.anonymize(
      referenceCurrency = Currency.MAD,
      settlementAmount = BigDecimal("100.00"),
      fee = BigDecimal("0.10")
    )(transaction)

    prepared.sourceIbanHash shouldBe
      "f528910ebd6e1661f465f3538e4e0b3f2e203e8d29fe4412e6c1c350f7fdecfc"
    prepared.destinationIbanHash shouldBe
      "605fe8aa83ca713a218d84632d580832f1e36ce2da779f81199e372cbc5359a9"
    prepared.productElementNames.toList should not contain "sourceIban"
    prepared.productElementNames.toList should not contain "destinationIban"
    prepared.toString should not include sourceIban
    prepared.toString should not include destinationIban

  "PreparedBatch et PureClearingReport" should "transporter seulement des valeurs immuables" in:
    val preparedTransaction = DataCleaner.anonymize(
      Currency.MAD,
      BigDecimal("100.00"),
      BigDecimal("0.10")
    )(transaction)
    val rejection = PureRejection(2, None, "CSV_MALFORME")
    val batch = PreparedBatch(
      Currency.MAD,
      List(preparedTransaction),
      List(rejection),
      List("[ATH] transaction 17 préparée")
    )
    val report = PureClearingReport(
      batch.referenceCurrency,
      batch.transactions,
      batch.rejections,
      Map("ATH" -> BigDecimal("-100.00"), "CIH" -> BigDecimal("100.00")),
      Map("ATH" -> BigDecimal("0.10")),
      batch.trace
    )

    report.transactions shouldBe List(preparedTransaction)
    report.rejections shouldBe List(rejection)
    report.positions.values.sum shouldBe BigDecimal("0.00")

  "PureClearingEngine.errorCode" should "rendre toute la hiérarchie sans donnée sensible" in:
    List(
      PureClearingEngine.errorCode(InvalidAmount(BigDecimal("-1"))),
      PureClearingEngine.errorCode(UnknownBank("ZZZ")),
      PureClearingEngine.errorCode(InvalidIban("secret")),
      PureClearingEngine.errorCode(
        FieldValidationError("sourceIban", "secret")
      ),
      PureClearingEngine.errorCode(MalformedCsv("secret")),
      PureClearingEngine.errorCode(DuplicateTransaction),
      PureClearingEngine.errorCode(SuspiciousTransaction(17, "secret")),
      PureClearingEngine.errorCode(
        Iso20022Rejection(Iso20022Code.AC01, 17)
      ),
      PureClearingEngine.errorCode(
        ParsingError(8, ParsingFailure.InvalidAmount)
      ),
      PureClearingEngine.errorCode(
        TransactionValidationError(8, Some(17), List("MONTANT_NON_POSITIF"))
      )
    ) shouldBe List(
      "MONTANT_INVALIDE",
      "BANQUE_INCONNUE",
      "IBAN_INVALIDE",
      "CHAMP_INVALIDE:SOURCEIBAN",
      "CSV_MALFORME",
      "TRANSACTION_DUPLIQUEE",
      "TRANSACTION_SUSPECTE",
      "ISO20022:AC01",
      "PARSE_AMOUNT",
      "VALIDATION_TRANSACTION"
    )

  "PureClearingEngine.validationErrorCode" should "rendre chaque erreur pure" in:
    List(
      PureClearingEngine.validationErrorCode(
        PureValidationError.NonPositiveAmount(17)
      ),
      PureClearingEngine.validationErrorCode(
        PureValidationError.BusinessRules(17, List("A", "B"))
      ),
      PureClearingEngine.validationErrorCode(
        PureValidationError.LimitExceeded(17, BigDecimal("100"))
      ),
      PureClearingEngine.validationErrorCode(
        PureValidationError.MissingRate(17, Currency.USD)
      )
    ) shouldBe List(
      "MONTANT_NON_POSITIF",
      "A+B",
      "LIMITE_DEPASSEE:100",
      "TAUX_MANQUANT:USD"
    )
