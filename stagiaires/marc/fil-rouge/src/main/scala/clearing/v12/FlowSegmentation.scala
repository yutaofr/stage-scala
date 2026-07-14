package clearing.v12

import clearing.model.*

object FlowSegmentation:
  def partitionByStatus(
    transactions: List[Transaction]
  ): (List[Transaction], List[Transaction]) =
    transactions.partition(_.status == TransactionStatus.Validated)

  def partitionSummary(
    transactions: List[Transaction]
  ): PartitionSummary =
    val (validated, others) = partitionByStatus(transactions)
    PartitionSummary(
      validatedCount = validated.size,
      otherCount = others.size,
      validatedAmount = validated.map(_.amount).sum,
      otherAmount = others.map(_.amount).sum
    )

  def processBatches(
    transactions: List[Transaction],
    size: Int = 1000
  ): List[BatchNetting] =
    require(size > 0, "La taille du batch doit être strictement positive")
    val (validated, _) = partitionByStatus(transactions)

    validated.sortBy(_.id).grouped(size).zipWithIndex.map:
      case (batch, index) =>
        BatchNetting(
          number = index + 1,
          transactionCount = batch.size,
          positions = MultilateralNetting.computePositions(batch)
        )
    .toList

  def mergePositions(
    batches: List[BatchNetting]
  ): Map[String, BigDecimal] =
    batches.iterator
      .flatMap(_.positions.iterator)
      .foldLeft(Map.empty[String, BigDecimal]):
        case (merged, (bank, balance)) =>
          merged.updatedWith(bank):
            case Some(current) => Some(current + balance)
            case None          => Some(balance)

  def fraudWindows(
    transactions: List[Transaction],
    size: Int = 5,
    threshold: BigDecimal = BigDecimal("500000")
  ): List[FraudWindowAlert] =
    require(size > 0, "La taille de fenêtre doit être strictement positive")
    val (validated, _) = partitionByStatus(transactions)

    validated.sortBy(_.id).sliding(size).collect:
      case window if window.size == size =>
        FraudWindowAlert(
          ids = window.map(_.id),
          totalAmount = window.map(_.amount).sum
        )
    .filter(_.totalAmount > threshold)
    .toList
