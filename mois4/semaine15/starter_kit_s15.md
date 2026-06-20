# Starter Kit Semaine 15 : Kafka Streaming

Le kit prolonge le projet `fil-rouge/`. Il fournit le contrat Kafka v1, les trois topics, le producer, la sortie, la DLQ et la validation manuelle des offsets.

## Préflight

```bash
cd fil-rouge
sbt test
```

Le stagiaire ne sérialise jamais directement les types opaques. Il convertit `Transaction` vers `TransactionSubmittedV1`, puis utilise le codec de `clearing.contract`.

---

## Kit 15.0 — Docker Compose Kafka

**Fichier fourni :** `docker/docker-compose-kafka.yml`

```yaml
services:
  kafka:
    image: apache/kafka:4.3.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      CLUSTER_ID: 4L6g3nShT-eMCtK--X86sw
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
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
            --bootstrap-server kafka:9092
        done
```

**Usage :**

```bash
docker compose -f docker/docker-compose-kafka.yml up -d --wait
docker compose -f docker/docker-compose-kafka.yml run --rm kafka-init
```

---

## Kit 15.1 — Contrat et producer

**Fichiers fournis :**

- `fil-rouge/src/main/scala/clearing/contract/Events.scala`
- `fil-rouge/src/main/scala/distributed/kafka/KafkaPipeline.scala`

Contrat stable :

```scala
final case class TransactionSubmittedV1(
  id: String,
  sender: String,
  receiver: String,
  amount: BigDecimal,
  status: String,
  transactionType: String
)
```

Règles de frontière :

```scala
val event = TransactionSubmittedV1.fromDomain(tx)
val key: String = tx.sender.value
val payload: String = event.toJson
```

Le producer fourni :

- utilise `sender.value` comme clé ;
- ajoute l’en-tête `transaction-id` ;
- attend l’accusé de réception avant d’annoncer le succès ;
- lit `KAFKA_BOOTSTRAP_SERVERS`.

**Zone stagiaire :** générer un batch déterministe, limiter son débit et compter les callbacks réussis ou échoués.

---

## Kit 15.2 — RecordProcessor, sortie et DLQ

Le `RecordProcessor` fourni retourne exactement une décision :

```scala
enum ProcessingDecision:
  case Validated(key: String, event: TransactionValidatedV1)
  case Rejected(key: Option[String], event: TransactionRejectedV1)
```

Le `ResultPublisher` applique la politique suivante :

| Cas | Topic | Commit autorisé après accusé |
|---|---|---|
| JSON et métier valides | `clearing-output` | oui |
| JSON invalide | `clearing-dlq` | oui |
| erreur métier | `clearing-dlq` | oui |
| publication impossible | aucun | non |

**Zone stagiaire :**

1. Ajouter un champ `occurredAt` au contrat.
2. Écrire le test aller-retour JSON.
3. Vérifier que l’ID reste identique de l’entrée à la sortie.
4. Vérifier que le payload original est conservé dans la DLQ.

---

## Kit 15.3 — Consumer et commit par partition

**Fichier fourni :** `fil-rouge/src/main/scala/distributed/kafka/KafkaPipeline.scala`

Le consumer :

1. regroupe le poll par `TopicPartition` ;
2. traite les offsets dans l’ordre ;
3. attend la publication vers `clearing-output` ou `clearing-dlq` ;
4. prépare `OffsetAndMetadata(dernierOffsetPublié + 1)` ;
5. appelle `commitSync(offsets)` ;
6. ne valide pas la partition qui a échoué.

Le `+ 1` représente le prochain offset à lire.

**Zone stagiaire :**

- appeler `DeduplicationCache.contains(id)` avant le traitement ;
- appeler `markProcessed(id)` seulement après l’accusé de publication ;
- provoquer un crash après publication et avant commit ;
- constater la relecture ;
- expliquer pourquoi la S16 doit rendre l’effet durable et réparable.

---

## Kit 15.4 — Vérification de bout en bout

```bash
#!/usr/bin/env bash
set -euo pipefail

docker compose -f docker/docker-compose-kafka.yml down -v --remove-orphans
docker compose -f docker/docker-compose-kafka.yml up -d --wait
docker compose -f docker/docker-compose-kafka.yml run --rm kafka-init

sbt "runMain distributed.kafka.runKafkaConsumer" &
CONSUMER_PID=$!
trap 'kill "$CONSUMER_PID" 2>/dev/null || true' EXIT

# TODO stagiaire : injecter 50 transactions avec une seed fixe.
# TODO stagiaire : compter input, output et DLQ.

test "$INPUT_COUNT" -eq "$((OUTPUT_COUNT + DLQ_COUNT))"
```

**Preuves attendues :**

- trois topics de trois partitions ;
- même ID entre entrée et sortie ;
- JSON invalide visible dans la DLQ ;
- partition non validée lorsque la publication échoue ;
- `input = output + dlq` pour le jeu déterministe.
