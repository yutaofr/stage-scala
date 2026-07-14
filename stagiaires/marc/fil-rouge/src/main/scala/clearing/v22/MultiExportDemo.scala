package clearing.v22

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.DomainTypes.*

object MultiExportDemo:
  private val ath = BankCode.unsafe("ATH")
  private val cih = BankCode.unsafe("CIH")
  private val boa = BankCode.unsafe("BOA")

  val sampleTransactions: List[Transaction] = List(
    transaction(1, ath, cih, BigDecimal(100)),
    transaction(2, cih, boa, BigDecimal(50)),
    transaction(3, boa, ath, BigDecimal(25))
  )

  def renderAll(
    transactions: List[Transaction]
  ): Map[OutputFormat, String] =
    val json =
      import JsonSerializers.given
      ExportEngine.exportBatch(transactions)
    val csv =
      import CsvSerializers.given
      ExportEngine.exportBatch(transactions)
    val xml =
      import XmlSerializers.given
      ExportEngine.exportBatch(transactions)

    Map(
      OutputFormat.Json -> json,
      OutputFormat.Csv -> csv,
      OutputFormat.Xml -> xml
    )

  private def transaction(
    id: Int,
    sender: BankCode,
    receiver: BankCode,
    amount: BigDecimal
  ): Transaction =
    Transaction(
      id = id,
      sender = sender,
      receiver = receiver,
      sourceIban = demoIban(sender),
      destinationIban = demoIban(receiver),
      amount = Money(amount),
      transactionType = TransactionType.Transfer,
      currency = Currency.MAD,
      status = TransactionStatus.Validated
    )

  private def demoIban(bank: BankCode): Iban =
    Iban.unsafe(s"MA64${bank.value}${"0" * (20 - bank.value.length)}")
