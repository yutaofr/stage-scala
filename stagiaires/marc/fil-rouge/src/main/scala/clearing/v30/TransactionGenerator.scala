package clearing.v30

import scala.util.Random

object TransactionGenerator:
  private val banks = Vector("ATH", "BOA", "CIH", "SGMB", "BCP")

  def generate(count: Int, seed: Long): List[InputTransactionEvent] =
    require(count >= 0, "count doit être positif ou nul")
    val random = new Random(seed)

    (1 to count).map: id =>
      val senderIndex = random.nextInt(banks.size)
      val sender = banks(senderIndex)
      val otherOffset = 1 + random.nextInt(banks.size - 1)
      val receiver = banks((senderIndex + otherOffset) % banks.size)
      val amountInCents = 1L + random.nextInt(900000)

      InputTransactionEvent(
        id = id,
        sender = sender,
        receiver = receiver,
        sourceIban = iban(sender, account = id * 2),
        destinationIban = iban(receiver, account = id * 2 + 1),
        amount = BigDecimal(amountInCents) / 100,
        transactionType = "VIR",
        currency = "MAD"
      )
    .toList

  private def iban(bank: String, account: Int): String =
    val prefix = s"MA64$bank"
    val suffix = account.toString
    prefix + "0" * (24 - prefix.length - suffix.length) + suffix

