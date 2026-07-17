package clearing.v21

import clearing.model.*

object RailValidation:
  def validate(
    config: V21Config,
    seenIds: Set[Int]
  )(
    rail: RailTransaction
  ): Either[ClearingError, RailTransaction] =
    val transaction = rail.transaction
    if seenIds.contains(transaction.id) then
      Left(Iso20022Rejection(Iso20022Code.AM05, transaction.id))
    else
      val reasons = validationReasons(config, transaction)
      Either.cond(
        reasons.isEmpty,
        rail.copy(
          transaction = transaction.copy(status = TransactionStatus.Validated)
        ),
        TransactionValidationError(
          rail.lineNumber,
          Some(transaction.id),
          reasons
        )
      )

  private def validationReasons(
    config: V21Config,
    transaction: Transaction
  ): List[String] =
    val amountErrors =
      Option.when(transaction.amount <= 0)("MONTANT_NON_POSITIF").toList
    val routeErrors =
      Option.when(transaction.sender == transaction.receiver)(
        "VIREMENT_INTERNE"
      ).toList
    val bankErrors = List(
      Option.when(!config.knownBanks.contains(transaction.sender))(
        s"BANQUE_SOURCE_INCONNUE:${transaction.sender}"
      ),
      Option.when(!config.knownBanks.contains(transaction.receiver))(
        s"BANQUE_DESTINATION_INCONNUE:${transaction.receiver}"
      )
    ).flatten
    val ibanErrors = List(
      Option.when(Iban(transaction.sourceIban).isEmpty)(
        "IBAN_SOURCE_INVALIDE"
      ),
      Option.when(Iban(transaction.destinationIban).isEmpty)(
        "IBAN_DESTINATION_INVALIDE"
      )
    ).flatten
    val ibanBankErrors = List(
      Option.when(
        Iban(transaction.sourceIban).nonEmpty &&
          !matchesBank(transaction.sourceIban, transaction.sender)
      )(s"IBAN_SOURCE_BANQUE_INCOHERENTE:${transaction.sender}"),
      Option.when(
        Iban(transaction.destinationIban).nonEmpty &&
          !matchesBank(transaction.destinationIban, transaction.receiver)
      )(s"IBAN_DESTINATION_BANQUE_INCOHERENTE:${transaction.receiver}")
    ).flatten
    val sameIbanErrors =
      Option.when(transaction.sourceIban == transaction.destinationIban)(
        "IBANS_IDENTIQUES"
      ).toList
    val limitErrors = config.limits.get(transaction.transactionType) match
      case None =>
        List(s"LIMITE_MANQUANTE:${transaction.transactionType.code}")
      case Some(limit) if transaction.amount >= limit =>
        List(s"LIMITE_DEPASSEE:${limit.bigDecimal.toPlainString}")
      case Some(_) => Nil

    amountErrors ++ routeErrors ++ bankErrors ++ ibanErrors ++
      ibanBankErrors ++ sameIbanErrors ++ limitErrors

  private def matchesBank(rawIban: String, bank: String): Boolean =
    Iban.unapply(rawIban).exists: (_, bankSegment, _) =>
      bankSegment.startsWith(bank)
