package clearing.v12

import clearing.model.*
import scala.util.Random

object S7TransactionGenerator:
  private val bankCodes = BankDirectory.all.map(_.code)

  private def iban(code: String): String =
    s"MA64$code${"0" * (20 - code.length)}"

  def generateVector(size: Int, seed: Long = 42L): Vector[Transaction] =
    require(size >= 0, "La taille doit être positive ou nulle")
    val random = Random(seed)

    Vector.tabulate(size): index =>
      val senderIndex = random.nextInt(bankCodes.size)
      val receiverCandidate = random.nextInt(bankCodes.size - 1)
      val receiverIndex =
        if receiverCandidate >= senderIndex then receiverCandidate + 1
        else receiverCandidate
      val sender = bankCodes(senderIndex)
      val receiver = bankCodes(receiverIndex)
      val cents = random.nextInt(10000) + 1

      Transaction(
        id = index + 1,
        sender = sender,
        receiver = receiver,
        amount = BigDecimal(cents) / BigDecimal(100),
        transactionType = TransactionType.Transfer,
        status = TransactionStatus.Validated,
        sourceIban = iban(sender),
        destinationIban = iban(receiver),
        currency = Currency.MAD
      )
