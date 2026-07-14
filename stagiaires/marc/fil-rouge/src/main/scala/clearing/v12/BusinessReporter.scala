package clearing.v12

import clearing.model.*

object BusinessReporter:
  private def displayAmount(amount: BigDecimal): String =
    amount.bigDecimal.stripTrailingZeros().toPlainString

  def totalVolume(transactions: List[Transaction]): BigDecimal =
    transactions.iterator
      .filter(_.status == TransactionStatus.Validated)
      .map(_.amount)
      .sum

  def mostActiveBank(
    transactions: List[Transaction]
  ): Option[BankActivity] =
    val participations = transactions.iterator
      .filter(_.status == TransactionStatus.Validated)
      .flatMap(transaction => Iterator(transaction.sender, transaction.receiver))
      .toList
      .groupMapReduce(identity)(_ => 1)(_ + _)

    participations.toList
      .sortBy: (code, count) =>
        (-count, code)
      .headOption
      .map: (code, count) =>
        BankActivity(BankDirectory.fromCode(code), count)

  def partitionLogs(logs: List[EngineLog]): LogPartition =
    val (financial, technical) = logs.partition:
      case _: FinancialLog => true
      case _: TechnicalLog => false

    LogPartition(
      financial = financial.collect:
        case log: FinancialLog => log,
      technical = technical.collect:
        case log: TechnicalLog => log
    )

  def renderSettlement(positions: Map[String, BigDecimal]): String =
    val ordered = MultilateralNetting.settlementOrder(positions)
    val (debtors, creditors) = ordered.partition(_.balance < 0)
    val debtorLines = debtors.map(position =>
      s"- ${position.bank.code} : ${displayAmount(position.balance)} DH"
    )
    val creditorLines = creditors.map(position =>
      s"- ${position.bank.code} : +${displayAmount(position.balance)} DH"
    )

    (List("BANQUES DÉBITRICES :") ++
      Option.when(debtorLines.isEmpty)("- aucune").toList ++
      debtorLines ++
      List("BANQUES CRÉDITRICES :") ++
      Option.when(creditorLines.isEmpty)("- aucune").toList ++
      creditorLines).mkString(System.lineSeparator())

  def renderBilateral(
    settlements: List[BilateralSettlement]
  ): String =
    val lines = settlements
      .sortBy(settlement =>
        (-settlement.amount, settlement.debtor.code, settlement.creditor.code)
      )
      .map(settlement =>
        s"${settlement.debtor.code} -> ${settlement.creditor.code} : ${displayAmount(settlement.amount)} DH"
      )

    if lines.isEmpty then "- aucun règlement bilatéral"
    else lines.mkString(System.lineSeparator())
