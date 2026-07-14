package clearing.v13

import clearing.model.Transaction
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.format.DateTimeFormatter
import java.time.{Clock, ZoneId, ZonedDateTime}
import java.util.UUID

case class SecureTransactionLog(
  transactionId: Int,
  sourceIbanHash: String,
  destinationIbanHash: String,
  amount: BigDecimal,
  formattedAt: String
)

case class SecureBatch(
  id: UUID,
  processedAt: ZonedDateTime,
  transactions: List[SecureTransactionLog]
)

object SecurityUtils:
  def hashIban(iban: String): String =
    val digest = MessageDigest.getInstance("SHA-256")
    digest
      .digest(iban.getBytes(StandardCharsets.UTF_8))
      .map(byte => f"${byte & 0xff}%02x")
      .mkString

object BankTime:
  val MoroccoZone: ZoneId = ZoneId.of("Africa/Casablanca")

  private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

  def format(timestamp: ZonedDateTime): String =
    formatter.format(timestamp.toLocalDateTime)

object SecureBatchFactory:
  def create(
    transactions: List[Transaction],
    clock: Clock,
    uuidSupplier: () => UUID
  ): SecureBatch =
    val processedAt =
      ZonedDateTime.now(clock).withZoneSameInstant(BankTime.MoroccoZone)
    val formattedAt = BankTime.format(processedAt)
    val secureTransactions = transactions.map: transaction =>
      SecureTransactionLog(
        transactionId = transaction.id,
        sourceIbanHash = SecurityUtils.hashIban(transaction.sourceIban),
        destinationIbanHash = SecurityUtils.hashIban(
          transaction.destinationIban
        ),
        amount = transaction.amount,
        formattedAt = formattedAt
      )

    SecureBatch(
      id = uuidSupplier(),
      processedAt = processedAt,
      transactions = secureTransactions
    )
