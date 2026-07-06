package distributed.kafka

import clearing.contract.*
import clearing.model.*
import io.circe.syntax.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.header.internals.RecordHeader
import java.nio.charset.StandardCharsets
import java.util.Properties

object TransactionProducer:
  
  /**
   * Crée un KafkaProducer avec les configurations de durabilité et d'idempotence requises.
   * Ces paramètres sont fournis pour montrer comment configurer correctement un producteur fiable.
   */
  def createProducer(settings: KafkaSettings): KafkaProducer[String, String] =
    val properties = Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.bootstrapServers)
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    
    // Configurations de durabilité du Jour 2
    properties.put(ProducerConfig.ACKS_CONFIG, "all")
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
    properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "5000")
    properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000")
    properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "5000")
    
    new KafkaProducer[String, String](properties)

  /**
   * Envoie une transaction sous forme d'événement JSON vers Kafka.
   * 
   * TODO (TP Jour 2 - Exercice 1) :
   * 1. Convertir l'objet Transaction métier en TransactionSubmittedV1 (contrat d'événement).
   *    Astuce : Utiliser TransactionSubmittedV1.fromDomain(tx)
   * 2. Sérialiser cet événement en chaîne JSON.
   *    Astuce : Utiliser le format JSON avec event.asJson.noSpaces
   * 3. Créer un ProducerRecord avec :
   *    - Le nom du topic d'entrée (settings.inputTopic)
   *    - La clé de routage : le code de la banque émettrice (tx.sender.value)
   *    - La valeur : le JSON obtenu à l'étape 2
   * 4. Ajouter un en-tête (Header) "transaction-id" contenant l'ID de la transaction.
   *    Astuce : record.headers().add(new RecordHeader("transaction-id", tx.id.value.getBytes(StandardCharsets.UTF_8)))
   * 5. Envoyer le message via le producer et retourner le Future de RecordMetadata retourné par producer.send.
   */
  def send(
    producer: KafkaProducer[String, String],
    settings: KafkaSettings,
    tx: Transaction
  ): java.util.concurrent.Future[RecordMetadata] =
    ???
