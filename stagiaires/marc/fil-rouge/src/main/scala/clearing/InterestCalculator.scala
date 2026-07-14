package clearing

object InterestCalculator:
  def interetsComposes(
    capital: BigDecimal,
    tauxAnnuel: BigDecimal,
    nombreAnnees: Int
  ): BigDecimal =
    require(nombreAnnees >= 0, "Le nombre d'années doit être positif ou nul")
    (1 to nombreAnnees).foldLeft(capital) { (acc, _) =>
      acc * (BigDecimal(1) + tauxAnnuel)
    }

@main def runInterestSimulation(): Unit =
  val capital = BigDecimal("100000.00")
  val taux = BigDecimal("0.035")
  for annee <- 1 to 10 do
    val resultat = InterestCalculator.interetsComposes(capital, taux, annee)
    println(
      s"Année $annee : ${resultat.setScale(2, BigDecimal.RoundingMode.HALF_UP)} DH"
    )
