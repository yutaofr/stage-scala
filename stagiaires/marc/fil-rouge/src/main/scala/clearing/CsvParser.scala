package clearing

object CsvParser:
  private val BankCodePattern = "^[A-Z]{2,10}$".r
  private val TransactionTypePattern = "^[A-Z]{2,5}$".r
  private val AmountPattern = "^[+-]?[0-9]+(?:\\.[0-9]+)?$".r

  private type ParseOutcome =
    (Option[TransactionV2.Transaction], Option[String])

  private def parseAmount(raw: String): Option[BigDecimal] =
    raw match
      case AmountPattern() => Some(BigDecimal(raw))
      case _               => None

  private def validCodes(
    sender: String,
    receiver: String,
    transactionType: String
  ): Boolean =
    BankCodePattern.matches(sender) &&
      BankCodePattern.matches(receiver) &&
      TransactionTypePattern.matches(transactionType)

  private def parseOutcome(line: String): ParseOutcome =
    if line.trim.isEmpty then
      (None, Some("ligne vide"))
    else
      line.split(",", -1).map(_.trim) match
        case Array(sender, receiver, amountRaw) =>
          parseAmount(amountRaw) match
            case Some(amount) if validCodes(sender, receiver, "VIR") =>
              (Some((0, sender, receiver, amount, "VIR")), None)
            case _ =>
              (None, Some("code ou nombre invalide"))

        case Array(idRaw, sender, receiver, amountRaw, transactionType) =>
          (idRaw.toIntOption, parseAmount(amountRaw)) match
            case (Some(id), Some(amount))
                if validCodes(sender, receiver, transactionType) =>
              (Some((id, sender, receiver, amount, transactionType)), None)
            case _ =>
              (None, Some("ID, code ou nombre invalide"))

        case _ =>
          (None, Some("colonnes attendues: 3 ou 5"))

  def parseLine(line: String): Option[TransactionV2.Transaction] =
    parseOutcome(line)._1

  def parseLines(lines: List[String]): List[TransactionV2.Transaction] =
    lines.flatMap { line =>
      val (transaction, error) = parseOutcome(line)
      error.foreach { reason =>
        Console.err.println(s"[ERROR] Ligne malformée : \"$line\" ($reason)")
      }
      transaction
    }
