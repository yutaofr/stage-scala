package clearing

object PrecisionDemo:
  val doubleSum: Double = 0.1 + 0.2
  val decimalSum: BigDecimal = BigDecimal("0.1") + BigDecimal("0.2")

@main def runPrecisionDemo(): Unit =
  println(s"Double : 0.1 + 0.2 = ${PrecisionDemo.doubleSum}")
  println(s"Double égal à 0.3 ? ${PrecisionDemo.doubleSum == 0.3}")
  println(s"BigDecimal : 0.1 + 0.2 = ${PrecisionDemo.decimalSum}")
  println(
    s"BigDecimal égal à 0.3 ? ${PrecisionDemo.decimalSum == BigDecimal("0.3")}"
  )
