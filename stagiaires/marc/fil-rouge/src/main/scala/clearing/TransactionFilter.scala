package clearing

object TransactionFilter:
  type TransactionTuple = (String, String, BigDecimal)

  def filterValid(batch: List[TransactionTuple]): List[TransactionTuple] =
    for transaction @ (_, _, amount) <- batch if amount > 0 yield transaction

  def filterByBank(
    batch: List[TransactionTuple],
    bank: String
  ): List[TransactionTuple] =
    for transaction @ (bankCode, _, _) <- batch if bankCode == bank
    yield transaction

  def totalsByBank(batch: List[TransactionTuple]): Map[String, BigDecimal] =
    batch
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._3).sum)
      .toMap

  def sumByBank(batch: List[TransactionTuple]): Unit =
    totalsByBank(batch).toList.sortBy(_._1).foreach { (bank, total) =>
      println(s"$bank : $total DH")
    }
