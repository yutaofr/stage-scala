package clearing.v22

import java.util.Locale
import scala.annotation.targetName
import scala.math.BigDecimal.RoundingMode
import scala.util.Try

object DomainTypes:
  opaque type BankCode = String
  opaque type Iban = String
  opaque type Money = BigDecimal

  object BankCode:
    def from(raw: String): Either[String, BankCode] =
      val normalized = raw.trim.toUpperCase(Locale.ROOT)
      Either.cond(
        normalized.matches("[A-Z]{3,4}"),
        normalized,
        "code bancaire invalide"
      )

    def unsafe(raw: String): BankCode =
      from(raw).fold(
        message => throw new IllegalArgumentException(message),
        identity
      )

  object Iban:
    def from(raw: String): Either[String, Iban] =
      val normalized = raw.trim.toUpperCase(Locale.ROOT)
      Either.cond(
        normalized.matches("MA[A-Z0-9]{22}"),
        normalized,
        "IBAN invalide"
      )

    def unsafe(raw: String): Iban =
      from(raw).fold(
        message => throw new IllegalArgumentException(message),
        identity
      )

  object Money:
    def apply(value: BigDecimal): Money = value

    def parse(raw: String): Either[String, Money] =
      Try(BigDecimal(raw.trim)).toEither.left.map(_ => "montant illisible")

    val zero: Money = BigDecimal(0)

  extension (code: BankCode)
    @targetName("bankCodeValue")
    def value: String = code

  extension (iban: Iban)
    @targetName("ibanValue")
    def value: String = iban

    def country: String = iban.take(2)

    def bankSegment: String = iban.slice(4, 9)

  extension (money: Money)
    def value: BigDecimal = money

    @targetName("addMoney")
    def +(other: Money): Money = money + other

    @targetName("subtractMoney")
    def -(other: Money): Money = money - other

    @targetName("multiplyMoneyByRate")
    def *(rate: BigDecimal): Money = money * rate

    def abs: Money = money.abs

    def isPositive: Boolean = money > 0

    def format: String =
      val Array(integer, decimals) = money
        .setScale(2, RoundingMode.HALF_UP)
        .bigDecimal
        .toPlainString
        .split("\\.")
      val sign = if integer.startsWith("-") then "-" else ""
      val digits = integer.stripPrefix("-")
      val grouped = digits.reverse.grouped(3).mkString(" ").reverse
      s"$sign$grouped.$decimals DH"

  given Numeric[Money] with
    def plus(x: Money, y: Money): Money = x + y
    def minus(x: Money, y: Money): Money = x - y
    def times(x: Money, y: Money): Money = x * y.value
    def negate(x: Money): Money = -x.value
    def fromInt(x: Int): Money = BigDecimal(x)
    def parseString(str: String): Option[Money] = Money.parse(str).toOption
    def toInt(x: Money): Int = x.value.toInt
    def toLong(x: Money): Long = x.value.toLong
    def toFloat(x: Money): Float = x.value.toFloat
    def toDouble(x: Money): Double = x.value.toDouble
    def compare(x: Money, y: Money): Int = x.value.compare(y.value)
