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
      .flatMap(V22Validation.validate(config, seenIds))
