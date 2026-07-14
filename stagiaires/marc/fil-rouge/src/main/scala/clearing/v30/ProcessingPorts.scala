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
  def contains(transactionId: Int): Boolean
  def markProcessed(transactionId: Int): Unit

trait OffsetCommitter:
  def commit(offsets: Map[InputPartition, Long]): Unit

object OffsetCommitter:
  def apply(
    commitOffsets: Map[InputPartition, Long] => Unit
  ): OffsetCommitter = new OffsetCommitter:
    def commit(offsets: Map[InputPartition, Long]): Unit =
      commitOffsets(offsets)

final case class InputPartition(value: Int)

final case class BatchReport(
  committableOffsets: Map[InputPartition, Long],
  published: Int,
  duplicates: Int,
  failedPartitions: Set[InputPartition]
)
