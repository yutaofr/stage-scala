package clearing.v20

object PureClearingRenderer:
  val render: PureClearingReport => String = report =>
    val header = s"REFERENCE|${report.referenceCurrency}"
    val counters = List(
      s"INPUT|${report.transactions.size + report.rejections.size}",
      s"ACCEPTED|${report.transactions.size}",
      s"REJECTED|${report.rejections.size}"
    )
    val transactions = report.transactions.map: transaction =>
      List(
        "TRANSACTION",
        transaction.id.toString,
        transaction.sender,
        transaction.receiver,
        decimal(transaction.settlementAmount),
        transaction.transactionType.code,
        transaction.status.toString,
        transaction.referenceCurrency.toString,
        decimal(transaction.fee),
        transaction.sourceIbanHash,
        transaction.destinationIbanHash
      ).mkString("|")
    val rejections = report.rejections.map: rejection =>
      List(
        "REJECTION",
        rejection.lineNumber.toString,
        rejection.transactionId.fold("-")(_.toString),
        rejection.reason
      ).mkString("|")
    val positions = report.positions.toList.sortBy(_._1).map:
      case (bank, amount) => s"POSITION|$bank|${decimal(amount)}"
    val fees = report.feesByBank.toList.sortBy(_._1).map:
      case (bank, amount) => s"FEE|$bank|${decimal(amount)}"
    val trace = report.trace.map(message => s"TRACE|$message")
    val global =
      s"GLOBAL|${decimal(DataCleaner.formatAmount(report.positions.values.sum))}"

    (header :: counters ++ transactions ++ rejections ++ positions ++ fees ++
      trace :+ global)
      .mkString("\n")

  private def decimal(value: BigDecimal): String =
    value.bigDecimal.toPlainString
