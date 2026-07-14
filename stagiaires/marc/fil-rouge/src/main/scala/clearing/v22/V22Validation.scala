package clearing.v22

import clearing.model.TransactionStatus
import clearing.v22.DomainTypes.*

object V22Validation:
  def validate(
    config: V22Config,
    seenIds: Set[Int]
  )(
    rail: TypedTransactionLine
  ): Either[V22Error, TypedTransactionLine] =
    val transaction = rail.transaction
    if seenIds.contains(transaction.id) then
      Left(V22DuplicateError(rail.lineNumber, transaction.id))
    else
      val reasons = validationReasons(config, transaction)
      Either.cond(
        reasons.isEmpty,
        rail.copy(
          transaction = transaction.copy(status = TransactionStatus.Validated)
        ),
        V22ValidationError(rail.lineNumber, transaction.id, reasons)
      )

  private def validationReasons(
    config: V22Config,
    transaction: Transaction
  ): List[String] =
    val amountErrors =
      Option.when(!transaction.amount.isPositive)(
        "MONTANT_NON_POSITIF"
      ).toList
    val routeErrors =
      Option.when(transaction.sender == transaction.receiver)(
        "VIREMENT_INTERNE"
      ).toList
    val bankErrors = List(
      Option.when(!config.knownBanks.contains(transaction.sender))(
        s"BANQUE_SOURCE_INCONNUE:${transaction.sender.value}"
      ),
      Option.when(!config.knownBanks.contains(transaction.receiver))(
        s"BANQUE_DESTINATION_INCONNUE:${transaction.receiver.value}"
      )
    ).flatten
    val coherenceErrors = List(
      Option.when(
        !transaction.sourceIban.bankSegment.startsWith(
          transaction.sender.value
        )
      )(
        s"IBAN_SOURCE_BANQUE_INCOHERENTE:${transaction.sender.value}"
      ),
      Option.when(
        !transaction.destinationIban.bankSegment.startsWith(
          transaction.receiver.value
        )
      )(
        s"IBAN_DESTINATION_BANQUE_INCOHERENTE:${transaction.receiver.value}"
      )
    ).flatten
    val sameIbanErrors =
      Option.when(transaction.sourceIban == transaction.destinationIban)(
        "IBANS_IDENTIQUES"
      ).toList
    val limitErrors = config.limits.get(transaction.transactionType) match
      case None =>
        List(s"LIMITE_MANQUANTE:${transaction.transactionType.code}")
      case Some(limit) if transaction.amount.value >= limit.value =>
        List(s"LIMITE_DEPASSEE:${limit.value.bigDecimal.toPlainString}")
      case Some(_) => Nil

    amountErrors ++ routeErrors ++ bankErrors ++ coherenceErrors ++
      sameIbanErrors ++ limitErrors
