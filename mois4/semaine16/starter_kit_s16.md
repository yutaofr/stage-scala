# Starter Kit Semaine 16 : Cassandra Persistance

L'infra Cassandra, le driver et le DAO sont fournis. Le stagiaire implémente les requêtes métier.

---

## Kit 16.0 — Docker Compose Complet (Kafka + Cassandra)

**Fichier fourni :** `docker/docker-compose-full.yml`

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on: [zookeeper]
    ports: ["9092:9092"]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  cassandra:
    image: cassandra:4.1
    ports: ["9042:9042"]
    environment:
      CASSANDRA_CLUSTER_NAME: clearing-cluster
    volumes:
      - ./init-cassandra.cql:/docker-entrypoint-initdb.d/init.cql
    healthcheck:
      test: ["CMD-SHELL", "cqlsh -e 'describe keyspaces'"]
      interval: 10s
      timeout: 5s
      retries: 10
```

---

## Kit 16.1 — Schéma Cassandra (FOURNI)

**Fichier fourni :** `docker/init-cassandra.cql`

```sql
CREATE KEYSPACE IF NOT EXISTS clearing
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE clearing;

-- Table d'historique de compensation (partitionnée par date)
CREATE TABLE IF NOT EXISTS clearing_history (
  clearing_date  date,
  batch_id       uuid,
  sender         text,
  receiver       text,
  amount         decimal,
  status         text,
  created_at     timestamp,
  PRIMARY KEY ((clearing_date), created_at, batch_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- Table des positions nettes par banque
CREATE TABLE IF NOT EXISTS bank_positions (
  bank_id        text,
  clearing_date  date,
  net_position   decimal,
  tx_count       int,
  updated_at     timestamp,
  PRIMARY KEY ((bank_id), clearing_date)
) WITH CLUSTERING ORDER BY (clearing_date DESC);
```

---

## Kit 16.2 — DAO Cassandra (Wrapper ZIO fourni)

**Fichier fourni :** `src/main/scala/distributed/cassandra/ClearingRepository.scala`

```scala
package distributed.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import zio.*
import java.net.InetSocketAddress
import java.util.UUID
import java.time.{Instant, LocalDate}

/**
 * STARTER KIT : La connexion Cassandra et les requêtes préparées
 * sont fournies. Le stagiaire implémente les méthodes métier.
 */
trait ClearingRepository:
  def saveClearingResult(date: LocalDate, sender: String, receiver: String, amount: BigDecimal, status: String): Task[Unit]
  def saveNetPosition(bankId: String, date: LocalDate, netPosition: BigDecimal, txCount: Int): Task[Unit]
  def getPositionsByBank(bankId: String, limit: Int): Task[List[(LocalDate, BigDecimal, Int)]]
  def getHistoryByDate(date: LocalDate): Task[List[(String, String, BigDecimal, String)]]

object ClearingRepository:
  // Connexion pré-câblée par le tuteur
  val live: ZLayer[Any, Throwable, ClearingRepository] = ZLayer.scoped {
    ZIO.acquireRelease(
      ZIO.attempt {
        CqlSession.builder()
          .addContactPoint(new InetSocketAddress("localhost", 9042))
          .withLocalDatacenter("datacenter1")
          .withKeyspace("clearing")
          .build()
      }
    )(session => ZIO.succeed(session.close())).map { session =>
      new ClearingRepository:
        // === ZONE STAGIAIRE ===

        def saveClearingResult(date: LocalDate, sender: String, receiver: String, amount: BigDecimal, status: String): Task[Unit] =
          ZIO.attempt {
            val stmt = SimpleStatement.newInstance(
              "INSERT INTO clearing_history (clearing_date, batch_id, sender, receiver, amount, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
              date, UUID.randomUUID(), sender, receiver, amount.bigDecimal, status, Instant.now()
            )
            session.execute(stmt)
            ()
          }

        def saveNetPosition(bankId: String, date: LocalDate, netPosition: BigDecimal, txCount: Int): Task[Unit] =
          ZIO.attempt {
            // TODO : Écrire l'INSERT pour bank_positions
            ???
          }

        def getPositionsByBank(bankId: String, limit: Int): Task[List[(LocalDate, BigDecimal, Int)]] =
          ZIO.attempt {
            // TODO : SELECT depuis bank_positions WHERE bank_id = ? LIMIT ?
            ???
          }

        def getHistoryByDate(date: LocalDate): Task[List[(String, String, BigDecimal, String)]] =
          ZIO.attempt {
            // TODO : SELECT depuis clearing_history WHERE clearing_date = ?
            ???
          }
    }
  }
```

**Exercice du stagiaire :** Implémenter les 3 méthodes `???`. Lancer les tests d'intégration avec Cassandra Docker.

---

## Kit 16.3 — Pipeline Complet Kafka → ZIO → Cassandra

```scala
package distributed

import zio.*
import distributed.kafka.*
import distributed.cassandra.*
import distributed.zio.*
import java.time.LocalDate

/**
 * STARTER KIT : Le wiring du pipeline complet est fourni.
 * Le stagiaire a juste rempli les "???" dans les kits précédents.
 */
object FullPipeline extends ZIOAppDefault:
  val pipeline = for
    _     <- Console.printLine("=== Clearing Engine v3.1 ===")
    // Lire depuis Kafka (le consumer remplit une queue)
    // Traiter avec le NettingService
    // Persister dans Cassandra
    repo  <- ZIO.service[ClearingRepository]
    today  = LocalDate.now()
    _     <- Console.printLine(s"Traitement du $today")
    // ... 
    _     <- Console.printLine("Pipeline terminé ✅")
  yield ()

  def run = pipeline.provide(
    ClearingRepository.live,
    NettingService.live,
    ValidationService.live
  )
```

---

## Kit 16.4 — Script de Vérification

```bash
#!/bin/bash
# verify_full_pipeline.sh

echo "1. Démarrage Kafka + Cassandra..."
docker-compose -f docker/docker-compose-full.yml up -d
echo "   Attente de Cassandra..."
sleep 30

echo "2. Injection de transactions..."
docker run --rm --network host -v $(pwd):/app -w /app \
  sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
  sbt "runMain distributed.kafka.TransactionProducer"

echo "3. Lancement du pipeline complet..."
docker run --rm --network host -v $(pwd):/app -w /app \
  sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
  sbt "runMain distributed.FullPipeline"

echo "4. Vérification Cassandra..."
docker exec -it $(docker ps -q -f name=cassandra) cqlsh -e \
  "SELECT * FROM clearing.bank_positions LIMIT 10;"

echo "Pipeline vérifié ✅"
```
