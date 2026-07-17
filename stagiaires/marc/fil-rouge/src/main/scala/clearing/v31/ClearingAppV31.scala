package clearing.v31

import clearing.v30.{ProducerApp, ProducerCli, ProducerCommand, V30Reporter, ValidatedEvent}
import java.time.{Duration, Instant, LocalDate}
import scala.util.Try

final case class ReportCommand(
  bank: String,
  date: LocalDate,
  limit: Int
)

object ReportCli:
  val Usage = "Usage : report --bank ID --date YYYY-MM-DD [--limit N]"

  def parse(args: List[String]): Either[String, ReportCommand] =
    def loop(
      remaining: List[String],
      bank: Option[String],
      date: Option[LocalDate],
      limit: Int
    ): Either[String, ReportCommand] = remaining match
      case Nil =>
        (bank, date) match
          case (Some(bankValue), Some(dateValue)) =>
            Right(ReportCommand(bankValue, dateValue, limit))
          case _ => Left(Usage)
      case "--bank" :: value :: tail if value.nonEmpty =>
        loop(tail, Some(value), date, limit)
      case "--date" :: raw :: tail =>
        Try(LocalDate.parse(raw))
          .toEither
          .left
          .map(_ => Usage)
          .flatMap(value => loop(tail, bank, Some(value), limit))
      case "--limit" :: raw :: tail =>
        raw.toIntOption
          .filter(_ > 0)
          .toRight(Usage)
          .flatMap(value => loop(tail, bank, date, value))
      case _ => Left(Usage)

    loop(args, None, None, 10)

final case class BenchmarkCommand(
  samples: Int,
  repetitions: Int,
  parallelism: Int
)

object BenchmarkCli:
  val Usage =
    "Usage : benchmark [--samples N] [--repetitions N>=3] [--parallelism N]"
  private val default = BenchmarkCommand(100, 3, 8)

  def parse(args: List[String]): Either[String, BenchmarkCommand] =
    def loop(
      remaining: List[String],
      command: BenchmarkCommand
    ): Either[String, BenchmarkCommand] = remaining match
      case Nil => Right(command)
      case "--samples" :: raw :: tail =>
        positive(raw).flatMap(value => loop(tail, command.copy(samples = value)))
      case "--repetitions" :: raw :: tail =>
        raw.toIntOption
          .filter(_ >= 3)
          .toRight(Usage)
          .flatMap(value => loop(tail, command.copy(repetitions = value)))
      case "--parallelism" :: raw :: tail =>
        positive(raw).flatMap(value => loop(tail, command.copy(parallelism = value)))
      case _ => Left(Usage)

    loop(args, default)

  private def positive(raw: String): Either[String, Int] =
    raw.toIntOption.filter(_ > 0).toRight(Usage)

final case class DashboardCommand(
  banks: List[String],
  date: LocalDate,
  intervalSeconds: Int,
  refreshes: Int
)

object DashboardCli:
  val Usage =
    "Usage : dashboard --banks A,B --date YYYY-MM-DD " +
      "[--interval-seconds N] [--refreshes N]"

  def parse(args: List[String]): Either[String, DashboardCommand] =
    def loop(
      remaining: List[String],
      banks: Option[List[String]],
      date: Option[LocalDate],
      intervalSeconds: Int,
      refreshes: Int
    ): Either[String, DashboardCommand] = remaining match
      case Nil =>
        (banks, date) match
          case (Some(values), Some(value)) =>
            Right(DashboardCommand(values, value, intervalSeconds, refreshes))
          case _ => Left(Usage)
      case "--banks" :: raw :: tail =>
        val values = raw.split(',').toList.map(_.trim).filter(_.nonEmpty).distinct
        if values.nonEmpty then
          loop(tail, Some(values), date, intervalSeconds, refreshes)
        else Left(Usage)
      case "--date" :: raw :: tail =>
        Try(LocalDate.parse(raw)).toEither.left.map(_ => Usage).flatMap: value =>
          loop(tail, banks, Some(value), intervalSeconds, refreshes)
      case "--interval-seconds" :: raw :: tail =>
        positive(raw).flatMap(value => loop(tail, banks, date, value, refreshes))
      case "--refreshes" :: raw :: tail =>
        positive(raw).flatMap(value => loop(tail, banks, date, intervalSeconds, value))
      case _ => Left(Usage)

    loop(args, None, None, 5, 3)

  private def positive(raw: String): Either[String, Int] =
    raw.toIntOption.filter(_ > 0).toRight(Usage)

