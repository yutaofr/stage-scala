package clearing.v22

import clearing.v22.DomainTypes.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object PrimitiveSyntax:
  extension (amount: BigDecimal)
    def isPositive: Boolean = amount > 0

  extension (raw: String)
    def isValidBankCode: Boolean = raw.matches("[A-Z]{3}")

object DateSyntax:
  private val shortDate = DateTimeFormatter.ofPattern("dd/MM", Locale.ROOT)

  extension (dateTime: LocalDateTime)
    def toSimpleFormat: String = dateTime.format(shortDate)

object TransactionSyntax:
  extension (transaction: Transaction)
    def isValid: Boolean =
      transaction.amount.isPositive &&
        transaction.sender != transaction.receiver &&
        transaction.sourceIban != transaction.destinationIban &&
        transaction.sourceIban.bankSegment.startsWith(
          transaction.sender.value
        ) &&
        transaction.destinationIban.bankSegment.startsWith(
          transaction.receiver.value
        )

    def toSummary: String =
      s"#${transaction.id} ${transaction.sender.value} -> " +
        s"${transaction.receiver.value} : ${transaction.amount.format}"

object SerializationSyntax:
  extension [T](value: T)
    def toJson(using serializer: JsonSerializer[T]): String =
      serializer.toText(value)

    def toCsv(using serializer: CsvSerializer[T]): String =
      serializer.toText(value)

    def toXml(using serializer: XmlSerializer[T]): String =
      serializer.toText(value)
