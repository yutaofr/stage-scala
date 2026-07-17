package clearing.v10

import clearing.model.*

object BatchProcessor:
  def processBatch(batch: ClearingBatch): ClearingResult =
    val validation = TransactionValidator.partition(batch.transactions)
    processValidated(batch, validation)

  def processValidated(
    batch: ClearingBatch,
    validation: ValidationSummary
  ): ClearingResult =
    val positions = NettingCalculatorV10.calculate(validation.valid)
    val errors = validation.invalid.flatMap(_.errors)
    ClearingResult(batch, positions, errors)

  def describeResult(result: ClearingResult): String =
    result.batch.status match
      case TransactionStatus.Rejected => "Batch Invalide"
      case _ if result.errors.isEmpty  => "Succès total"
      case _ => s"Échec partiel (${result.errors.size} erreurs)"
