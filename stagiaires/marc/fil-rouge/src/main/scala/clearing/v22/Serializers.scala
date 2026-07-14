package clearing.v22

import clearing.v22.DomainTypes.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.math.BigDecimal.RoundingMode

trait ClearingSerializable[-T]:
  def toText(value: T): String

trait JsonSerializer[-T] extends ClearingSerializable[T]
trait CsvSerializer[-T] extends ClearingSerializable[T]
trait XmlSerializer[-T] extends ClearingSerializable[T]

object ExportEngine:
  def exportWith[T](
    value: T,
    serializer: ClearingSerializable[T]
  ): String = serializer.toText(value)

  def `export`[T](
    value: T
  )(using serializer: ClearingSerializable[T]): String =
    serializer.toText(value)

  def exportBatch[T](
    values: List[T]
  )(using serializer: ClearingSerializable[T]): String =
    values.map(serializer.toText).mkString("\n")

  def exportItemXml[T](
    value: T
  )(using serializer: XmlSerializer[T]): String =
    serializer.toText(value)

  def exportBatchXml[T](
    values: List[T]
  )(using serializer: XmlSerializer[T]): String =
    values.map(serializer.toText).mkString("\n")

object ManualSerializers:
  val transactionCsv: CsvSerializer[Transaction] =
    (transaction: Transaction) => Rendering.transactionCsv(transaction)

  val bankCsv: CsvSerializer[Bank] =
    (bank: Bank) => Rendering.bankCsv(bank)

  val transactionJson: JsonSerializer[Transaction] =
    (transaction: Transaction) => Rendering.transactionJson(transaction)

  val localDateTime: ClearingSerializable[LocalDateTime] =
    (dateTime: LocalDateTime) =>
      dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

object CsvSerializers:
  given CsvSerializer[Transaction] = ManualSerializers.transactionCsv
  given CsvSerializer[Bank] = ManualSerializers.bankCsv
  given CsvSerializer[ClearingResult] with
    def toText(result: ClearingResult): String = Rendering.resultCsv(result)

object JsonSerializers:
  given JsonSerializer[Transaction] = ManualSerializers.transactionJson
  given JsonSerializer[Bank] with
    def toText(bank: Bank): String = Rendering.bankJson(bank)
  given JsonSerializer[ClearingResult] with
    def toText(result: ClearingResult): String = Rendering.resultJson(result)

object XmlSerializers:
  given XmlSerializer[Transaction] with
    def toText(transaction: Transaction): String =
      Rendering.transactionXml(transaction)
  given XmlSerializer[Bank] with
    def toText(bank: Bank): String = Rendering.bankXml(bank)
  given XmlSerializer[ClearingResult] with
    def toText(result: ClearingResult): String = Rendering.resultXml(result)

