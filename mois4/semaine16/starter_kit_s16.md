# Starter Kit Semaine 16 : Cassandra et reprise durable

Le kit rend le pipeline S15 réparable. Un record n’est plus considéré comme « traité » dès que son ID existe : son état progresse de `Received` à `Projected`, puis à `Completed`.

## Kit 16.0 — Compose Kafka + Cassandra

**Fichier fourni :** `docker/docker-compose-full.yml`

Kafka expose deux listeners :

```yaml
services:
  kafka:
    image: apache/kafka:4.3.0
    ports: ["9092:9092"]
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: EXTERNAL://:9092,INTERNAL://:29092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: EXTERNAL://localhost:9092,INTERNAL://kafka:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: EXTERNAL:PLAINTEXT,INTERNAL:PLAINTEXT,CONTROLLER:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      CLUSTER_ID: 4L6g3nShT-eMCtK--X86sw
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
    healthcheck:
      test: [CMD-SHELL, "/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list >/dev/null 2>&1"]
      interval: 5s
      timeout: 5s
      retries: 20

  kafka-init:
    image: apache/kafka:4.3.0
    depends_on:
      kafka:
        condition: service_healthy
    entrypoint:
      - /bin/sh
      - -ec
      - |
        for topic in clearing-input clearing-output clearing-dlq; do
          /opt/kafka/bin/kafka-topics.sh --create --if-not-exists \
            --topic "$$topic" --partitions 3 --replication-factor 1 \
            --bootstrap-server kafka:29092
        done

  cassandra:
    image: cassandra:4.1
    ports: ["9042:9042"]
    environment:
      CASSANDRA_CLUSTER_NAME: clearing-cluster
    healthcheck:
      test: ["CMD-SHELL", "cqlsh -e 'DESCRIBE CLUSTER'"]
      interval: 10s
      timeout: 5s
      retries: 20

  cassandra-init:
    image: cassandra:4.1
    depends_on:
      cassandra:
        condition: service_healthy
    volumes:
      - ../../fil-rouge/docker/init-cassandra.cql:/init-cassandra.cql:ro
    entrypoint:
      - /bin/sh
      - -ec
      - cqlsh cassandra 9042 -f /init-cassandra.cql
```

Valeurs selon l’exécution :

| Exécution | Kafka | Cassandra |
|---|---|---|
| application locale | `localhost:9092` | `localhost:9042` |
| application dans Compose | `kafka:29092` | `cassandra:9042` |

L’application lit `KAFKA_BOOTSTRAP_SERVERS`, `CASSANDRA_HOST` et `CASSANDRA_PORT`. Aucun hostname n’est codé en dur dans le domaine.

---

## Kit 16.1 — Schéma orienté reprise et requêtes

**Fichier fourni :** `fil-rouge/docker/init-cassandra.cql`

Tables :

- `processing_state` : progression durable d’un ID ;
- `clearing_history_by_day` : historique par date et bucket ;
- `transactions_by_bank_day` : historique d’une banque ;
- `bank_positions` : projection nette idempotente ;
- `pair_activity_by_day` : activité d’une paire de banques.

Stratégie de bucket fournie :

```scala
def historyBucket(transactionId: String): Short =
  Math.floorMod(transactionId.hashCode, 16).toShort
```

Le nombre `16` reste fixe pour cette version du schéma. Le modifier exige une migration.

---

## Kit 16.2 — Repository et requêtes préparées

**Interface fournie :**

```scala
trait ClearingRepository:
  def stage(txId: TransactionId): Task[Option[ProcessingStage]]
  def markStage(txId: TransactionId, stage: ProcessingStage, source: RecordEnvelope): Task[Unit]
  def saveHistory(tx: Transaction, date: LocalDate, bucket: Short, occurredAt: Instant): Task[Unit]
  def saveTransactionByBank(tx: Transaction, date: LocalDate, occurredAt: Instant): Task[Unit]
  def saveNetPositions(txId: TransactionId, date: LocalDate, occurredAt: Instant, values: List[NetPositionProjection]): Task[Unit]
  def savePairActivity(tx: Transaction, date: LocalDate): Task[Unit]
  def getPositionsByBank(bank: BankCode, date: LocalDate, limit: Int): Task[List[Position]]
  def getHistoryByDate(date: LocalDate, buckets: Range = 0 until 16): Task[List[HistoryRow]]
```

La couche `live` prépare les statements une fois à l’acquisition de la session :

```scala
for
  session <- acquireSession
  selectStage <- ZIO.attempt(session.prepare(
    "SELECT stage FROM processing_state WHERE tx_id = ?"
  ))
  markStage <- ZIO.attempt(session.prepare(
    """INSERT INTO processing_state
      |(tx_id, stage, source_topic, source_partition, source_offset, updated_at)
      |VALUES (?, ?, ?, ?, ?, ?)""".stripMargin
  ))
  // Préparer aussi les quatre projections et les deux lectures.
yield new LiveClearingRepository(session, selectStage, markStage, ...)
```

**Zone stagiaire :**

1. Lier chaque type opaque avec `.value`.
2. Utiliser `executeAsync` via `ZIO.fromCompletionStage`.
3. Alimenter toutes les tables exposées par l’interface.
4. Tester la lecture des 16 buckets.
5. Vérifier les requêtes avec `TRACING ON` dans `cqlsh`.

---

## Kit 16.3 — DurableProcessor

**Fichier fourni :** `fil-rouge/src/main/scala/distributed/persistence/DurableProcessing.scala`

**Worker fourni :** `fil-rouge/src/main/scala/distributed/pipeline/ClearingPipelineApp.scala`

Algorithme :

```text
charger l’état
├── Completed  → ne pas rejouer les projections
└── absent, Received ou Projected
    ├── marquer Received si nécessaire
    ├── valider le contrat et calculer les projections
    ├── écrire historique, vues par banque, positions et activité par paire
    ├── marquer Projected
    ├── marquer Completed
    └── autoriser le commit Kafka
```

Les projections utilisent `(date, txId, bankId)` ou une clé équivalente. Une reprise peut donc les réécrire sans créer une seconde opération métier.

**Cas de panne obligatoire :**

1. arrêter après `Received` ;
2. redémarrer et compléter les projections ;
3. arrêter après les projections mais avant `Completed` ;
4. redémarrer et vérifier que les upserts réparent l’état ;
5. valider l’offset seulement après `Completed`.

Le worker suit l’ordre `poll → durable processing → output/DLQ → accusé Kafka → commit par partition`.

---

## Kit 16.4 — Script de vérification

```bash
#!/usr/bin/env bash
set -euo pipefail

docker compose -f docker/docker-compose-full.yml down -v --remove-orphans
docker compose -f docker/docker-compose-full.yml up -d --wait kafka cassandra
docker compose -f docker/docker-compose-full.yml run --rm kafka-init
docker compose -f docker/docker-compose-full.yml run --rm cassandra-init

export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export CASSANDRA_HOST=localhost
export CASSANDRA_PORT=9042

sbt test
# TODO stagiaire : injecter le batch déterministe.
# TODO stagiaire : interrompre le processeur aux deux points de panne.

docker compose -f docker/docker-compose-full.yml exec -T cassandra cqlsh -e \
  "SELECT tx_id, stage FROM clearing.processing_state;"
```

**Invariants à prouver :**

- aucun ID `Completed` sans ses projections ;
- aucune projection métier dupliquée après reprise ;
- aucun offset validé avant `Completed` ;
- les lectures par banque et par date utilisent les tables prévues.
