package clearing.v30

import java.util.concurrent.ConcurrentHashMap

final class InMemoryDeduplicationRegistry(
  onMark: Int => Unit = _ => ()
) extends DeduplicationRegistry:
  private val processed = new ConcurrentHashMap[Int, java.util.Set[String]]()

  def status(
    transactionId: Int,
    payloadFingerprint: String
  ): DeduplicationStatus =
    Option(processed.get(transactionId)) match
      case None => DeduplicationStatus.New
      case Some(fingerprints) if fingerprints.contains(payloadFingerprint) =>
        DeduplicationStatus.Duplicate
      case Some(_) => DeduplicationStatus.Conflict

  def markProcessed(
    transactionId: Int,
    payloadFingerprint: String
  ): Unit =
    processed
      .computeIfAbsent(
        transactionId,
        _ => ConcurrentHashMap.newKeySet[String]()
      )
      .add(payloadFingerprint)
    onMark(transactionId)

  def contains(transactionId: Int): Boolean =
    processed.containsKey(transactionId)

  def markProcessed(transactionId: Int): Unit =
    markProcessed(transactionId, s"manual-$transactionId")