private object Rendering:
  def transactionCsv(transaction: Transaction): String =
    List(
      transaction.id.toString,
      transaction.sender.value,
      transaction.receiver.value,
      formatAmount(transaction.amount),
      transaction.transactionType.code,
      transaction.currency.toString,
      transaction.status.toString
    ).map(csvField).mkString(",")

  def bankCsv(bank: Bank): String =
    List(bank.code.value, bank.name).map(csvField).mkString(",")

  def resultCsv(result: ClearingResult): String =
    val header =
      s"RESULT,${result.referenceCurrency},${result.transactions.size},${result.rejections.size}"
    val transactions = result.transactions.map: transaction =>
      List(
        "TRANSACTION",
        transaction.id.toString,
        transaction.sender.value,
        transaction.receiver.value,
        formatAmount(transaction.settlementAmount),
        formatAmount(transaction.fee),
        transaction.referenceCurrency.toString,
        transaction.label,
        warningCodes(transaction.warnings).mkString("+")
      ).map(csvField).mkString(",")
    val rejections = result.rejections.map: rejection =>
      List(
        "REJECTION",
        rejection.lineNumber.toString,
        rejection.transactionId.fold("")(_.toString),
        rejection.code,
        rejection.message
      ).map(csvField).mkString(",")
    val positions = result.positions.toList.sortBy(_._1.value).map:
      (bank, amount) => s"POSITION,${bank.value},${formatAmount(amount)}"
    val fees = result.feesByBank.toList.sortBy(_._1.value).map:
      (bank, amount) => s"FEE,${bank.value},${formatAmount(amount)}"
    val statistics = result.statistics
    val global = s"GLOBAL,${formatAmount(result.positions.values.sum)}"
    val stats =
      s"STATISTICS,${statistics.parsing},${statistics.validation},${statistics.business},${statistics.technical},${statistics.warnings}"

    ((header :: transactions) ++ rejections ++ positions ++ fees ++
      List(global, stats))
      .mkString("\n")

  def transactionJson(transaction: Transaction): String =
    s"""{"id":${transaction.id},"sender":"${json(transaction.sender.value)}","receiver":"${json(transaction.receiver.value)}","amount":"${formatAmount(transaction.amount)}","type":"${transaction.transactionType.code}","currency":"${transaction.currency}","status":"${transaction.status}"}"""

  def bankJson(bank: Bank): String =
    s"""{"code":"${json(bank.code.value)}","name":"${json(bank.name)}"}"""

  def resultJson(result: ClearingResult): String =
    val transactions = result.transactions.map(preparedJson).mkString("[", ",", "]")
    val rejections = result.rejections.map(rejectionJson).mkString("[", ",", "]")
    val positions = moneyMapJson(result.positions)
    val fees = moneyMapJson(result.feesByBank)
    val global = formatAmount(result.positions.values.sum)
    val stats = result.statistics

    s"""{"referenceCurrency":"${result.referenceCurrency}","successes":${result.transactions.size},"rejections":${result.rejections.size},"transactions":$transactions,"errors":$rejections,"positions":$positions,"fees":$fees,"global":"$global","statistics":{"parsing":${stats.parsing},"validation":${stats.validation},"business":${stats.business},"technical":${stats.technical},"warnings":${stats.warnings}}}"""

  def transactionXml(transaction: Transaction): String =
    s"<transaction><id>${transaction.id}</id><sender>${xml(transaction.sender.value)}</sender><receiver>${xml(transaction.receiver.value)}</receiver><amount>${formatAmount(transaction.amount)}</amount><type>${transaction.transactionType.code}</type><currency>${transaction.currency}</currency><status>${transaction.status}</status></transaction>"

  def bankXml(bank: Bank): String =
    s"<bank><code>${xml(bank.code.value)}</code><name>${xml(bank.name)}</name></bank>"

  def resultXml(result: ClearingResult): String =
    val transactions = result.transactions.map(preparedXml).mkString
    val rejections = result.rejections.map(rejectionXml).mkString
    val positions = result.positions.toList.sortBy(_._1.value).map:
      (bank, amount) =>
        s"<position bank=\"${xml(bank.value)}\">${formatAmount(amount)}</position>"
    val fees = result.feesByBank.toList.sortBy(_._1.value).map:
      (bank, amount) =>
        s"<fee bank=\"${xml(bank.value)}\">${formatAmount(amount)}</fee>"
    val stats = result.statistics
    val global = formatAmount(result.positions.values.sum)

    s"<clearingResult referenceCurrency=\"${result.referenceCurrency}\" successes=\"${result.transactions.size}\" rejections=\"${result.rejections.size}\"><transactions>${transactions}</transactions><rejections>${rejections}</rejections><positions>${positions.mkString}</positions><fees>${fees.mkString}</fees><global>$global</global><statistics parsing=\"${stats.parsing}\" validation=\"${stats.validation}\" business=\"${stats.business}\" technical=\"${stats.technical}\" warnings=\"${stats.warnings}\"/></clearingResult>"

  private def preparedJson(transaction: PreparedTransaction): String =
    val warnings = warningCodes(transaction.warnings)
      .map(code => s"\"${json(code)}\"")
      .mkString("[", ",", "]")
    s"""{"id":${transaction.id},"sender":"${json(transaction.sender.value)}","receiver":"${json(transaction.receiver.value)}","amount":"${formatAmount(transaction.settlementAmount)}","fee":"${formatAmount(transaction.fee)}","currency":"${transaction.referenceCurrency}","label":"${json(transaction.label)}","sourceIbanHash":"${json(transaction.sourceIbanHash.value)}","destinationIbanHash":"${json(transaction.destinationIbanHash.value)}","warnings":$warnings}"""

  private def rejectionJson(rejection: Rejection): String =
    val id = rejection.transactionId.fold("null")(_.toString)
    s"""{"line":${rejection.lineNumber},"transactionId":$id,"code":"${json(rejection.code)}","message":"${json(rejection.message)}"}"""

  private def moneyMapJson(values: Map[BankCode, Money]): String =
    values.toList.sortBy(_._1.value).map: (bank, amount) =>
      s"\"${json(bank.value)}\":\"${formatAmount(amount)}\""
    .mkString("{", ",", "}")

  private def preparedXml(transaction: PreparedTransaction): String =
    val warnings = warningCodes(transaction.warnings)
      .map(code => s"<warning>${xml(code)}</warning>")
      .mkString
    s"<transaction><id>${transaction.id}</id><sender>${xml(transaction.sender.value)}</sender><receiver>${xml(transaction.receiver.value)}</receiver><amount>${formatAmount(transaction.settlementAmount)}</amount><fee>${formatAmount(transaction.fee)}</fee><currency>${transaction.referenceCurrency}</currency><label>${xml(transaction.label)}</label><sourceIbanHash>${xml(transaction.sourceIbanHash.value)}</sourceIbanHash><destinationIbanHash>${xml(transaction.destinationIbanHash.value)}</destinationIbanHash><warnings>$warnings</warnings></transaction>"

  private def rejectionXml(rejection: Rejection): String =
    val id = rejection.transactionId.fold("")(_.toString)
    s"<rejection line=\"${rejection.lineNumber}\" transactionId=\"$id\"><code>${xml(rejection.code)}</code><message>${xml(rejection.message)}</message></rejection>"

  private def formatAmount(amount: Money): String =
    amount.value
      .setScale(2, RoundingMode.HALF_UP)
      .bigDecimal
      .toPlainString

  private def warningCodes(warnings: List[V22Warning]): List[String] =
    warnings.map:
      case V22Warning.MissingLabel(_) => "LABEL_MANQUANT"

  private def csvField(value: String): String =
    if value.exists(character => character == ',' || character == '"' ||
        character == '\n' || character == '\r')
    then s"\"${value.replace("\"", "\"\"")}\""
    else value

  private def json(value: String): String =
    value.flatMap:
      case '"'  => "\\\""
      case '\\' => "\\\\"
      case '\b' => "\\b"
      case '\f' => "\\f"
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case character if character.isControl =>
        f"\\u${character.toInt}%04x"
      case character => character.toString

  private def xml(value: String): String =
    value.flatMap:
      case '&'  => "&amp;"
      case '<'  => "&lt;"
      case '>'  => "&gt;"
      case '"'  => "&quot;"
      case '\'' => "&apos;"
      case character => character.toString
