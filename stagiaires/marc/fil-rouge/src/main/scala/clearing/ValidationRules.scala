package clearing

object ValidationRules:
  type Rule = BigDecimal => Boolean

  val nonNul: Rule = _ != 0
  val positif: Rule = _ > 0
  val seuilAudit: Rule = _ < 100_000
  val defaultRules: List[Rule] = List(nonNul, positif, seuilAudit)

  def filtrerTransactions(
    transactions: List[BigDecimal],
    critere: Rule
  ): List[BigDecimal] =
    transactions.filter(critere)

  def appliquerFrais(
    nomBanque: String
  )(
    montant: BigDecimal
  ): BigDecimal =
    val taux = if nomBanque == "ATH" then BigDecimal("0.01")
    else BigDecimal("0.02")
    (montant * (BigDecimal(1) + taux))
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

  val fraisATH: BigDecimal => BigDecimal = appliquerFrais("ATH")
  val fraisGenerique: BigDecimal => BigDecimal = appliquerFrais("AUTRE")

  def validateAll(amount: BigDecimal, rules: List[Rule]): Boolean =
    rules.forall(rule => rule(amount))

  def validateAny(amount: BigDecimal, rules: List[Rule]): Boolean =
    rules.exists(rule => rule(amount))
