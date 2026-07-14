package clearing.v30

import java.util.concurrent.ConcurrentHashMap

final class InMemoryDeduplicationRegistry(
  onMark: Int => Unit = _ => ()
) extends DeduplicationRegistry:
  private val processed = new ConcurrentHashMap[Int, java.lang.Boolean]()

  def contains(transactionId: Int): Boolean =
    processed.containsKey(transactionId)

  def markProcessed(transactionId: Int): Unit =
    processed.put(transactionId, java.lang.Boolean.TRUE)
    onMark(transactionId)

