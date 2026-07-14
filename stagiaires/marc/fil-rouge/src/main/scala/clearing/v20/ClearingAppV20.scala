package clearing.v20

import java.util.Locale

enum V20Profile:
  case MAD, EUR

  def config: PureEngineConfig = this match
    case MAD => PureEngineProfiles.clearingMAD
    case EUR => PureEngineProfiles.clearingEUR

object V20Profile:
  def parse(value: String): Option[V20Profile] =
    V20Profile.values.find(
      _.toString == value.trim.toUpperCase(Locale.ROOT)
    )

case class V20Command(path: String, profile: V20Profile)

object V20Cli:
  val DefaultPath = "transactions-v20.csv"
  val usage =
    "Usage : run [chemin.csv] | run --profile MAD|EUR [chemin.csv]"

  def parse(arguments: List[String]): Option[V20Command] =
    arguments match
      case Nil => Some(V20Command(DefaultPath, V20Profile.MAD))
      case path :: Nil if !path.startsWith("--") =>
        Some(V20Command(path, V20Profile.MAD))
      case "--profile" :: profile :: Nil =>
        V20Profile.parse(profile).map(V20Command(DefaultPath, _))
      case "--profile" :: profile :: path :: Nil if !path.startsWith("--") =>
        V20Profile.parse(profile).map(V20Command(path, _))
      case _ => None

enum V20Execution:
  case Completed(report: PureClearingReport)
  case ReadFailed(path: String, reason: String)

object ClearingReporter:
  def print(report: PureClearingReport): Unit =
    println(PureClearingRenderer.render(report))

  def printFailure(path: String, reason: String): Unit =
    Console.err.println(s"LECTURE_ECHOUEE|$path|$reason")

  def printUsage(): Unit =
    Console.err.println(V20Cli.usage)

object ClearingAppV20:
  def process(command: V20Command): V20Execution =
    IOBridge.read(command.path) match
      case FileReadResult.Success(_, content) =>
        V20Execution.Completed(
          PureClearingEngine.configure(command.profile.config)(content)
        )
      case FileReadResult.Failure(path, reason) =>
        V20Execution.ReadFailed(path, reason)

  def run(command: V20Command): V20Execution =
    val execution = process(command)
    execution match
      case V20Execution.Completed(report) => ClearingReporter.print(report)
      case V20Execution.ReadFailed(path, reason) =>
        ClearingReporter.printFailure(path, reason)
    execution

@main def runClearingAppV20(arguments: String*): Unit =
  V20Cli.parse(arguments.toList) match
    case Some(command) => ClearingAppV20.run(command)
    case None          => ClearingReporter.printUsage()
