package clearing.v23

import clearing.v22.{
  NumberedLine,
  TypedCsvParser,
  TypedTransactionLine,
  V22Config,
  V22Error,
  V22Validation
}

object ForEquivalence:
  def withFor(
    config: V22Config,
    seenIds: Set[Int]
  )(
    line: NumberedLine
  ): Either[V22Error, TypedTransactionLine] =
    for
      parsed <- TypedCsvParser.parse(line)
      validated <- V22Validation.validate(config, seenIds)(parsed)
    yield validated

  def withFlatMap(
    config: V22Config,
    seenIds: Set[Int]
  )(
    line: NumberedLine
  ): Either[V22Error, TypedTransactionLine] =
    TypedCsvParser
      .parse(line)
      .flatMap: parsed =>
        V22Validation
          .validate(config, seenIds)(parsed)
          .map(validated => validated)

  def loggerWithFlatMap(
    config: V22Config,
    seenIds: Set[Int]
  )(
    line: NumberedLine
  ): MonadicLogger[LoggedRailwayLab.Rail] =
    LoggedRailwayLab.logParse(line).flatMap: parsed =>
      LoggedRailwayLab
        .logValidate(config, seenIds)(parsed)
        .flatMap: validated =>
          LoggedRailwayLab
            .logSave(validated)
            .map(saved => saved)
