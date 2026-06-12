# Starter Kit Semaine 15 : Kafka Streaming

C'est la semaine la plus critique de la zone rouge. Le stagiaire ne doit absolument PAS configurer Kafka de zéro. Tout l'infra est fourni.

---

## Kit 15.0 — Docker Compose Kafka (FOURNI INTÉGRALEMENT)

**Fichier fourni :** `docker/docker-compose-kafka.yml`

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  kafka-init:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - kafka
    entrypoint: ['/bin/sh', '-c']
    command: |
      "
      echo 'Waiting for Kafka...' && sleep 10 &&
      kafka-topics --create --if-not-exists --topic clearing-input --partitions 3 --replication-factor 1 --bootstrap-server kafka:9092 &&
      kafka-topics --create --if-not-exists --topic clearing-output --partitions 3 --replication-factor 1 --bootstrap-server kafka:9092 &&
      echo 'Topics created ✅'
      "
```

**Usage :** `docker-compose -f docker/docker-compose-kafka.yml up -d`

---

## Kit 15.1 — Kafka Producer Template

**Fichier fourni :** `src/main/scala/distributed/kafka/TransactionProducer.scala`

```scala
package distributed.kafka

import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

/**
 * STARTER KIT : Le producer est entièrement pré-câblé.
 * Le stagiaire modifie uniquement la méthode "serialize".
 */
object TransactionProducer:
  private val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.ACKS_CONFIG, "all")

  private val producer = new KafkaProducer[String, String](props)

  // === ZONE STAGIAIRE ===
  /** Sérialiser une Transaction en JSON String */
  def serialize(tx: clearing.Transaction): String =
    ???  // TODO : Convertir la Transaction en JSON (utiliser le Serializer du Mois 3)

  // === ZONE TUTEUR : Infrastructure ===
  def send(tx: clearing.Transaction): Unit =
    val record = new ProducerRecord[String, String](
      "clearing-input",
      tx.sender,  // Clé = banque émettrice (pour le partitionnement)
      serialize(tx)
    )
    producer.send(record).get()

  def sendBatch(txs: List[clearing.Transaction]): Unit =
    txs.foreach(send)
    producer.flush()

  def close(): Unit = producer.close()

  @main def injectTransactions(): Unit =
    println("Injection de 100 transactions dans Kafka...")
    val txs = ??? // TODO : Générer 100 transactions
    sendBatch(txs)
    println("Done ✅")
    close()
```

---

## Kit 15.2 — Kafka Consumer Template

**Fichier fourni :** `src/main/scala/distributed/kafka/ClearingConsumer.scala`

```scala
package distributed.kafka

import org.apache.kafka.clients.consumer.*
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.Properties
import java.time.Duration
import scala.jdk.CollectionConverters.*

/**
 * STARTER KIT : Le consumer loop est pré-câblé.
 * Le stagiaire implémente "deserialize" et "processTransaction".
 */
object ClearingConsumer:
  private val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "clearing-engine")
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

  private val consumer = new KafkaConsumer[String, String](props)

  // === ZONE STAGIAIRE ===
  /** Désérialiser un JSON String en Transaction */
  def deserialize(json: String): Either[String, clearing.Transaction] =
    ???  // TODO : Parser le JSON

  /** Traiter une transaction validée (logique métier) */
  def processTransaction(tx: clearing.Transaction): Unit =
    ???  // TODO : Appliquer la validation + netting du Mois 3

  // === ZONE TUTEUR : Consumer Loop ===
  @main def startConsumer(): Unit =
    consumer.subscribe(java.util.List.of("clearing-input"))
    println("Consumer démarré. En attente de transactions...")

    var running = true
    while running do
      val records = consumer.poll(Duration.ofMillis(500))
      if !records.isEmpty then
        val batch = records.asScala.toList
        println(s"Batch reçu : ${batch.size} messages")

        batch.foreach { record =>
          deserialize(record.value()) match
            case Right(tx) =>
              processTransaction(tx)
              println(s"  ✅ Transaction #${tx.id} traitée")
            case Left(error) =>
              println(s"  ❌ Erreur de parsing : $error")
        }

        // Commit manuel des offsets (at-least-once)
        consumer.commitSync()
```

---

## Kit 15.3 — Deduplication Cache

**Fichier fourni :** `src/main/scala/distributed/kafka/DeduplicationCache.scala`

```scala
package distributed.kafka

import java.util.concurrent.ConcurrentHashMap

/**
 * STARTER KIT : Le cache de déduplication est fourni.
 * Le stagiaire l'intègre dans le consumer.
 */
object DeduplicationCache:
  private val seen = new ConcurrentHashMap[Int, Boolean]()

  /** Retourne true si la transaction n'a JAMAIS été vue */
  def isNew(transactionId: Int): Boolean =
    seen.putIfAbsent(transactionId, true) == null

  def size: Int = seen.size()
  def clear(): Unit = seen.clear()
```

**Exercice du stagiaire :** Intégrer `DeduplicationCache.isNew` dans le consumer AVANT `processTransaction`. Simuler un double-envoi et vérifier qu'il est ignoré.

---

## Kit 15.4 — Script de Test End-to-End

```bash
#!/bin/bash
# test_kafka_pipeline.sh — Fourni par le tuteur

echo "1. Démarrage de Kafka..."
docker-compose -f docker/docker-compose-kafka.yml up -d
sleep 15

echo "2. Lancement du Consumer en arrière-plan..."
docker run --rm --network host -v $(pwd):/app -w /app \
  sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
  sbt "runMain distributed.kafka.ClearingConsumer" &
CONSUMER_PID=$!
sleep 5

echo "3. Injection de 50 transactions..."
docker run --rm --network host -v $(pwd):/app -w /app \
  sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
  sbt "runMain distributed.kafka.TransactionProducer"

sleep 5
echo "4. Arrêt..."
kill $CONSUMER_PID 2>/dev/null
echo "Pipeline Kafka terminé ✅"
```
