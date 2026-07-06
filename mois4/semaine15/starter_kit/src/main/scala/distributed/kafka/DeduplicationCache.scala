package distributed.kafka

import java.util.concurrent.ConcurrentHashMap

/**
 * Cache mémoire pour éviter les double-traitements de transactions en cas de rejeu (At-Least-Once).
 * Utile pour le TP Jour 4.
 * Ce cache est entièrement fourni pour simplifier l'utilisation d'une structure concurrente thread-safe.
 */
object DeduplicationCache:
  private val processed = new ConcurrentHashMap[String, java.lang.Boolean]()

  /**
   * Vérifie si l'ID de la transaction a déjà été marqué comme traité.
   */
  def contains(transactionId: String): Boolean =
    processed.containsKey(transactionId)

  /**
   * Enregistre l'ID de la transaction dans le cache.
   */
  def markProcessed(transactionId: String): Unit =
    processed.put(transactionId, true)

  /**
   * Efface le cache (principalement pour les tests unitaires).
   */
  def clear(): Unit = 
    processed.clear()
