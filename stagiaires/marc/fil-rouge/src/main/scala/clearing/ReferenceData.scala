package clearing

object ReferenceData:
  val repertoire: Map[String, String] = Map(
    "001" -> "Attijariwafa Bank",
    "002" -> "CIH Bank",
    "003" -> "Bank of Africa",
    "004" -> "BMCE Capital",
    "005" -> "Société Générale Maroc"
  )

  val clearingBanks: Map[String, String] = Map(
    "ATH" -> "Attijariwafa Bank",
    "CIH" -> "CIH Bank",
    "BOA" -> "Bank of Africa",
    "BMCE" -> "BMCE Capital",
    "SGMB" -> "Société Générale Maroc"
  )

  private val allBanks = repertoire ++ clearingBanks

  def getBankName(code: String): String =
    allBanks.getOrElse(code, "Banque Inconnue")

  def findBankName(code: String): Option[String] =
    allBanks.get(code)

  def isClearingBank(code: String): Boolean =
    clearingBanks.contains(code)

  def uniqueIbans(ibans: List[String]): Set[String] =
    ibans.toSet

  def indexBySender(
    transactions: List[TransactionV2.Transaction]
  ): Map[String, List[TransactionV2.Transaction]] =
    transactions.groupBy(_._2)

  def sentCounts(
    transactions: List[TransactionV2.Transaction]
  ): Map[String, Int] =
    indexBySender(transactions).view.mapValues(_.size).toMap
