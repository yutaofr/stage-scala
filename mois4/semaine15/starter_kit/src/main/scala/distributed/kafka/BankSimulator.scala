package distributed.kafka

import clearing.model.*
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.atomic.AtomicInteger

/**
 * Simulateur de banques qui génère et injecte des transactions dans Kafka.
 * Concerne le TP Jour 2 - Exercices 2 & 3.
 */
@main def runBankSimulator(): Unit =
  val settings = KafkaSettings.fromEnvironment()
  val producer = TransactionProducer.createProducer(settings)
  
  val successCount = AtomicInteger(0)
  val failureCount = AtomicInteger(0)
  
  println("Démarrage du simulateur de banques...")
  
  // Génère un ensemble de transactions déterministes à injecter
  val transactions = generateDeterministicTransactions()
  
  try
    // TODO (TP Jour 2) :
    // 1. Boucler sur la liste de transactions 'transactions'.
    // 2. Envoyer chaque transaction vers Kafka en appelant TransactionProducer.send.
    // 3. Fournir un Callback pour recevoir la notification asynchrone d'écriture de Kafka :
    //    - En cas de succès : afficher la clé, la partition et l'offset, et incrémenter successCount.
    //    - En cas d'échec : afficher l'exception et incrémenter failureCount.
    // 4. Introduire un délai de 100ms entre chaque envoi pour respecter la limite de débit de 10 tx/s.
    //    Astuce : Thread.sleep(100)
    // 5. Veiller à ce que l'application ne se termine pas avant d'avoir reçu les accusés de tous les messages envoyés.
    //    (Le producer.close() bloquera jusqu'à ce que tous les messages en attente soient envoyés).
    ???
  finally
    producer.close()
    println(s"Simulateur terminé. Succès : ${successCount.get()}, Échecs : ${failureCount.get()}")

/**
 * Génère 50 transactions déterministes pour peupler le topic d'entrée.
 */
def generateDeterministicTransactions(): List[Transaction] =
  val banks = List("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe)
  (1 to 50).map { i =>
    val sender = banks(i % banks.size)
    val receiver = banks((i + 1) % banks.size)
    Transaction(
      id = TransactionId.unsafe(s"tx-$i"),
      sender = sender,
      receiver = receiver,
      amount = Money(BigDecimal(100 * i)),
      status = TransactionStatus.Pending
    )
  }.toList
