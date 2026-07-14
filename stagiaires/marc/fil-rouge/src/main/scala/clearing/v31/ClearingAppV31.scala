package clearing.v31

import clearing.v30.{ProducerApp, ProducerCli, ProducerCommand, V30Reporter}
import java.time.LocalDate
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

enum V31Command:
  case Produce(command: ProducerCommand)
  case Qualify(command: QualificationCommand)
  case Consume(command: V31ConsumerCommand)
  case Report(command: ReportCommand)

object V31Cli:
  val Usage = "Usage : run producer|qualify|consumer|report [options]"

  def parse(args: List[String]): Either[String, V31Command] = args match
    case "producer" :: tail =>
      ProducerCli.parse(tail).map(V31Command.Produce.apply)
    case "qualify" :: tail =>
      QualificationCli.parse(tail).map(V31Command.Qualify.apply)
    case "consumer" :: tail =>
      V31ConsumerCli.parse(tail).map(V31Command.Consume.apply)
    case "report" :: tail =>
      ReportCli.parse(tail).map(V31Command.Report.apply)
    case _ => Left(Usage)

final case class ReportView(
  position: BankPosition,
  movements: List[BankMovement],
  history: List[HistoryRow],
  topPairs: List[PairSummary]
)

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
      ReportView(
        await(repository.positionByBank(command.bank, command.date)),
        await(
          repository.movementsByBank(command.bank, command.date, command.limit)
        ),
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
      s"movements=${view.movements.size} history=${view.history.size} " +
      s"pairs=${view.topPairs.size}"

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

@main def runClearingAppV31(args: String*): Unit =
  V31Cli.parse(args.toList) match
    case Left(error)    => V30Reporter.print(error)
    case Right(command) => ClearingAppV31.run(command)
