package clearing.v30

final class BatchCoordinator(
  processor: RecordEnvelope => ProcessingDecision,
  publisher: DecisionPublisher,
  registry: DeduplicationRegistry
):
  def process(records: List[RecordEnvelope]): BatchReport =
    val partitions = records
      .groupBy(record => InputPartition(record.partition))
      .toList
      .sortBy((partition, _) => partition.value)

    partitions.foldLeft(
      BatchReport(Map.empty, 0, 0, Set.empty)
    ): (report, partitionRecords) =>
      val (partition, unordered) = partitionRecords
      processPartition(
        partition,
        unordered.sortBy(_.offset),
        report
      )

  private def processPartition(
    partition: InputPartition,
    records: List[RecordEnvelope],
    initial: BatchReport
  ): BatchReport =
    records.foldLeft((initial, false)):
      case ((report, true), _) => (report, true)
      case ((report, false), envelope) =>
        val decision = processor(envelope)
        decision.transactionId match
          case Some(id) if registry.contains(id) =>
            (
              report.copy(
                committableOffsets = report.committableOffsets.updated(
                  partition,
                  envelope.offset + 1L
                ),
                duplicates = report.duplicates + 1
              ),
              false
            )
          case transactionId =>
            publisher.publish(envelope.key, decision) match
              case Right(_) =>
                transactionId.foreach(registry.markProcessed)
                (
                  report.copy(
                    committableOffsets = report.committableOffsets.updated(
                      partition,
                      envelope.offset + 1L
                    ),
                    published = report.published + 1
                  ),
                  false
                )
              case Left(_) =>
                (
                  report.copy(
                    failedPartitions = report.failedPartitions + partition
                  ),
                  true
                )
    ._1
