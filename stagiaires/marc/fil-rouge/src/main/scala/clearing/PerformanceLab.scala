package clearing

import scala.util.Random

object PerformanceLab:
  def measureMillis[A](task: => A): (A, Long) =
    val startedAt = System.currentTimeMillis()
    val result = task
    val duration = System.currentTimeMillis() - startedAt
    (result, duration)

  def pipelineEager(
    transactions: Vector[TransactionV2.Transaction]
  ): Vector[TransactionV2.Transaction] =
    transactions
      .filter(_._4 > 0)
      .map { case (id, sender, receiver, amount, transactionType) =>
        (id, sender, receiver, amount, transactionType.toUpperCase)
      }
      .map { case (id, sender, receiver, amount, transactionType) =>
        (
          id,
          sender,
          receiver,
          amount.setScale(2, BigDecimal.RoundingMode.HALF_UP),
          transactionType
        )
      }
      .filter(_._4 >= 10)
      .sortBy(_._4)

  def pipelineWithView(
    transactions: Vector[TransactionV2.Transaction]
  ): Vector[TransactionV2.Transaction] =
    transactions
      .view
      .filter(_._4 > 0)
      .map { case (id, sender, receiver, amount, transactionType) =>
        (id, sender, receiver, amount, transactionType.toUpperCase)
      }
      .map { case (id, sender, receiver, amount, transactionType) =>
        (
          id,
          sender,
          receiver,
          amount.setScale(2, BigDecimal.RoundingMode.HALF_UP),
          transactionType
        )
      }
      .filter(_._4 >= 10)
      .toVector
      .sortBy(_._4)

  def nettingOnListAndVector(
    transactions: List[TransactionV2.Transaction]
  ): (Map[String, BigDecimal], Map[String, BigDecimal]) =
    (
      NettingCalculator.calculate(transactions),
      NettingCalculator.calculate(transactions.toVector)
    )

  def totalAmount(
    transactions: Iterable[TransactionV2.Transaction]
  ): BigDecimal =
    transactions.foldLeft(BigDecimal(0))((total, tx) => total + tx._4)

  def safeSum(size: Int): Long =
    (1 to size).foldLeft(0L)((sum, value) => sum + value.toLong)

  def unsafeRecursiveSum(values: List[Int]): Long =
    values match
      case Nil          => 0L
      case head :: tail => head.toLong + unsafeRecursiveSum(tail)

  def stressNetting(size: Int, seed: Long): Map[String, BigDecimal] =
    NettingCalculator.calculate(
      TransactionV2.generateBatch(size, new Random(seed))
    )

  def repeatedMiddleAccess(
    transactions: Seq[TransactionV2.Transaction],
    repetitions: Int
  ): BigDecimal =
    val middle = transactions.size / 2
    (1 to repetitions).foldLeft(BigDecimal(0)) { (total, _) =>
      total + transactions(middle)._4
    }

  def report(size: Int, seed: Long = 0L): String =
    val transactions = TransactionV2.generateBatch(size, new Random(seed))
    val vector = transactions.toVector
    val (listPositions, listNettingMs) = measureMillis(
      NettingCalculator.calculate(transactions)
    )
    val (vectorPositions, vectorNettingMs) = measureMillis(
      NettingCalculator.calculate(vector)
    )
    val (listTotal, listTraversalMs) = measureMillis(totalAmount(transactions))
    val (vectorTotal, vectorTraversalMs) = measureMillis(totalAmount(vector))
    val (_, listHeadMs) = measureMillis(
      (1 to 1_000).foldLeft(Option.empty[TransactionV2.Transaction]) {
        (_, _) => transactions.headOption
      }
    )
    val (_, listLastMs) = measureMillis(
      (1 to 1_000).foldLeft(Option.empty[TransactionV2.Transaction]) {
        (_, _) => transactions.lastOption
      }
    )
    val (_, listIndexMs) = measureMillis(
      repeatedMiddleAccess(transactions, 1_000)
    )
    val (_, vectorIndexMs) = measureMillis(
      repeatedMiddleAccess(vector, 1_000)
    )
    val (_, eagerMs) = measureMillis(pipelineEager(vector))
    val (_, viewMs) = measureMillis(pipelineWithView(vector))

    List(
      s"Transactions : $size",
      s"Netting List : $listNettingMs ms",
      s"Netting Vector : $vectorNettingMs ms",
      s"Parcours somme List : $listTraversalMs ms",
      s"Parcours somme Vector : $vectorTraversalMs ms",
      s"Accès tête List : $listHeadMs ms",
      s"Accès fin List : $listLastMs ms",
      s"Accès médian List : $listIndexMs ms",
      s"Accès médian Vector : $vectorIndexMs ms",
      s"Pipeline strict : $eagerMs ms",
      s"Pipeline view : $viewMs ms",
      s"Sommes identiques : ${listTotal == vectorTotal}",
      s"Résultats netting identiques : ${listPositions == vectorPositions}",
      s"Solde global : ${NettingCalculator.globalNet(listPositions)} DH"
    ).mkString("\n")

@main def runPerformanceLab(arguments: String*): Unit =
  val size = arguments.headOption.flatMap(_.toIntOption).getOrElse(100_000)
  println(PerformanceLab.report(size))

@main def runStackSafetyLab(arguments: String*): Unit =
  val size = arguments.headOption.flatMap(_.toIntOption).getOrElse(1_000_000)
  val values = (1 to size).toList
  val overflowObserved =
    try
      PerformanceLab.unsafeRecursiveSum(values)
      false
    catch
      case _: StackOverflowError => true
  println(s"StackOverflow non terminal observé : $overflowObserved")
  println(s"Somme foldLeft : ${PerformanceLab.safeSum(size)}")
