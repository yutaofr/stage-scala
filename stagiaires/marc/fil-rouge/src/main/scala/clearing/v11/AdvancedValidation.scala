package clearing.v11

import clearing.model.*

case class TransactionAssessment(
  transaction: Transaction,
  errors: List[LineError],
  warnings: List[SuspiciousTransaction]
)

object FraudDetector:
  def unapply(transaction: Transaction): Option[String] =
    if transaction.amount > BigDecimal("1000000") then
      Some("montant supérieur à 1000000 DH")
    else if transaction.sourceIban.startsWith("XX") then
      Some("IBAN source sur liste noire XX")
    else None

object InternationalTx:
  def unapply(transaction: Transaction): Boolean =
    !transaction.sourceIban.startsWith("MA")

object ExchangeFees:
  def amountWithExchangeFee(transaction: Transaction): BigDecimal =
    transaction match
      case InternationalTx() =>
        (transaction.amount * BigDecimal("1.02"))
          .setScale(2, BigDecimal.RoundingMode.HALF_UP)
      case _ => transaction.amount

object TransactionClassifier:
  def describe(transaction: Transaction): String =
    transaction match
      case current if current.amount == 0 => "MONTANT_NUL"
      case current if current.sourceIban == current.destinationIban =>
        "IBAN_IDENTIQUES"
      case current if current.amount == BigDecimal("9999.99") =>
        "MONTANT_SUSPECT"
      case FraudDetector(reason) => s"FRAUDE: $reason"
      case InternationalTx()     => "INTERNATIONAL"
      case _                     => "STANDARD"

object AdvancedTransactionValidator:
  val knownBanks: Set[String] = Set("ATH", "CIH", "BOA", "BMCE", "SGMB")

  private def validateIban(
    raw: String,
    expectedBank: String,
    field: String
  ): List[ValidationError] =
    raw match
      case Iban(_, bankSegment, _)
          if bankSegment.startsWith(expectedBank) => Nil
      case Iban(_, _, _) =>
        List(
          FieldValidationError(
            field,
            s"ne correspond pas à la banque $expectedBank"
          )
        )
      case _ => List(InvalidIban(raw))

  def validate(
    transaction: Transaction,
    bankCodes: Set[String] = knownBanks
  ): List[LineError] =
    val amountErrors = transaction match
      case current if current.amount == 0 =>
        List(
          FieldValidationError(
            "amount",
            "doit être strictement positif"
          )
        )
      case current if current.amount < 0 =>
        List(InvalidAmount(current.amount))
      case _ => Nil

    val ibanErrors = validateIban(
      transaction.sourceIban,
      transaction.sender,
      "sourceIban"
    ) ++ validateIban(
      transaction.destinationIban,
      transaction.receiver,
      "destinationIban"
    )

    val sameIbanErrors = transaction match
      case current if current.sourceIban == current.destinationIban =>
        List(
          FieldValidationError(
            "destinationIban",
            "doit être différent de l'IBAN source"
          )
        )
      case _ => Nil

    val bankErrors = List(
      Option.unless(bankCodes.contains(transaction.sender))(
        UnknownBank(transaction.sender)
      ),
      Option.unless(bankCodes.contains(transaction.receiver))(
        UnknownBank(transaction.receiver)
      )
    ).flatten

    amountErrors ++ ibanErrors ++ sameIbanErrors ++ bankErrors

  def assess(
    transaction: Transaction,
    bankCodes: Set[String] = knownBanks
  ): TransactionAssessment =
    val exactAmountWarnings = transaction match
      case current if current.amount == BigDecimal("9999.99") =>
        List(
          SuspiciousTransaction(
            current.id,
            "montant exact de 9999.99 DH"
          )
        )
      case _ => Nil
    val fraudWarnings = transaction match
      case FraudDetector(reason) =>
        List(SuspiciousTransaction(transaction.id, reason))
      case _ => Nil

    TransactionAssessment(
      transaction,
      validate(transaction, bankCodes),
      exactAmountWarnings ++ fraudWarnings
    )
