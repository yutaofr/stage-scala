package clearing.v23

import clearing.v13.SecurityUtils
import clearing.v22.{
  HashBoundary,
  HashFailure,
  V22Cli,
  V22Command,
  V22Error,
  V22IO,
  V22Profiles,
  V22TechnicalError
}
import clearing.v22.DomainTypes.*
import scala.util.control.NonFatal

object ClearingAppV23:
  def run(command: V22Command): String = runSafely:
    V22IO.read(command.path).fold(
      renderError,
      content =>
        renderExecution(
          V23Pipeline.runPure(
            V22Profiles.clearingMAD,
            hashBoundary(command.simulateHashFailure),
            command.format
          )(content)
        )
    )

  def runSafely(program: => String): String =
    try program
    catch
      case NonFatal(error) =>
        renderError(
          V22TechnicalError(
            lineNumber = 0,
            transactionId = None,
            operation = "run-application",
            causeType = error.getClass.getSimpleName,
            detail = "erreur inattendue"
          )
        )

  private def renderExecution(
    execution: MonadicLogger[V23Execution]
  ): String =
    List(
      execution.value.report,
      "JOURNAL_PUR",
      execution.logs.mkString("\n")
    ).mkString("\n")

  private def renderError(error: V22Error): String =
    s"REJET : ${error.code} - ${error.message}"

  private def hashBoundary(simulateFailure: Boolean): HashBoundary = iban =>
    if simulateFailure && iban.bankSegment.startsWith("BOA") then
      Left(HashFailure.Simulated)
    else
      SecurityUtils
        .hashIbanTry(iban.value)
        .toEither
        .left
        .map(_ => HashFailure.Unavailable)
        .flatMap: rawHash =>
          IbanHash.from(rawHash).left.map(_ => HashFailure.InvalidDigest)

object V23Reporter:
  def print(report: String): Unit = println(report)

@main def runClearingAppV23(args: String*): Unit =
  val rendered = V22Cli.parse(args.toList).fold(identity, ClearingAppV23.run)
  V23Reporter.print(rendered)
