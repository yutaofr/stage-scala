package clearing.v20

import clearing.model.*
import clearing.v11.AdvancedTransactionValidator
import java.util.Locale

object PureClearingEngine:
  val splitLines: String => List[RawTransaction] = input =>
    if input.isEmpty then Nil
    else
      input.linesIterator.zipWithIndex
        .map((value, index) => RawTransaction(index + 1, value))
        .toList

  val parse: List[RawTransaction] => ParsedBatch = rawTransactions =>
    val (transactions, rejections) = rawTransactions.foldLeft(
      (List.empty[NumberedTransaction], List.empty[PureRejection])
    ): (state, raw) =>
      Transaction.fromCsv(raw.value) match
        case Some(transaction) =>
          (
            NumberedTransaction(raw.lineNumber, transaction) :: state._1,
            state._2
          )
        case None =>
          (
            state._1,
            PureRejection(
              raw.lineNumber,
              None,
              "CSV_MALFORME"
            ) :: state._2
          )

    ParsedBatch(transactions.reverse, rejections.reverse)

  def validate(config: PureEngineConfig): ParsedBatch => ValidatedBatch =
    parsed =>
      val (transactions, newRejections, _) = parsed.transactions.foldLeft(
        (
          List.empty[NumberedTransaction],
          List.empty[PureRejection],
          Set.empty[Int]
        )
      ): (state, numbered) =>
        val cleaned = DataCleaner.cleanTransaction(numbered.transaction)
        val nextSeenIds = state._3 + cleaned.id
        validationFailure(
          config,
          cleaned,
          duplicate = state._3.contains(cleaned.id)
        ) match
          case Some(error) =>
            (
              state._1,
              rejection(numbered.lineNumber, cleaned.id, error) :: state._2,
              nextSeenIds
            )
          case None =>
            (
              numbered.copy(
                transaction = cleaned.copy(status = TransactionStatus.Validated)
              ) :: state._1,
              state._2,
              nextSeenIds
            )

      ValidatedBatch(
        transactions.reverse,
        (parsed.rejections ++ newRejections.reverse).sortBy(_.lineNumber)
      )

  def prepare(config: PureEngineConfig): ValidatedBatch => PreparedBatch =
    validated =>
      val (transactions, newRejections, trace) = validated.transactions.foldLeft(
        (
          List.empty[PreparedTransaction],
          List.empty[PureRejection],
          List.empty[String]
        )
      ): (state, numbered) =>
        val transaction = numbered.transaction
        config.ratesToReference.get(transaction.currency).filter(_ > 0) match
          case None =>
            val error = PureValidationError.MissingRate(
              transaction.id,
              transaction.currency
            )
            (
              state._1,
              rejection(
                numbered.lineNumber,
                transaction.id,
                error
              ) :: state._2,
              state._3
            )
          case Some(rate) =>
            config.feeRates.get(transaction.sender) match
              case None =>
                val error = PureValidationError.BusinessRules(
                  transaction.id,
                  List(s"TAUX_FRAIS_MANQUANT:${transaction.sender}")
                )
                (
                  state._1,
                  rejection(
                    numbered.lineNumber,
                    transaction.id,
                    error
                  ) :: state._2,
                  state._3
                )
              case Some(feeRate) if feeRate < 0 =>
                val error = PureValidationError.BusinessRules(
                  transaction.id,
                  List(s"TAUX_FRAIS_NEGATIF:${transaction.sender}")
                )
                (
                  state._1,
                  rejection(
                    numbered.lineNumber,
                    transaction.id,
                    error
                  ) :: state._2,
                  state._3
                )
              case Some(feeRate) =>
                val settlementAmount = DataCleaner.formatAmount(
                  transaction.amount * rate
                )
                val amountWithFee = CurriedRules.applyPreciseFee(feeRate)(
                  settlementAmount
                )
                val fee = DataCleaner.formatAmount(
                  amountWithFee - settlementAmount
                )
                val prepared = DataCleaner.anonymize(
                  config.referenceCurrency,
                  settlementAmount,
                  fee
                )(transaction)
                val message = CurriedRules.logWithBank(transaction.sender)(
                  s"transaction ${transaction.id} préparée"
                )
                (prepared :: state._1, state._2, message :: state._3)

      PreparedBatch(
        referenceCurrency = config.referenceCurrency,
        transactions = transactions.reverse,
        rejections = (validated.rejections ++ newRejections.reverse)
          .sortBy(_.lineNumber),
        trace = trace.reverse
      )

  val calculate: PreparedBatch => PureClearingReport = prepared =>
    val feesByBank = prepared.transactions.foldLeft(
      Map.empty[String, BigDecimal]
    ): (fees, transaction) =>
      fees.updatedWith(transaction.sender):
        case Some(total) =>
          Some(DataCleaner.formatAmount(total + transaction.fee))
        case None        => Some(transaction.fee)

    PureClearingReport(
      referenceCurrency = prepared.referenceCurrency,
      transactions = prepared.transactions,
      rejections = prepared.rejections,
      positions = PureNettingCalculator.positions(prepared.transactions),
      feesByBank = feesByBank,
      trace = prepared.trace
    )

  def configure(config: PureEngineConfig): String => PureClearingReport =
    splitLines andThen parse andThen validate(config) andThen
      prepare(config) andThen calculate

  private def validationFailure(
    config: PureEngineConfig,
    transaction: Transaction,
    duplicate: Boolean
  ): Option[PureValidationError] =
    val regularFailure = DataCleaner.validateStatus(transaction) match
      case Left(error) => Some(error)
      case Right(_) =>
        val businessErrors =
          AdvancedTransactionValidator.validate(transaction)
        if businessErrors.nonEmpty then
          Some(
            PureValidationError.BusinessRules(
              transaction.id,
              businessErrors.map(errorCode)
            )
          )
        else
          config.limits.get(transaction.transactionType) match
            case None =>
              Some(
                PureValidationError.BusinessRules(
                  transaction.id,
                  List(
                    s"LIMITE_MANQUANTE:${transaction.transactionType.code}"
                  )
                )
              )
            case Some(limit)
                if !CurriedRules.checkLimit(limit)(transaction) =>
              Some(PureValidationError.LimitExceeded(transaction.id, limit))
            case Some(_) => None

    if duplicate then
      val duplicateCode = "TRANSACTION_DUPLIQUEE"
      regularFailure match
        case Some(PureValidationError.BusinessRules(_, messages)) =>
          Some(
            PureValidationError.BusinessRules(
              transaction.id,
              messages :+ duplicateCode
            )
          )
        case Some(error) =>
          Some(
            PureValidationError.BusinessRules(
              transaction.id,
              List(validationErrorCode(error), duplicateCode)
            )
          )
        case None =>
          Some(
            PureValidationError.BusinessRules(
              transaction.id,
              List(duplicateCode)
            )
          )
    else regularFailure

  private[v20] def errorCode(error: LineError): String = error match
    case InvalidAmount(_) => "MONTANT_INVALIDE"
    case UnknownBank(_)   => "BANQUE_INCONNUE"
    case InvalidIban(_)   => "IBAN_INVALIDE"
    case FieldValidationError(field, _) =>
      s"CHAMP_INVALIDE:${field.toUpperCase(Locale.ROOT)}"
    case MalformedCsv(_)              => "CSV_MALFORME"
    case DuplicateTransaction         => "TRANSACTION_DUPLIQUEE"
    case SuspiciousTransaction(_, _)  => "TRANSACTION_SUSPECTE"
    case Iso20022Rejection(code, _)   => s"ISO20022:${code.toString}"

  private[v20] def validationErrorCode(
    error: PureValidationError
  ): String =
    error match
      case PureValidationError.NonPositiveAmount(_) =>
        "MONTANT_NON_POSITIF"
      case PureValidationError.BusinessRules(_, messages) =>
        messages.mkString("+")
      case PureValidationError.LimitExceeded(_, limit) =>
        s"LIMITE_DEPASSEE:${limit.bigDecimal.toPlainString}"
      case PureValidationError.MissingRate(_, currency) =>
        s"TAUX_MANQUANT:${currency.toString}"

  private def rejection(
    lineNumber: Int,
    transactionId: Int,
    error: PureValidationError
  ): PureRejection =
    val reason = error match
      case PureValidationError.NonPositiveAmount(_) =>
        "MONTANT_NON_POSITIF"
      case PureValidationError.BusinessRules(_, messages) =>
        s"REGLES_METIER:${messages.mkString("+")}"
      case PureValidationError.LimitExceeded(_, limit) =>
        s"LIMITE_DEPASSEE:${limit.bigDecimal.toPlainString}"
      case PureValidationError.MissingRate(_, currency) =>
        s"TAUX_MANQUANT:${currency.toString}"

    PureRejection(lineNumber, Some(transactionId), reason)
