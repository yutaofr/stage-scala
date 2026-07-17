package clearing

object TransactionCategorizer:
  def categorize(amount: BigDecimal): String =
    amount match
      case value if value <= 0                  => "Rejetée"
      case value if value < BigDecimal(100)     => "Micro"
      case value if value < BigDecimal(10000)   => "Standard"
      case value if value < BigDecimal(100000)  => "Importante"
      case _                                    => "Exceptionnelle"

  def describeType(code: String): String =
    code match
      case "VIR" => "Virement"
      case "PRE" => "Prélèvement"
      case "CHQ" => "Chèque"
      case "CB"  => "Carte Bancaire"
      case _     => "Inconnu"
