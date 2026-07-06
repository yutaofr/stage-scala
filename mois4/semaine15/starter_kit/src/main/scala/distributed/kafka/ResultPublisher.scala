package distributed.kafka

import io.circe.syntax.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.header.internals.RecordHeader
import java.nio.charset.StandardCharsets

final class ResultPublisher(
  producer: KafkaProducer[String, String],
  settings: KafkaSettings
):
  /**
   * Publie la décision de traitement sur le topic approprié (output ou DLQ).
   * 
   * TODO (TP Jour 3 - Exercice 2) :
   * 1. Matcher sur la décision (Validated ou Rejected) :
   *    - Si ProcessingDecision.Validated(key, event) :
   *      - Topic de destination : settings.outputTopic
   *      - Clé : key
   *      - Valeur : JSON de l'événement (event.asJson.noSpaces)
   *      - Header "transaction-id" : event.id
   *    - Si ProcessingDecision.Rejected(keyOpt, event) :
   *      - Topic de destination : settings.dlqTopic
   *      - Clé : keyOpt.orNull
   *      - Valeur : JSON de l'événement (event.asJson.noSpaces)
   *      - Header "transaction-id" : event.id.getOrElse(null) (si disponible)
   * 2. Instancier le ProducerRecord[String, String] correspondant.
   * 3. Ajouter l'en-tête de transaction dans le ProducerRecord :
   *    Astuce : record.headers().add(new RecordHeader("transaction-id", transactionId.getBytes(StandardCharsets.UTF_8)))
   * 4. Appeler producer.send(record) et retourner le Future[RecordMetadata] obtenu.
   */
  def publish(decision: ProcessingDecision): java.util.concurrent.Future[RecordMetadata] =
    ???
