package clearing

object Transaction:
  def apply(line: String): Option[TransactionV2.Transaction] =
    CsvParser.parseLine(line)
