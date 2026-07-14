package clearing

object TransactionPipelineV3:
  type Lot = (String, List[BigDecimal])

  def enrich(
    transactions: List[TransactionV2.Transaction]
  ): List[TransactionV2.Transaction] =
    transactions.map { case (id, sender, receiver, amount, transactionType) =>
      (
        id,
        ReferenceData.getBankName(sender),
        ReferenceData.getBankName(receiver),
        amount,
        transactionType
      )
    }

  def enrichWithFor(
    transactions: List[TransactionV2.Transaction]
  ): List[TransactionV2.Transaction] =
    for transaction <- transactions yield
      val (id, sender, receiver, amount, transactionType) = transaction
      (
        id,
        ReferenceData.getBankName(sender),
        ReferenceData.getBankName(receiver),
        amount,
        transactionType
      )

  def cleanBatches(
    batches: List[List[TransactionV2.Transaction]]
  ): List[TransactionV2.Transaction] =
    batches
      .flatMap(identity)
      .filter { case (_, sender, receiver, amount, _) =>
        amount > 0 &&
        sender != receiver &&
        ReferenceData.isClearingBank(sender) &&
        ReferenceData.isClearingBank(receiver)
      }
      .map { case (id, sender, receiver, amount, transactionType) =>
        (id, sender, receiver, amount, transactionType.toUpperCase)
      }

  def positiveAmounts(lots: List[Lot]): List[BigDecimal] =
    lots.flatMap(_._2).filter(_ > 0)

  def auditRequired(
    transactions: List[TransactionV2.Transaction]
  ): List[TransactionV2.Transaction] =
    transactions.filter { case (_, sender, receiver, amount, _) =>
      amount > 5_000 &&
      sender != receiver &&
      ReferenceData.isClearingBank(sender) &&
      ReferenceData.isClearingBank(receiver)
    }
