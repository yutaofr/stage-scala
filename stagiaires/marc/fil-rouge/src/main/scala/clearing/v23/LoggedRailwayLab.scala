package clearing.v23

import clearing.v22.{
  NumberedLine,
  TypedCsvParser,
  TypedTransactionLine,
  V22Config,
  V22Error,
  V22Validation
}

object LoggedRailwayLab:
  type Rail = Either[V22Error, TypedTransactionLine]

  def logParse(line: NumberedLine): MonadicLogger[Rail] =
    val parsed = TypedCsvParser.parse(line)
    val log = parsed.fold(
      _ => s"Parsing rejeté : ligne ${line.lineNumber}",
      value => s"Parsing OK : ${value.transaction.id}"
    )
    MonadicLogger(parsed, List(log))

  def logValidate(
    config: V22Config,
    seenIds: Set[Int]
  )(
    parsed: Rail
  ): MonadicLogger[Rail] = parsed match
    case left @ Left(_) =>
      MonadicLogger(left, List("Validation ignorée : rail gauche"))
    case Right(value) =>
      val validated = V22Validation.validate(config, seenIds)(value)
      val log = validated.fold(
        _ => s"Validation rejetée : ${value.transaction.id}",
        _ => s"Validation OK : ${value.transaction.id}"
      )
      MonadicLogger(validated, List(log))

  def logSave(validated: Rail): MonadicLogger[Rail] = validated match
    case left @ Left(_) =>
      MonadicLogger(left, List("Sauvegarde ignorée : rail gauche"))
    case right @ Right(value) =>
      MonadicLogger(
        right,
        List(s"Sauvegarde observée : ${value.transaction.id}")
      )

  def pipeline(
    config: V22Config,
    seenIds: Set[Int]
  )(
    line: NumberedLine
  ): MonadicLogger[Rail] =
    for
      parsed <- logParse(line)
      validated <- logValidate(config, seenIds)(parsed)
      saved <- logSave(validated)
    yield saved
