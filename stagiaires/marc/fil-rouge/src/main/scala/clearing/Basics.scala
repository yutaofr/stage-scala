package clearing

object Basics:
  val transaction1: BigDecimal = BigDecimal("1500.50")
  val transaction2: BigDecimal = BigDecimal("-700.25")
  val transaction3: BigDecimal = BigDecimal("350.00")

  val soldeTotal: BigDecimal = transaction1 + transaction2 + transaction3

  def position(solde: BigDecimal): String =
    if solde >= 0 then "CRÉDITRICE" else "DÉBITRICE"

  def resume(transactions: List[BigDecimal]): String =
    val total = transactions.sum
    s"Nombre de transactions : ${transactions.size}\n" +
      s"Solde total : $total DH\n" +
      s"Position : ${position(total)}"

@main def runBasics(): Unit =
  println(
    Basics.resume(
      List(Basics.transaction1, Basics.transaction2, Basics.transaction3)
    )
  )
  println()
  println(Basics.resume(List(BigDecimal("100"), BigDecimal("-250"))))
