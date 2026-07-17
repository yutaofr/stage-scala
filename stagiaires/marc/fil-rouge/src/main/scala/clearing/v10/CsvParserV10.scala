package clearing.v10

import clearing.model.*

object CsvParserV10:
  private case class ReindexState(
    nextId: Int,
    transactionsReversed: List[Transaction]
  )

  private val BankCodePattern = "^[A-Z]{2,10}$".r
  private val AmountPattern = "^[+-]?[0-9]+(?:\\.[0-9]+)?$".r

  private def parseAmount(raw: String): Option[BigDecimal] =
    raw match
      case AmountPattern() => Some(BigDecimal(raw))
      case _               => None

  private def validBankCode(raw: String): Boolean =
    BankCodePattern.matches(raw)

  private def buildTransaction(
    id: Int,
    sender: String,
    receiver: String,
    amountRaw: String,
    transactionType: TransactionType
  ): Option[Transaction] =
    parseAmount(amountRaw).filter(_ =>
      validBankCode(sender) && validBankCode(receiver)
    ).map(amount =>
      Transaction(
        id,
        sender,
        receiver,
        amount,
        transactionType,
        TransactionStatus.Pending
      )
    )

  def parseLine(line: String): Option[Transaction] =
    if line.trim.isEmpty then None
    else
      line.split(",", -1).map(_.trim) match
        case Array(sender, receiver, amountRaw) =>
          buildTransaction(
            0,
            sender,
            receiver,
            amountRaw,
            TransactionType.Transfer
          )
        case Array(idRaw, sender, receiver, amountRaw, typeRaw) =>
          idRaw.toIntOption.flatMap(id =>
            TransactionType.fromCode(typeRaw).flatMap(transactionType =>
              buildTransaction(
                id,
                sender,
                receiver,
                amountRaw,
                transactionType
              )
            )
          )
        case _ => None

  def parseLines(lines: List[String]): List[Transaction] =
    val parsed = lines.flatMap(parseLine)
    val firstGeneratedId = parsed.map(_.id).filter(_ != 0).maxOption
      .map(_ + 1)
      .getOrElse(1)
    val initial = ReindexState(firstGeneratedId, Nil)
    val finalState = parsed.foldLeft(initial)((state, transaction) =>
      if transaction.id == 0 then
        state.copy(
          nextId = state.nextId + 1,
          transactionsReversed =
            transaction.copy(id = state.nextId) :: state.transactionsReversed
        )
      else
        state.copy(
          transactionsReversed = transaction :: state.transactionsReversed
        )
    )
    finalState.transactionsReversed.reverse
