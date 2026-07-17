package clearing

import scala.util.Random

object TransactionGenerator:
  val banks: List[String] = List("ATH", "CIH", "BOA", "BMCE", "SGMB")
  val types: List[String] = List("VIR", "PRE", "CHQ", "CB")

  def generateAmount(random: Random = Random): BigDecimal =
    val cents = random.nextInt(5_000_000) - 1_000_000
    BigDecimal(java.math.BigDecimal.valueOf(cents.toLong, 2))

  def generateBatch(
    n: Int,
    random: Random = Random
  ): List[(String, String, BigDecimal)] =
    List.fill(n):
      val bank = banks(random.nextInt(banks.size))
      val transactionType = types(random.nextInt(types.size))
      (bank, transactionType, generateAmount(random))

@main def runTransactionGenerator(): Unit =
  val batch = TransactionGenerator.generateBatch(20)
  for (bank, transactionType, amount) <- batch do
    val category = TransactionCategorizer.categorize(amount)
    println(
      f"$bank%-6s | ${TransactionCategorizer.describeType(transactionType)}%-15s | $amount%12.2f DH | $category"
    )
