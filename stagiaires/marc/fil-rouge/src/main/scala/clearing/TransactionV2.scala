package clearing

import scala.util.Random

object TransactionV2:
  type Transaction = (Int, String, String, BigDecimal, String)

  def generateBatch(
    n: Int,
    random: Random = Random
  ): List[Transaction] =
    (1 to n).toList.map { id =>
      val sender = TransactionGenerator.banks(
        random.nextInt(TransactionGenerator.banks.size)
      )
      val receivers = TransactionGenerator.banks.filterNot(_ == sender)
      val receiver = receivers(random.nextInt(receivers.size))
      val amount = TransactionGenerator.generateAmount(random)
      val transactionType = TransactionGenerator.types(
        random.nextInt(TransactionGenerator.types.size)
      )
      (id, sender, receiver, amount, transactionType)
    }
