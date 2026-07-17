package clearing.v31

import java.time.{Duration, Instant, LocalDate, ZoneOffset}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CompletableFuture, CompletionStage, Executors, ScheduledExecutorService, TimeUnit}
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

enum BenchmarkMode:
  case Sequential, Parallel

final case class BenchmarkMeasurement(
  mode: BenchmarkMode,
  iteration: Int,
  durationMillis: Double,
  succeeded: Int,
  failed: Int
):
  def throughputPerSecond: Double =
    if durationMillis == 0.0 then succeeded.toDouble
    else succeeded.toDouble * 1000.0 / durationMillis

final case class BenchmarkReport(
  sampleSize: Int,
  parallelism: Int,
  measurements: List[BenchmarkMeasurement]
)

object RepositoryPerformanceLab:
  def measure[A](
    samples: List[A],
    save: A => CompletionStage[Unit],
    repetitions: Int = 3,
    parallelism: Int = 8
  ): BenchmarkReport =
    require(samples.nonEmpty, "le benchmark exige au moins un échantillon")
    require(repetitions >= 3, "le benchmark exige au moins trois mesures")
    require(parallelism > 0, "parallelism doit être strictement positif")

    val warmup = samples
    runSequential(warmup, save)
    val executor = Executors.newFixedThreadPool(parallelism)
    try
      runParallel(warmup, save, executor)
      val sequential = (1 to repetitions).toList.map: iteration =>
        timed(BenchmarkMode.Sequential, iteration, samples.size):
          runSequential(samples, save)
      val parallel = (1 to repetitions).toList.map: iteration =>
        timed(BenchmarkMode.Parallel, iteration, samples.size):
          runParallel(samples, save, executor)
      BenchmarkReport(samples.size, parallelism, sequential ::: parallel)
    finally
      executor.shutdown()
      executor.awaitTermination(30, TimeUnit.SECONDS)

  private def timed(
    mode: BenchmarkMode,
    iteration: Int,
    total: Int
  )(run: => Int): BenchmarkMeasurement =
    val started = System.nanoTime()
    val failures = run
    val duration = (System.nanoTime() - started).toDouble / 1000000.0
    BenchmarkMeasurement(mode, iteration, duration, total - failures, failures)

  private def runSequential[A](
    samples: List[A],
    save: A => CompletionStage[Unit]
  ): Int =
    samples.count(sample => !runOne(sample, save))

  private def runParallel[A](
    samples: List[A],
    save: A => CompletionStage[Unit],
    executor: java.util.concurrent.ExecutorService
  ): Int =
    val results = samples.map: sample =>
      CompletableFuture.supplyAsync(
        () => runOne(sample, save),
        executor
      )
    CompletableFuture.allOf(results*).get()
    results.count(result => !result.join())

  private def runOne[A](
    sample: A,
    save: A => CompletionStage[Unit]
  ): Boolean =
    try
      save(sample).toCompletableFuture.get()
      true
    catch case NonFatal(_) => false

object PositionReconciliation:
  def calculate(
    bank: String,
    date: LocalDate,
    movements: List[BankMovement]
  ): BankPosition =
    require(
      movements.forall(row => row.bank == bank && row.date == date),
      "les mouvements doivent appartenir à une seule partition banque/jour"
    )
    val amount = movements.map: movement =>
      movement.direction match
        case "IN"  => movement.amount
        case "OUT" => -movement.amount
        case other  => throw new IllegalArgumentException(s"direction inconnue: $other")
    BankPosition(bank, date, amount.sum, movements.map(_.eventKey).distinct.size)

final case class DashboardRow(
  bank: String,
  date: LocalDate,
  position: BigDecimal,
  lastDataAt: Instant,
  observedAt: Instant,
  ageSeconds: Long
)

object DashboardLoader:
  def load(
    repository: DurableRepository,
    banks: List[String],
    date: LocalDate,
    now: () => Instant = () => Instant.now()
  ): CompletionStage[List[DashboardRow]] =
    val reads = banks.distinct.sorted.map: bank =>
      repository
        .positionByBank(bank, date)
        .thenCombine(
          repository.movementsByBank(bank, date, 1),
          (position, movements) =>
            val observed = now()
            val lastData = movements.headOption
              .map(_.occurredAt)
              .getOrElse(date.atStartOfDay(ZoneOffset.UTC).toInstant)
            DashboardRow(
              bank,
              date,
              position.amount,
              lastData,
              observed,
              Math.max(0L, Duration.between(lastData, observed).getSeconds)
            )
        )
    CompletionStages.values(reads)

final class DashboardScheduler(
  executor: ScheduledExecutorService
) extends AutoCloseable:
  def run(
    load: () => CompletionStage[List[DashboardRow]],
    interval: Duration,
    maxRefreshes: Int
  ): CompletionStage[List[List[DashboardRow]]] =
    require(!interval.isNegative && !interval.isZero, "interval doit être positif")
    require(maxRefreshes > 0, "maxRefreshes doit être strictement positif")

    val result = new CompletableFuture[List[List[DashboardRow]]]()
    val inFlight = new AtomicBoolean(false)
    val refreshes = new java.util.concurrent.CopyOnWriteArrayList[List[DashboardRow]]()
    val task = new Runnable:
      def run(): Unit =
        if !result.isDone && inFlight.compareAndSet(false, true) then
          try
            load().whenComplete: (rows, error) =>
              if error != null then result.completeExceptionally(error)
              else
                refreshes.add(rows)
                if refreshes.size() >= maxRefreshes then
                  result.complete(refreshes.asScala.toList)
                else inFlight.set(false)
          catch
            case NonFatal(error) =>
              result.completeExceptionally(error)

    val future = executor.scheduleAtFixedRate(
      task,
      0L,
      interval.toNanos,
      TimeUnit.NANOSECONDS
    )
    result.whenComplete((_, _) => future.cancel(false))
    result

  def isShutdown: Boolean = executor.isShutdown

  override def close(): Unit =
    executor.shutdownNow()
    executor.awaitTermination(30, TimeUnit.SECONDS)

object DashboardScheduler:
  def singleThreaded(): DashboardScheduler =
    DashboardScheduler(Executors.newSingleThreadScheduledExecutor())
