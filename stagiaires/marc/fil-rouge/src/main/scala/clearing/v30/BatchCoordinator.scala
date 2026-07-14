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
        val fingerprint = PayloadFingerprint.sha256(envelope.value)
        decision.transactionId match
          case Some(id) =>
            registry.status(id, fingerprint) match
              case DeduplicationStatus.Duplicate =>
                completeDuplicate(report, partition, envelope)
              case DeduplicationStatus.New =>
                publish(
                  report,
                  partition,
                  envelope,
                  decision,
                  Some(id -> fingerprint)
                )
              case DeduplicationStatus.Conflict =>
                val conflict = ProcessingDecision.Rejected(
                  RejectedEvent(
                    transactionId = Some(id),
                    code = "EVENT_ID_CONFLICT",
                    message =
                      "identifiant réutilisé avec un payload différent",
                    payloadFingerprint = fingerprint,
                    occurredAt = envelope.occurredAt
                  )
                )
                publish(
                  report,
                  partition,
                  envelope,
                  conflict,
                  Some(id -> fingerprint)
                )
          case None =>
            publish(report, partition, envelope, decision, None)
    ._1

  private def completeDuplicate(
    report: BatchReport,
    partition: InputPartition,
    envelope: RecordEnvelope
  ): (BatchReport, Boolean) =
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

  private def publish(
    report: BatchReport,
    partition: InputPartition,
    envelope: RecordEnvelope,
    decision: ProcessingDecision,
    identity: Option[(Int, String)]
  ): (BatchReport, Boolean) =
    publisher.publish(envelope.key, decision) match
      case Right(_) =>
        identity.foreach(registry.markProcessed)
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
            failedPartitions = report.failedPartitions + partition,
            retryOffsets = report.retryOffsets.updated(
              partition,
              envelope.offset
            )
          ),
          true
        )
