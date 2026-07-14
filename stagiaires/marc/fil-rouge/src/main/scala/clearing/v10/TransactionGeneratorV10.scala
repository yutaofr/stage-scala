package clearing.v10

import clearing.model.*
import scala.util.Random

object TransactionGeneratorV10:
  val bankCodes: List[String] = List("ATH", "CIH", "BOA", "BMCE", "SGMB")

  def generateBatch(
    size: Int,
    random: Random = Random
  ): List[Transaction] =
    val transactionTypes = TransactionType.values
    (1 to size).toList.map(id =>
      val sender = bankCodes(random.nextInt(bankCodes.size))
      val receivers = bankCodes.filterNot(_ == sender)
      val receiver = receivers(random.nextInt(receivers.size))
      val cents = random.nextInt(5_000_000) + 1
      val amount = BigDecimal(java.math.BigDecimal.valueOf(cents.toLong, 2))
      val transactionType = transactionTypes(
        random.nextInt(transactionTypes.length)
      )
      Transaction(
        id,
        sender,
        receiver,
        amount,
        transactionType,
        TransactionStatus.Pending
      )
    )
