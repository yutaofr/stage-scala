package clearing.model

import scala.annotation.targetName

opaque type TransactionId = String

object TransactionId:
  def from(value: String): Either[String, TransactionId] =
    Option(value).map(_.trim).filter(_.nonEmpty).toRight("transactionId vide")

  def unsafe(value: String): TransactionId = value

  extension (value: TransactionId)
    @targetName("transactionIdValue")
    def value: String = value

opaque type BankCode = String

object BankCode:
  def from(value: String): Either[String, BankCode] =
    Option(value)
      .map(_.trim.toUpperCase)
      .filter(code => code.matches("[A-Z0-9]{3,8}"))
      .toRight(s"code banque invalide: $value")

  def unsafe(value: String): BankCode = value

  extension (value: BankCode)
    @targetName("bankCodeValue")
    def value: String = value

opaque type Money = BigDecimal

object Money:
  val zero: Money = BigDecimal(0)

  def apply(value: BigDecimal): Money = value

  def positive(value: BigDecimal): Either[String, Money] =
    Either.cond(value > 0, value, s"montant non positif: $value")

  extension (value: Money)
    @targetName("moneyValue")
    def value: BigDecimal = value
    def +(other: Money): Money = value + other
    def -(other: Money): Money = value - other
    def unary_- : Money = -value
    def isPositive: Boolean = value > 0
    def abs: Money = value.abs
    def format: String = f"$value%,.2f DH"

  given Numeric[Money] with
    def plus(x: Money, y: Money): Money = x + y
    def minus(x: Money, y: Money): Money = x - y
    def times(x: Money, y: Money): Money = Money(x * y)
    def negate(x: Money): Money = -x
    def fromInt(x: Int): Money = Money(BigDecimal(x))
    def parseString(str: String): Option[Money] = str.toDoubleOption.map(v => Money(BigDecimal(v)))
    def toInt(x: Money): Int = x.toInt
    def toLong(x: Money): Long = x.toLong
    def toFloat(x: Money): Float = x.toFloat
    def toDouble(x: Money): Double = x.toDouble
    def compare(x: Money, y: Money): Int = x.compare(y)

enum TransactionStatus:
  case Pending, Validated, Rejected

enum TransactionType:
  case Transfer, Withdrawal, Check

final case class Transaction(
  id: TransactionId,
  sender: BankCode,
  receiver: BankCode,
  amount: Money,
  status: TransactionStatus = TransactionStatus.Pending,
  transactionType: TransactionType = TransactionType.Transfer
)
