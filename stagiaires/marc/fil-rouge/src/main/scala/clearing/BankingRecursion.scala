package clearing

import scala.annotation.tailrec

object BankingRecursion:
  def calculerSoldeHistorique(
    transactions: List[BigDecimal]
  ): List[BigDecimal] =
    @tailrec
    def loop(
      remaining: List[BigDecimal],
      balance: BigDecimal,
      reversedHistory: List[BigDecimal]
    ): List[BigDecimal] =
      remaining match
        case Nil => reversedHistory.reverse
        case amount :: tail =>
          val nextBalance = balance + amount
          loop(tail, nextBalance, nextBalance :: reversedHistory)

    loop(transactions, BigDecimal(0), Nil)

  def trouverMomentIncident(
    transactions: List[BigDecimal],
    soldeInitial: BigDecimal
  ): Option[Int] =
    @tailrec
    def loop(
      remaining: List[BigDecimal],
      balance: BigDecimal,
      index: Int
    ): Option[Int] =
      remaining match
        case Nil => None
        case amount :: tail =>
          val nextBalance = balance + amount
          if nextBalance < -500 then Some(index)
          else loop(tail, nextBalance, index + 1)

    loop(transactions, soldeInitial, 0)

  def signatureExiste(signatures: List[String], target: String): Boolean =
    @tailrec
    def loop(remaining: List[String]): Boolean =
      remaining match
        case Nil                  => false
        case head :: _ if head == target => true
        case _ :: tail            => loop(tail)

    loop(signatures)

  def fibonacci(index: Int): BigInt =
    require(index >= 0, "L'index Fibonacci doit être positif ou nul")

    @tailrec
    def loop(remaining: Int, current: BigInt, next: BigInt): BigInt =
      if remaining == 0 then current
      else loop(remaining - 1, next, current + next)

    loop(index, BigInt(0), BigInt(1))
