package clearing

import scala.annotation.tailrec

object TransactionSearch:
  @tailrec
  def findFirstAbove(
    transactions: List[BigDecimal],
    threshold: BigDecimal
  ): Option[BigDecimal] =
    transactions match
      case Nil => None
      case head :: tail =>
        if head > threshold then Some(head)
        else findFirstAbove(tail, threshold)

  def countAbove(
    transactions: List[BigDecimal],
    threshold: BigDecimal
  ): Int =
    @tailrec
    def loop(remaining: List[BigDecimal], count: Int): Int =
      remaining match
        case Nil => count
        case head :: tail =>
          val nextCount = if head > threshold then count + 1 else count
          loop(tail, nextCount)

    loop(transactions, 0)

  def sumAbove(
    transactions: List[BigDecimal],
    threshold: BigDecimal
  ): BigDecimal =
    @tailrec
    def loop(
      remaining: List[BigDecimal],
      total: BigDecimal
    ): BigDecimal =
      remaining match
        case Nil => total
        case head :: tail =>
          val nextTotal = if head > threshold then total + head else total
          loop(tail, nextTotal)

    loop(transactions, BigDecimal(0))
