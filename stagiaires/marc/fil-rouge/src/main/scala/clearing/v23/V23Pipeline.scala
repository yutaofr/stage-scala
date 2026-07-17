package clearing.v23

import clearing.v22.{
  ClearingResult,
  CsvSerializers,
  ExportEngine,
  HashBoundary,
  JsonSerializers,
  OutputFormat,
  TypedRailwayEngine,
  V22Config,
  XmlSerializers
}
import clearing.v22.DomainTypes.*
import clearing.v22.DomainTypes.given

final case class V23Execution(result: ClearingResult, report: String)

object V23Pipeline:
  def runPure(
    config: V22Config,
    hash: HashBoundary,
    format: OutputFormat
  )(
    raw: String
  ): MonadicLogger[V23Execution] =
    for
      input <- observeInput(raw)
      result <- clear(config, hash)(input)
      audited <- auditNetting(result)
      report <- `export`(format)(audited)
    yield V23Execution(audited, report)

  private def observeInput(raw: String): MonadicLogger[String] =
    val lineCount = if raw.isEmpty then 0 else raw.linesIterator.size
    MonadicLogger(raw, List(s"Entrée observée : $lineCount lignes"))

  private def clear(
    config: V22Config,
    hash: HashBoundary
  )(
    raw: String
  ): MonadicLogger[ClearingResult] =
    val result = TypedRailwayEngine.process(config, hash)(raw)
    MonadicLogger(
      result,
      List(
        s"Clearing terminé : ${result.transactions.size} succès, " +
          s"${result.rejections.size} rejets"
      )
    )

  private def auditNetting(
    result: ClearingResult
  ): MonadicLogger[ClearingResult] =
    val positions = V23Netting.positions(result.transactions)
    val globalIsZero = positions.values.sum == Money.zero
    val observation =
      if globalIsZero then "Netting audité : somme globale nulle"
      else "Netting audité : somme globale non nulle"

    MonadicLogger(result.copy(positions = positions), List(observation))

  private def `export`(
    format: OutputFormat
  )(
    result: ClearingResult
  ): MonadicLogger[String] =
    val report = format match
      case OutputFormat.Json =>
        import JsonSerializers.given
        ExportEngine.`export`(result)
      case OutputFormat.Csv =>
        import CsvSerializers.given
        ExportEngine.`export`(result)
      case OutputFormat.Xml =>
        import XmlSerializers.given
        ExportEngine.`export`(result)

    MonadicLogger(
      report,
      List(s"Export ${format.toString.toUpperCase} terminé")
    )
