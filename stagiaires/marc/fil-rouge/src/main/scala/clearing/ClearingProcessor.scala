package clearing

trait ClearingProcessor:
  def validate(tx: TransactionV2.Transaction): Boolean

  def calculate(
    transactions: List[TransactionV2.Transaction]
  ): Map[String, BigDecimal]

  def report(results: Map[String, BigDecimal]): Unit

  def process(
    transactions: List[TransactionV2.Transaction]
  ): Map[String, BigDecimal] =
    calculate(transactions.filter(validate))
