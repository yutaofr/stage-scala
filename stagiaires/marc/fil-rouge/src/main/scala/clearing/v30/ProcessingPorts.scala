package clearing.v30

final case class PublishingFailure(reason: String)

trait DecisionPublisher:
  def publish(
    key: Option[String],
    decision: ProcessingDecision
  ): Either[PublishingFailure, Unit]

object DecisionPublisher:
  def apply(
    publishDecision: (
      Option[String],
      ProcessingDecision
    ) => Either[PublishingFailure, Unit]
  ): DecisionPublisher = new DecisionPublisher:
    def publish(
      key: Option[String],
      decision: ProcessingDecision
    ): Either[PublishingFailure, Unit] =
      publishDecision(key, decision)

trait DeduplicationRegistry:
  def status(
    transactionId: Int,
    payloadFingerprint: String
  ): DeduplicationStatus
  def markProcessed(
    transactionId: Int,
    payloadFingerprint: String
  ): Unit

enum DeduplicationStatus:
  case New, Duplicate, Conflict

trait OffsetCommitter:
  def commit(offsets: Map[InputPartition, Long]): Unit

object OffsetCommitter:
  def apply(
    commitOffsets: Map[InputPartition, Long] => Unit
  ): OffsetCommitter = new OffsetCommitter:
    def commit(offsets: Map[InputPartition, Long]): Unit =
      commitOffsets(offsets)

trait PartitionRewinder:
  def rewind(offsets: Map[InputPartition, Long]): Unit

object PartitionRewinder:
  def apply(
    rewindOffsets: Map[InputPartition, Long] => Unit
  ): PartitionRewinder = new PartitionRewinder:
    def rewind(offsets: Map[InputPartition, Long]): Unit =
      rewindOffsets(offsets)

final case class InputPartition(value: Int)

final case class BatchReport(
  committableOffsets: Map[InputPartition, Long],
  published: Int,
  duplicates: Int,
  failedPartitions: Set[InputPartition],
  retryOffsets: Map[InputPartition, Long] = Map.empty
)
