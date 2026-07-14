package clearing.v12

import clearing.model.Transaction
import scala.collection.parallel.CollectionConverters.*

case class Timed[A](value: A, durationMillis: Long)

case class NettingComparison(
  listPositions: Map[String, BigDecimal],
  vectorPositions: Map[String, BigDecimal],
  listDurationMillis: Long,
  vectorDurationMillis: Long
)

case class PipelineDigest(
  transactionCount: Int,
  totalAmount: BigDecimal,
  idChecksum: Long
)

case class ScalabilityReport(
  transactionCount: Int,
  durations: Map[String, Long],
  nettingEquivalent: Boolean,
  pipelineEquivalent: Boolean,
  cpuEquivalent: Boolean,
  balanced: Boolean,
  maxMemoryMb: Long
)

object ScalabilityCli:
  val usage: String =
    "Usage : runMain clearing.v12.runScalabilityLab [taille-positive-ou-nulle]"

  def parseSize(arguments: List[String]): Option[Int] =
    arguments match
      case Nil => Some(1_000_000)
      case rawSize :: Nil => rawSize.toIntOption.filter(_ >= 0)
      case _ => None

object ScalabilityLab:
  def measureMillis[A](task: => A): Timed[A] =
    val startedAt = System.nanoTime()
    val value = task
    val elapsedNanos = System.nanoTime() - startedAt
    Timed(value, elapsedNanos / 1_000_000L)

  def compareNetting(
    transactions: Vector[Transaction]
  ): NettingComparison =
    comparePreparedNetting(transactions.toList, transactions)

  def comparePreparedNetting(
    listTransactions: List[Transaction],
    vectorTransactions: Vector[Transaction]
  ): NettingComparison =
    val listTimed = measureMillis(
      MultilateralNetting.computePositions(listTransactions)
    )
    val vectorTimed = measureMillis(
      MultilateralNetting.computePositions(vectorTransactions)
    )

    NettingComparison(
      listPositions = listTimed.value,
      vectorPositions = vectorTimed.value,
      listDurationMillis = listTimed.durationMillis,
      vectorDurationMillis = vectorTimed.durationMillis
    )

  private def digest(
    transactions: IterableOnce[Transaction]
  ): PipelineDigest =
    transactions.iterator.foldLeft(
      PipelineDigest(0, BigDecimal(0), 0L)
    ): (current, transaction) =>
      PipelineDigest(
        transactionCount = current.transactionCount + 1,
        totalAmount = current.totalAmount + transaction.amount,
        idChecksum = current.idChecksum + transaction.id.toLong
      )

  def strictPipelineDigest(
    transactions: Vector[Transaction]
  ): PipelineDigest =
    digest(
      transactions
        .filter(_.amount > 0)
        .map(transaction =>
          transaction.copy(
            amount = transaction.amount * BigDecimal("1.01")
          )
        )
        .filter(_.id % 2 == 0)
        .map(transaction =>
          transaction.copy(
            amount = transaction.amount.setScale(
              2,
              BigDecimal.RoundingMode.HALF_UP
            )
          )
        )
    )

  def viewPipelineDigest(
    transactions: Vector[Transaction]
  ): PipelineDigest =
    digest(
      transactions.view
        .filter(_.amount > 0)
        .map(transaction =>
          transaction.copy(
            amount = transaction.amount * BigDecimal("1.01")
          )
        )
        .filter(_.id % 2 == 0)
        .map(transaction =>
          transaction.copy(
            amount = transaction.amount.setScale(
              2,
              BigDecimal.RoundingMode.HALF_UP
            )
          )
        )
    )

  private def cpuScore(transaction: Transaction, rounds: Int): Long =
    (0 until rounds).foldLeft(transaction.id.toLong): (hash, round) =>
      java.lang.Long.rotateLeft(
        hash * 31L + transaction.amount.bigDecimal.unscaledValue.longValue,
        7
      ) ^ round.toLong

  def sequentialScores(
    transactions: Vector[Transaction],
    rounds: Int
  ): Vector[Long] =
    require(rounds >= 0, "Le nombre de tours doit être positif ou nul")
    transactions.map(transaction => cpuScore(transaction, rounds))

  def parallelScores(
    transactions: Vector[Transaction],
    rounds: Int
  ): Vector[Long] =
    require(rounds >= 0, "Le nombre de tours doit être positif ou nul")
    transactions.par
      .map(transaction => cpuScore(transaction, rounds))
      .seq
      .toVector

  def benchmark(
    size: Int,
    cpuSampleSize: Int = 1000,
    cpuRounds: Int = 1000
  ): ScalabilityReport =
    require(size >= 0, "Le volume doit être positif ou nul")
    require(cpuSampleSize >= 0, "L'échantillon CPU doit être positif ou nul")

    val generated = measureMillis(
      S7TransactionGenerator.generateVector(size)
    )
    val transactions = generated.value
    val netting = compareNetting(transactions)
    val strict = measureMillis(strictPipelineDigest(transactions))
    val viewed = measureMillis(viewPipelineDigest(transactions))
    val cpuSample = transactions.take(cpuSampleSize)
    val sequential = measureMillis(
      sequentialScores(cpuSample, cpuRounds)
    )
    val parallel = measureMillis(
      parallelScores(cpuSample, cpuRounds)
    )

    ScalabilityReport(
      transactionCount = transactions.size,
      durations = Map(
        "génération" -> generated.durationMillis,
        "netting List" -> netting.listDurationMillis,
        "netting Vector" -> netting.vectorDurationMillis,
        "pipeline strict" -> strict.durationMillis,
        "pipeline view" -> viewed.durationMillis,
        "CPU séquentiel" -> sequential.durationMillis,
        "CPU parallèle" -> parallel.durationMillis
      ),
      nettingEquivalent =
        netting.listPositions == netting.vectorPositions,
      pipelineEquivalent = strict.value == viewed.value,
      cpuEquivalent = sequential.value == parallel.value,
      balanced = MultilateralNetting.isBalanced(
        netting.vectorPositions
      ),
      maxMemoryMb = Runtime.getRuntime.maxMemory() / 1024L / 1024L
    )

  def render(report: ScalabilityReport): String =
    val durationLines = report.durations.toList.sortBy(_._1).map:
      case (label, duration) => s"$label : $duration ms"

    (List(
      s"Transactions : ${report.transactionCount}",
      s"Mémoire JVM maximale : ${report.maxMemoryMb} MB"
    ) ++ durationLines ++ List(
      s"Netting identique : ${report.nettingEquivalent}",
      s"Pipeline identique : ${report.pipelineEquivalent}",
      s"CPU séquentiel/parallèle identique : ${report.cpuEquivalent}",
      s"Solde global nul : ${report.balanced}"
    )).mkString(System.lineSeparator())

@main def runScalabilityLab(arguments: String*): Unit =
  ScalabilityCli.parseSize(arguments.toList) match
    case Some(size) =>
      println(ScalabilityLab.render(ScalabilityLab.benchmark(size)))
    case None =>
      Console.err.println(ScalabilityCli.usage)
