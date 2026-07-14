package clearing.v22

import clearing.model.Currency
import clearing.v22.DomainTypes.*
import scala.math.BigDecimal.RoundingMode

private case class ConvertedTransaction(
  rail: TypedTransactionLine,
  settlementAmount: Money,
  referenceCurrency: Currency
)

private case class ChargedTransaction(
  converted: ConvertedTransaction,
  fee: Money
)

object TypedRailwayEngine:
  def processLine(
    config: V22Config,
    seenIds: Set[Int],
    hash: HashBoundary
  )(
    line: NumberedLine
  ): Either[V22Error, PreparedTransaction] =
    for
      parsed <- TypedCsvParser.parse(line)
      valid <- V22Validation.validate(config, seenIds)(parsed)
      recovered <- recoverLabel(config)(valid)
      converted <- applyForex(config)(recovered)
      charged <- applyFee(config)(converted)
      secured <- anonymize(hash)(charged)
    yield secured

  def process(
    config: V22Config,
    hash: HashBoundary
  )(
    input: String
  ): ClearingResult =
    val lines =
      if input.isEmpty then Nil
      else
        input.linesIterator.zipWithIndex
          .map((value, index) => NumberedLine(index + 1, value))
          .toList
    val (_, reversedResults) = lines.foldLeft(
      (Set.empty[Int], List.empty[Either[V22Error, PreparedTransaction]])
    ): (state, line) =>
      val result = processLine(config, state._1, hash)(line)
      val nextSeen = result.fold(
        _ => state._1,
        transaction => state._1 + transaction.id
      )
      (nextSeen, result :: state._2)
    val (errors, transactions) =
      reversedResults.reverse.partitionMap(identity)

    ClearingResult(
      referenceCurrency = config.referenceCurrency,
      transactions = transactions,
      rejections = errors.map(_.toRejection),
      positions = positions(transactions),
      feesByBank = feesByBank(transactions),
      statistics = statistics(errors, transactions)
    )

  private def recoverLabel(
    config: V22Config
  )(
    rail: TypedTransactionLine
  ): Either[V22Error, TypedTransactionLine] =
    config.labelsByTransactionId.get(rail.transaction.id) match
      case Some(label) => Right(rail.copy(label = Some(label)))
      case None =>
        val defaultLabel = "NON RENSEIGNE"
        Right(
          rail.copy(
            label = Some(defaultLabel),
            warnings = rail.warnings :+
              V22Warning.MissingLabel(defaultLabel)
          )
        )

  private def applyForex(
    config: V22Config
  )(
    rail: TypedTransactionLine
  ): Either[V22Error, ConvertedTransaction] =
    val transaction = rail.transaction
    config.ratesToReference.get(transaction.currency) match
      case None =>
        Left(
          V22ConfigurationError(
            rail.lineNumber,
            transaction.id,
            "FX_RATE_MISSING",
            s"taux ${transaction.currency} absent"
          )
        )
      case Some(rate) if rate <= 0 =>
        Left(
          V22ConfigurationError(
            rail.lineNumber,
            transaction.id,
            "FX_RATE_INVALID",
            s"taux ${transaction.currency} invalide"
          )
        )
      case Some(rate) =>
        Right(
          ConvertedTransaction(
            rail,
            rounded(transaction.amount * rate),
            config.referenceCurrency
          )
        )

  private def applyFee(
    config: V22Config
  )(
    converted: ConvertedTransaction
  ): Either[V22Error, ChargedTransaction] =
    val transaction = converted.rail.transaction
    config.feeRates.get(transaction.sender) match
      case None =>
        Left(
          V22ConfigurationError(
            converted.rail.lineNumber,
            transaction.id,
            "FEE_RATE_MISSING",
            s"frais ${transaction.sender.value} absents"
          )
        )
      case Some(rate) if rate < 0 =>
        Left(
          V22ConfigurationError(
            converted.rail.lineNumber,
            transaction.id,
            "FEE_RATE_INVALID",
            s"frais ${transaction.sender.value} invalides"
          )
        )
      case Some(rate) =>
        Right(
          ChargedTransaction(
            converted,
            rounded(converted.settlementAmount * rate)
          )
        )

  private def anonymize(
    hash: HashBoundary
  )(
    charged: ChargedTransaction
  ): Either[V22Error, PreparedTransaction] =
    val rail = charged.converted.rail
    val transaction = rail.transaction
    for
      sourceHash <- hash(transaction.sourceIban).left.map(
        hashTechnicalError(rail.lineNumber, transaction.id)
      )
      destinationHash <- hash(transaction.destinationIban).left.map(
        hashTechnicalError(rail.lineNumber, transaction.id)
      )
      label <- rail.label.toRight(
        V22ConfigurationError(
          rail.lineNumber,
          transaction.id,
          "LABEL_UNRECOVERED",
          "libellé non récupéré"
        )
      )
    yield PreparedTransaction(
      id = transaction.id,
      sender = transaction.sender,
      receiver = transaction.receiver,
      settlementAmount = charged.converted.settlementAmount,
      transactionType = transaction.transactionType,
      status = transaction.status,
      referenceCurrency = charged.converted.referenceCurrency,
      fee = charged.fee,
      sourceIbanHash = sourceHash,
      destinationIbanHash = destinationHash,
      label = label,
      warnings = rail.warnings
    )

  private def hashTechnicalError(
    lineNumber: Int,
    transactionId: Int
  )(
    failure: HashFailure
  ): V22TechnicalError =
    val (causeType, detail) = failure match
      case HashFailure.Unavailable =>
        ("HashProviderFailure", "hachage impossible")
      case HashFailure.InvalidDigest =>
        ("InvalidHashDigest", "empreinte de hash invalide")
      case HashFailure.Simulated =>
        ("SimulatedHashFailure", "panne de hash simulée")

    V22TechnicalError(
      lineNumber = lineNumber,
      transactionId = Some(transactionId),
      operation = "hash-iban",
      causeType = causeType,
      detail = detail
    )

  private def positions(
    transactions: List[PreparedTransaction]
  ): Map[BankCode, Money] =
    transactions.foldLeft(Map.empty[BankCode, Money]):
      (current, transaction) =>
        val debited = current.updatedWith(transaction.sender):
          case Some(amount) => Some(amount - transaction.settlementAmount)
          case None         => Some(Money.zero - transaction.settlementAmount)
        debited.updatedWith(transaction.receiver):
          case Some(amount) => Some(amount + transaction.settlementAmount)
          case None         => Some(transaction.settlementAmount)

  private def feesByBank(
    transactions: List[PreparedTransaction]
  ): Map[BankCode, Money] =
    transactions.foldLeft(Map.empty[BankCode, Money]):
      (fees, transaction) =>
        fees.updatedWith(transaction.sender):
          case Some(total) => Some(rounded(total + transaction.fee))
          case None        => Some(transaction.fee)

  private def statistics(
    errors: List[V22Error],
    transactions: List[PreparedTransaction]
  ): ErrorStatistics =
    val counts = errors.foldLeft(ErrorStatistics(0, 0, 0, 0, 0)):
      (current, error) =>
        error.category match
          case V22ErrorCategory.Parsing =>
            current.copy(parsing = current.parsing + 1)
          case V22ErrorCategory.Validation =>
            current.copy(validation = current.validation + 1)
          case V22ErrorCategory.Business =>
            current.copy(business = current.business + 1)
          case V22ErrorCategory.Technical =>
            current.copy(technical = current.technical + 1)

    counts.copy(warnings = transactions.map(_.warnings.size).sum)

  private def rounded(amount: Money): Money =
    Money(amount.value.setScale(2, RoundingMode.HALF_UP))
