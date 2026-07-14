package clearing.v31

import java.time.{Duration, Instant, LocalDate}
import java.util.concurrent.{CompletableFuture, Executors}
import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class S16OperationalLabsSpec extends AnyFlatSpec with Matchers:
  private val date = LocalDate.parse("2026-07-14")
  private val instant = Instant.parse("2026-07-14T12:00:00Z")

  "RepositoryPerformanceLab" should "mesurer trois runs après warm-up pour chaque mode" in:
    val active = new AtomicInteger(0)
    val maximum = new AtomicInteger(0)
    val save: Int => java.util.concurrent.CompletionStage[Unit] = _ =>
      val current = active.incrementAndGet()
      maximum.accumulateAndGet(current, Math.max)
      Thread.sleep(2)
      active.decrementAndGet()
      CompletableFuture.completedFuture(())

    val report = RepositoryPerformanceLab.measure(
      (1 to 100).toList,
      save,
      repetitions = 3,
      parallelism = 8
    )

    report.sampleSize shouldBe 100
    report.parallelism shouldBe 8
    report.measurements.map(_.mode) should contain theSameElementsAs
      List.fill(3)(BenchmarkMode.Sequential) :::
        List.fill(3)(BenchmarkMode.Parallel)
    report.measurements.map(_.iteration).toSet shouldBe Set(1, 2, 3)
    all(report.measurements.map(_.succeeded)) shouldBe 100
    all(report.measurements.map(_.failed)) shouldBe 0
    all(report.measurements.map(_.durationMillis)) should be >= 0.0
    maximum.get() should be > 1
    maximum.get() should be <= 8

  "PositionReconciliation" should "retrouver la projection depuis les mouvements" in:
    val movements = List(
      BankMovement("AWB", date, "tx:1", "OUT", "CIH", 12, "MAD", instant),
      BankMovement("AWB", date, "tx:2", "IN", "BCP", 7, "MAD", instant)
    )

    PositionReconciliation.calculate("AWB", date, movements) shouldBe
      BankPosition("AWB", date, BigDecimal(-5), 2)

  "DashboardScheduler" should "rafraîchir trois fois sans chevauchement puis se fermer" in:
    val active = new AtomicInteger(0)
    val maximum = new AtomicInteger(0)
    val refreshNumber = new AtomicInteger(0)
    val worker = Executors.newCachedThreadPool()
    val scheduler = DashboardScheduler(Executors.newScheduledThreadPool(2))
    val load = () => CompletableFuture.supplyAsync(
      () =>
        val current = active.incrementAndGet()
        maximum.accumulateAndGet(current, Math.max)
        try
          Thread.sleep(25)
          val index = refreshNumber.incrementAndGet()
          List(
            DashboardRow(
              "AWB",
              date,
              BigDecimal(index),
              instant,
              instant.plusSeconds(index.toLong),
              index.toLong
            )
          )
        finally active.decrementAndGet(),
      worker
    )

    try
      val refreshes = scheduler
        .run(load, Duration.ofMillis(5), maxRefreshes = 3)
        .toCompletableFuture
        .get()

      refreshes should have size 3
      refreshes.flatten.map(_.position) shouldBe List(1, 2, 3)
      maximum.get() shouldBe 1
    finally
      scheduler.close()
      worker.shutdownNow()

    scheduler.isShutdown shouldBe true
