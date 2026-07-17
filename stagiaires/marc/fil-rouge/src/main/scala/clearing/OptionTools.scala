package clearing

object OptionTools:
  private val accounts: Map[String, Int] = Map(
    "MA-ATH-001" -> 1,
    "MA-CIH-002" -> 2,
    "MA-GHOST" -> 99
  )

  private val users: Map[Int, String] = Map(
    1 -> "Amina",
    2 -> "Youssef"
  )

  private val rates: Map[String, BigDecimal] = Map(
    "EUR" -> BigDecimal("10.85"),
    "USD" -> BigDecimal("10.00"),
    "GBP" -> BigDecimal("12.60")
  )

  def findUserNameByIban(iban: String): Option[String] =
    accounts.get(iban).flatMap(users.get)

  def cleanInput(input: String): Option[String] =
    val cleaned = input.trim
    Option.when(cleaned.nonEmpty)(cleaned)

  def cleanUpper(input: String): Option[String] =
    cleanInput(input).map(_.toUpperCase)

  def presentAmounts(
    amounts: List[Option[BigDecimal]]
  ): List[BigDecimal] =
    amounts.flatten

  def sumPresent(amounts: List[Option[BigDecimal]]): BigDecimal =
    presentAmounts(amounts).foldLeft(BigDecimal(0))(_ + _)

  def convert(amount: BigDecimal, currency: String): Option[BigDecimal] =
    rates.get(currency).map { rate =>
      (amount * rate).setScale(2, BigDecimal.RoundingMode.HALF_UP)
    }