enum V31Command:
  case Produce(command: ProducerCommand)
  case Qualify(command: QualificationCommand)
  case Consume(command: V31ConsumerCommand)
  case Report(command: ReportCommand)
  case Benchmark(command: BenchmarkCommand)
  case Dashboard(command: DashboardCommand)

object V31Cli:
  val Usage =
    "Usage : run producer|qualify|consumer|report|benchmark|dashboard [options]"

  def parse(args: List[String]): Either[String, V31Command] = args match
    case "producer" :: tail =>
      ProducerCli.parse(tail).map(V31Command.Produce.apply)
    case "qualify" :: tail =>
      QualificationCli.parse(tail).map(V31Command.Qualify.apply)
    case "consumer" :: tail =>
      V31ConsumerCli.parse(tail).map(V31Command.Consume.apply)
    case "report" :: tail =>
      ReportCli.parse(tail).map(V31Command.Report.apply)
    case "benchmark" :: tail =>
      BenchmarkCli.parse(tail).map(V31Command.Benchmark.apply)
    case "dashboard" :: tail =>
      DashboardCli.parse(tail).map(V31Command.Dashboard.apply)
    case _ => Left(Usage)

final case class ReportView(
  position: BankPosition,
  calculatedPosition: BankPosition,
  movements: List[BankMovement],
  history: List[HistoryRow],
  topPairs: List[PairSummary]
):
  def positionsMatch: Boolean = position == calculatedPosition

object CassandraReportApp:
  def run(
    command: ReportCommand,
    settings: CassandraSettings = CassandraSettings.fromEnvironment()
  ): ReportView =
    val session = CassandraSession.open(settings)
    try
      val repository = LiveCassandraRepository(
        session,
        CassandraStatements.prepare(session)
      )
      val allMovements = await(
        repository.movementsByBank(command.bank, command.date, Int.MaxValue)
      )
      ReportView(
        await(repository.positionByBank(command.bank, command.date)),
        PositionReconciliation.calculate(command.bank, command.date, allMovements),
        allMovements.take(command.limit),
        await(repository.historyByDate(command.date)),
        await(repository.topPairsByDate(command.date, command.limit))
      )
    finally session.close()

  private def await[A](stage: java.util.concurrent.CompletionStage[A]): A =
    stage.toCompletableFuture.get()

object V31Renderer:
  def producer(report: clearing.v30.ProducerReport): String =
    s"PRODUCER_V31 attempted=${report.attempted} " +
      s"acknowledged=${report.acknowledged} failed=${report.failed}"

  def consumer(report: clearing.v30.BatchReport): String =
    s"CONSUMER_V31 published=${report.published} " +
      s"duplicates=${report.duplicates} " +
      s"failedPartitions=${report.failedPartitions.size}"

  def report(view: ReportView): String =
    s"REPORT_V31 bank=${view.position.bank} date=${view.position.date} " +
      s"position=${view.position.amount} txCount=${view.position.transactionCount} " +
      s"movementPosition=${view.calculatedPosition.amount} " +
      s"positionsMatch=${view.positionsMatch} " +
      s"movements=${view.movements.size} history=${view.history.size} " +
      s"pairs=${view.topPairs.size}"

  def benchmark(report: BenchmarkReport): String =
    val rows = report.measurements.map: measurement =>
      f"${measurement.mode}%-10s run=${measurement.iteration}%d " +
        f"duration_ms=${measurement.durationMillis}%.2f " +
        f"throughput_s=${measurement.throughputPerSecond}%.2f " +
        s"succeeded=${measurement.succeeded} failed=${measurement.failed}"
    (s"BENCHMARK_V31 samples=${report.sampleSize} parallelism=${report.parallelism}" :: rows)
      .mkString("\n")

  def dashboard(refreshes: List[List[DashboardRow]]): String =
    refreshes.zipWithIndex.flatMap: (rows, index) =>
      rows.map: row =>
        s"DASHBOARD_V31 refresh=${index + 1} bank=${row.bank} " +
          s"date=${row.date} position=${row.position} " +
          s"observedAt=${row.observedAt} lastDataAt=${row.lastDataAt} " +
          s"ageSeconds=${row.ageSeconds}"
    .mkString("\n")

