package clearing.v10

import clearing.model.*

object TransactionValidator:
  private case class PartitionState(
    seenIds: Set[Int],
    validReversed: List[Transaction],
    invalidReversed: List[InvalidTransaction]
  )

  val knownBanks: Set[String] = TransactionGeneratorV10.bankCodes.toSet

  def validate(
    transaction: Transaction,
    bankCodes: Set[String] = knownBanks
  ): List[ClearingError] =
    val amountErrors =
      if transaction.amount <= 0 then List(InvalidAmount(transaction.amount))
      else Nil
    val senderErrors =
      if bankCodes.contains(transaction.sender) then Nil
      else List(UnknownBank(transaction.sender))
    val receiverErrors =
      if bankCodes.contains(transaction.receiver) then Nil
      else List(UnknownBank(transaction.receiver))
    val transferErrors =
      if transaction.sender == transaction.receiver then
        List(
          FieldValidationError(
            "receiver",
            "doit être différente de la banque source"
          )
        )
      else Nil

    amountErrors ++ senderErrors ++ receiverErrors ++ transferErrors

  def partition(
    transactions: List[Transaction],
    bankCodes: Set[String] = knownBanks
  ): ValidationSummary =
    val initial = PartitionState(Set.empty, Nil, Nil)
    val finalState = transactions.foldLeft(initial)((state, transaction) =>
      val ruleErrors = validate(transaction, bankCodes)
      val errors =
        if state.seenIds.contains(transaction.id) then
          ruleErrors :+ DuplicateTransaction
        else ruleErrors
      val nextSeenIds = state.seenIds + transaction.id

      if errors.isEmpty then
        state.copy(
          seenIds = nextSeenIds,
          validReversed = transaction :: state.validReversed
        )
      else
        state.copy(
          seenIds = nextSeenIds,
          invalidReversed =
            InvalidTransaction(transaction, errors) :: state.invalidReversed
        )
    )

    ValidationSummary(
      finalState.validReversed.reverse,
      finalState.invalidReversed.reverse
    )
