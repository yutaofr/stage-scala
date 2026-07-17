package clearing.v31

import clearing.v30.{BatchReport, InputPartition, RecordEnvelope}

final class DurableBatchCoordinator(
  processor: DurableProcessing
):
  def process(records: List[RecordEnvelope]): BatchReport =
    records
      .groupBy(record => InputPartition(record.partition))
      .toList
      .sortBy((partition, _) => partition.value)
      .foldLeft(BatchReport(Map.empty, 0, 0, Set.empty)):
        case (report, (partition, unordered)) =>
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
    records
      .foldLeft((initial, false)):
        case ((report, true), _) => (report, true)
        case ((report, false), envelope) =>
          processor.process(envelope) match
            case DurableRecordOutcome.Published =>
              (
                advance(report, partition, envelope).copy(
                  published = report.published + 1
                ),
                false
              )
            case DurableRecordOutcome.Duplicate =>
              (
                advance(report, partition, envelope).copy(
                  duplicates = report.duplicates + 1
                ),
                false
              )
            case DurableRecordOutcome.Failed(_) =>
              (
                report.copy(
                  failedPartitions = report.failedPartitions + partition,
                  retryOffsets = report.retryOffsets.updated(
                    partition,
                    envelope.offset
                  )
                ),
                true
              )
      ._1

  private def advance(
    report: BatchReport,
    partition: InputPartition,
    envelope: RecordEnvelope
  ): BatchReport =
    report.copy(
      committableOffsets = report.committableOffsets.updated(
        partition,
        envelope.offset + 1L
      )
    )
