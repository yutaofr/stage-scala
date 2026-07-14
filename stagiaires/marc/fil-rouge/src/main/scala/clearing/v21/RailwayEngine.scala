package clearing.v21

import clearing.model.*
import clearing.v20.{DataCleaner, PreparedTransaction, PureNettingCalculator}

private case class ConvertedRail(
  rail: RailTransaction,
  settlementAmount: BigDecimal,
  referenceCurrency: Currency
)

private case class ChargedRail(
  converted: ConvertedRail,
  fee: BigDecimal
)

object RailwayEngine:
  def processLine(
    config: V21Config,
    seenIds: Set[Int],
    hash: HashBoundary
  )(
    line: NumberedLine
  ): Either[ClearingError, RailSuccess] =
    for
      parsed <- EitherCsvParser.parse(line)
      valid <- RailValidation.validate(config, seenIds)(parsed)
      recovered <- RailRecovery.recover(
        config.labelsByTransactionId.get(valid.transaction.id)
      )(valid)
      converted <- applyForex(config)(recovered)
      charged <- applyFee(config)(converted)
      secured <- anonymize(hash)(charged)
    yield secured

  def process(
    config: V21Config,
    hash: HashBoundary
  )(
    input: String
  ): V21Report =
    val lines =
      if input.isEmpty then Nil
      else
        input.linesIterator.zipWithIndex
          .map((value, index) => NumberedLine(index + 1, value))
          .toList
    val (_, reversedResults) = lines.foldLeft(
      (Set.empty[Int], List.empty[LineResult])
    ): (state, line) =>
      val result = processLine(config, state._1, hash)(line)
      val nextSeen = result.fold(
        _ => state._1,
        success => state._1 + success.prepared.id
      )
      (nextSeen, LineResult(line.lineNumber, result) :: state._2)
    val lineResults = reversedResults.reverse
    val (errors, successes) =
      lineResults.map(_.result).partitionMap(identity)
    val prepared = successes.map(_.prepared)

    V21Report(
      referenceCurrency = config.referenceCurrency,
      lineResults = lineResults,
      successes = successes,
      errors = errors,
      positions = PureNettingCalculator.positions(prepared),
      feesByBank = feesByBank(prepared),
      statistics = statistics(errors, successes)
    )

  private def applyForex(
    config: V21Config
  )(
    rail: RailTransaction
  ): Either[ClearingError, ConvertedRail] =
    val transaction = rail.transaction
    config.ratesToReference.get(transaction.currency) match
      case None =>
        Left(
          ConfigurationError(
            rail.lineNumber,
            transaction.id,
            "FX_RATE_MISSING",
            s"taux ${transaction.currency} absent"
          )
        )
      case Some(rate) if rate <= 0 =>
        Left(
          ConfigurationError(
            rail.lineNumber,
            transaction.id,
            "FX_RATE_INVALID",
            s"taux ${transaction.currency} invalide"
          )
        )
      case Some(rate) =>
        Right(
          ConvertedRail(
            rail,
            DataCleaner.formatAmount(transaction.amount * rate),
            config.referenceCurrency
          )
        )

  private def applyFee(
    config: V21Config
  )(
    converted: ConvertedRail
  ): Either[ClearingError, ChargedRail] =
    val transaction = converted.rail.transaction
    config.feeRates.get(transaction.sender) match
      case None =>
        Left(
          ConfigurationError(
            converted.rail.lineNumber,
            transaction.id,
            "FEE_RATE_MISSING",
            s"frais ${transaction.sender} absents"
          )
        )
      case Some(rate) if rate < 0 =>
        Left(
          ConfigurationError(
            converted.rail.lineNumber,
            transaction.id,
            "FEE_RATE_INVALID",
            s"frais ${transaction.sender} invalides"
          )
        )
      case Some(rate) =>
        Right(
          ChargedRail(
            converted,
            DataCleaner.formatAmount(converted.settlementAmount * rate)
          )
        )

  private def anonymize(
    hash: HashBoundary
  )(
    charged: ChargedRail
  ): Either[ClearingError, RailSuccess] =
    val rail = charged.converted.rail
    val transaction = rail.transaction
    for
      sourceHash <- hash(transaction.sourceIban)
      destinationHash <- hash(transaction.destinationIban)
      label <- rail.label.toRight(
        ConfigurationError(
          rail.lineNumber,
          transaction.id,
          "LABEL_UNRECOVERED",
          "libellé non récupéré"
        )
      )
    yield RailSuccess(
      lineNumber = rail.lineNumber,
      prepared = PreparedTransaction(
        id = transaction.id,
        sender = transaction.sender,
        receiver = transaction.receiver,
        settlementAmount = charged.converted.settlementAmount,
        transactionType = transaction.transactionType,
        status = transaction.status,
        referenceCurrency = charged.converted.referenceCurrency,
        fee = charged.fee,
        sourceIbanHash = sourceHash,
        destinationIbanHash = destinationHash
      ),
      label = label,
      warnings = rail.warnings
    )

  private def feesByBank(
    transactions: List[PreparedTransaction]
  ): Map[String, BigDecimal] =
    transactions.foldLeft(Map.empty[String, BigDecimal]):
      (fees, transaction) =>
        fees.updatedWith(transaction.sender):
          case Some(total) =>
            Some(DataCleaner.formatAmount(total + transaction.fee))
          case None => Some(transaction.fee)

  private def statistics(
    errors: List[ClearingError],
    successes: List[RailSuccess]
  ): ErrorStatistics =
    val counts = errors.foldLeft(ErrorStatistics(0, 0, 0, 0, 0)):
      (current, error) =>
        error match
          case _: ParsingError =>
            current.copy(parsing = current.parsing + 1)
          case _: ValidationError =>
            current.copy(validation = current.validation + 1)
          case _: BusinessError =>
            current.copy(business = current.business + 1)
          case _: SystemError | _: HighLevelError =>
            current.copy(technical = current.technical + 1)

    counts.copy(warnings = successes.map(_.warnings.size).sum)
