package clearing

object Validator:
  private val IbanPattern = "^[A-Za-z]{2}[0-9]{22}$".r

  def isPositiveAmount(amount: BigDecimal): Boolean = amount > 0

  def isValidIban(iban: String): Boolean =
    IbanPattern.matches(iban)

  def isKnownBank(
    bankCode: String,
    knownBanks: List[String] = List("ATH", "CIH", "BOA", "BMCE", "SGMB")
  ): Boolean =
    knownBanks.contains(bankCode)

  def validateTransaction(
    iban: String,
    bankCode: String,
    amount: BigDecimal
  ): String =
    (isValidIban(iban), isKnownBank(bankCode), isPositiveAmount(amount)) match
      case (false, _, _)          => "IBAN invalide"
      case (_, false, _)          => "Banque inconnue"
      case (_, _, false)          => "Montant invalide"
      case (true, true, true)     => "OK"
