package distributed.kafka

import clearing.model.*
import org.apache.kafka.clients.consumer.*
import org.apache.kafka.common.TopicPartition
import java.time.{Duration, Instant}
import java.util.Properties
import scala.jdk.CollectionConverters.*

object KafkaConsumerLoop:
  /**
   * Crée un KafkaConsumer configuré pour un commit manuel précis.
   */
  def createConsumer(settings: KafkaSettings): KafkaConsumer[String, String] =
    val properties = Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.bootstrapServers)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, settings.groupId)
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    
    // Désactiver l'auto-commit pour gérer précisément les offsets
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    
    new KafkaConsumer[String, String](properties)

final class KafkaConsumerLoop(
  consumer: KafkaConsumer[String, String],
  publisher: ResultPublisher,
  processor: RecordProcessor,
  settings: KafkaSettings
):
  
  /**
   * Effectue un poll de records, les regroupe par TopicPartition pour un traitement séquentiel,
   * puis valide manuellement les offsets par partition après publication réussie.
   */
  def pollOnce(timeout: Duration = Duration.ofMillis(500)): Unit =
    val records = consumer.poll(timeout)
    if (!records.isEmpty) then
      // 1. Boucler sur les partitions ayant reçu des messages
      for partition <- records.partitions().asScala do
        val partitionRecords = records.records(partition).asScala
        
        // TODO (TP Jour 3 - Exercice 1 & 2 / TP Jour 4 - Exercice 1 & 2) :
        // Pour chaque record de la partition :
        //   a. Mapper vers un RecordEnvelope :
        //      val envelope = RecordEnvelope(
        //        topic = record.topic,
        //        partition = record.partition,
        //        offset = record.offset,
        //        occurredAt = Instant.ofEpochMilli(record.timestamp),
        //        key = Option(record.key),
        //        payload = record.value
        //      )
        //   b. (TP Jour 4 - Exercice 2) : Si l'ID de transaction (s'il est extractible ou après décodage)
        //      existe déjà dans DeduplicationCache.contains(id), ignorer le traitement métier et passer au message suivant.
        //   c. Appeler processor.process(envelope) pour obtenir la ProcessingDecision.
        //   d. Publier le résultat via publisher.publish(decision) et récupérer le Future.
        //   e. Gérer l'at-least-once : bloquer sur future.get() pour s'assurer que Kafka a bien acquitté la sortie ou la DLQ.
        //   f. (TP Jour 4 - Exercice 2) : Enregistrer l'ID de transaction traité avec DeduplicationCache.markProcessed(id).
        //
        // Après avoir traité TOUS les records de cette partition dans le batch actuel :
        //   g. Calculer le prochain offset à commiter : le dernier offset traité de la partition + 1.
        //      val nextOffset = partitionRecords.last.offset() + 1
        //   h. Effectuer un commit manuel précis pour cette partition :
        //      consumer.commitSync(Map(partition -> new OffsetAndMetadata(nextOffset)).asJava)
        ???

  def run(): Unit =
    consumer.subscribe(java.util.List.of(settings.inputTopic))
    try 
      while !Thread.currentThread().isInterrupted do 
        pollOnce()
    catch
      case _: InterruptedException => println("Boucle de consommation interrompue par shutdown.")
    finally 
      consumer.close()
      println("Consumer Kafka arrêté proprement.")

@main def runKafkaConsumer(): Unit =
  val settings = KafkaSettings.fromEnvironment()
  val knownBanks = Set("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe)
  val consumer = KafkaConsumerLoop.createConsumer(settings)
  val producer = TransactionProducer.createProducer(settings)
  
  val publisher = new ResultPublisher(producer, settings)
  val processor = new RecordProcessor(knownBanks)
  val loop = new KafkaConsumerLoop(consumer, publisher, processor, settings)
  
  // Hook d'arrêt propre (TP Jour 3 - Exercice 1)
  val mainThread = Thread.currentThread()
  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    println("Shutdown intercepté. Arrêt propre du consumer en cours...")
    mainThread.interrupt()
  }))

  try
    loop.run()
  finally
    producer.close()
    println("Ressources fermées.")