object RepositoryBenchmarkApp:
  final case class Sample(identity: DurableIdentity, event: ValidatedEvent)

  def run(
    command: BenchmarkCommand,
    settings: CassandraSettings = CassandraSettings.fromEnvironment()
  ): BenchmarkReport =
    val session = CassandraSession.open(settings)
    try
      val repository = LiveCassandraRepository(
        session,
        CassandraStatements.prepare(session)
      )
      RepositoryPerformanceLab.measure(
        samples(command.samples),
        sample => repository.saveValidated(sample.identity, sample.event),
        command.repetitions,
        command.parallelism
      )
    finally session.close()

  private def samples(count: Int): List[Sample] =
    val base = Instant.parse("2026-07-14T13:00:00Z")
    (1 to count).toList.map: index =>
      val sender = if index % 2 == 0 then "AWB" else "CIH"
      val receiver = if sender == "AWB" then "CIH" else "AWB"
      Sample(
        DurableIdentity(s"benchmark:$index", s"benchmark-fingerprint-$index"),
        ValidatedEvent(
          100000 + index,
          sender,
          receiver,
          BigDecimal(index).setScale(2).toString,
          "0.00",
          "MAD",
          "TRANSFER",
          "Pending",
          "a" * 64,
          "b" * 64,
          "benchmark",
          Nil,
          base.plusSeconds(index.toLong)
        )
      )

object DashboardApp:
  def run(
    command: DashboardCommand,
    settings: CassandraSettings = CassandraSettings.fromEnvironment()
  ): List[List[DashboardRow]] =
    val session = CassandraSession.open(settings)
    val scheduler = DashboardScheduler.singleThreaded()
    try
      val repository = LiveCassandraRepository(
        session,
        CassandraStatements.prepare(session)
      )
      scheduler
        .run(
          () => DashboardLoader.load(repository, command.banks, command.date),
          Duration.ofSeconds(command.intervalSeconds.toLong),
          command.refreshes
        )
        .toCompletableFuture
        .get()
    finally
      try scheduler.close()
      finally session.close()

object ClearingAppV31:
  def run(command: V31Command): Unit = command match
    case V31Command.Produce(producerCommand) =>
      V30Reporter.print(V31Renderer.producer(ProducerApp.run(producerCommand)))
    case V31Command.Qualify(qualificationCommand) =>
      val producer = V31QualificationProducer.live(
        qualificationCommand.bootstrapServers
      )
      try
        V30Reporter.print(
          V31Renderer.producer(
            producer.send(
              V31QualificationScenario.records(qualificationCommand.seed)
            )
          )
        )
      finally producer.close()
    case V31Command.Consume(consumerCommand) =>
      consumerCommand.maxRecords match
        case None =>
          V31ConsumerApp.run(
            consumerCommand.bootstrapServers,
            consumerCommand.groupId
          )
        case Some(maxRecords) =>
          val loop = KafkaConsumerLoopV31.live(
            consumerCommand.bootstrapServers,
            consumerCommand.groupId
          )
          try
            V30Reporter.print(
              V31Renderer.consumer(
                V31ConsumerApp.runBounded(loop, maxRecords)
              )
            )
          finally loop.close()
    case V31Command.Report(reportCommand) =>
      V30Reporter.print(V31Renderer.report(CassandraReportApp.run(reportCommand)))
    case V31Command.Benchmark(benchmarkCommand) =>
      V30Reporter.print(
        V31Renderer.benchmark(RepositoryBenchmarkApp.run(benchmarkCommand))
      )
    case V31Command.Dashboard(dashboardCommand) =>
      V30Reporter.print(V31Renderer.dashboard(DashboardApp.run(dashboardCommand)))

@main def runClearingAppV31(args: String*): Unit =
  V31Cli.parse(args.toList) match
    case Left(error)    => V30Reporter.print(error)
    case Right(command) => ClearingAppV31.run(command)
