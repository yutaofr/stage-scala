package clearing

object TransactionWorkflowV4:
  def validate(
    transaction: TransactionV2.Transaction,
    rules: List[ValidationRules.Rule]
  ): Option[TransactionV2.Transaction] =
    val (_, sender, receiver, amount, _) = transaction
    Option.when(
      sender != receiver && ValidationRules.validateAll(amount, rules)
    )(transaction)

  def enrich(
    transaction: TransactionV2.Transaction
  ): Option[TransactionV2.Transaction] =
    val (_, sender, receiver, _, _) = transaction
    for
      _ <- ReferenceData.findBankName(sender)
      _ <- ReferenceData.findBankName(receiver)
    yield transaction

  def parseValidateEnrich(
    line: String,
    rules: List[ValidationRules.Rule]
  ): Option[TransactionV2.Transaction] =
    for
      parsed <- Transaction(line)
      validated <- validate(parsed, rules)
      enriched <- enrich(validated)
    yield enriched

  def collectValid(
    lines: List[String],
    rules: List[ValidationRules.Rule]
  ): (List[TransactionV2.Transaction], Int) =
    val accepted = lines
      .view
      .map(line => parseValidateEnrich(line, rules))
      .flatten
      .toList
    (accepted, lines.size - accepted.size)
