package clearing.v31

import java.time.{Instant, LocalDate}

enum ProcessingStage:
  case Received, Projected, Completed

final case class DurableIdentity(
  eventKey: String,
  payloadFingerprint: String
)

final case class ProcessingState(
  eventKey: String,
  payloadFingerprint: String,
  stage: ProcessingStage,
  decisionKind: String,
  topic: String,
  partition: Int,
  offset: Long,
  updatedAt: Instant
)

final case class HistoryRow(
  eventKey: String,
  transactionId: Int,
  sender: String,
  receiver: String,
  amount: BigDecimal,
  currency: String,
  status: String,
  occurredAt: Instant
)

final case class BankMovement(
  bank: String,
  date: LocalDate,
  eventKey: String,
  direction: String,
  counterparty: String,
  amount: BigDecimal,
  currency: String,
  occurredAt: Instant
)

final case class PositionContribution(
  bank: String,
  date: LocalDate,
  eventKey: String,
  amount: BigDecimal,
  transactionCount: Int,
  occurredAt: Instant
)

final case class PairActivity(
  date: LocalDate,
  pair: String,
  eventKey: String,
  amount: BigDecimal,
  transactionCount: Int
)

final case class BankPosition(
  bank: String,
  date: LocalDate,
  amount: BigDecimal,
  transactionCount: Int
)

final case class PairSummary(
  pair: String,
  amount: BigDecimal,
  transactionCount: Int
)

final case class DurableSnapshot(
  historyRows: Int,
  bankMovements: Int,
  positionContributions: Int,
  pairActivities: Int
)

object HistoryBucket:
  val Count = 16

  def forEvent(eventKey: String): Int =
    Math.floorMod(eventKey.hashCode, Count)
