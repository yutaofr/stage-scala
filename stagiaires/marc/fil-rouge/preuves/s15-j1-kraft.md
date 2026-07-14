# Preuve S15 J1 — Kafka KRaft

Date d'exécution : 14 juillet 2026
Environnement : macOS, Docker Compose, `apache/kafka:4.3.0`

## Validation statique

```bash
docker compose -f docker/docker-compose-kafka.yml config
sbt 'testOnly clearing.v30.KafkaComposeContractSpec'
```

Résultat : configuration Compose valide et 4 tests réussis. Le contrat vérifie
KRaft sans ZooKeeper, les listeners séparés, le healthcheck broker et les trois
topics.

## Démarrage du broker

```bash
docker compose -f docker/docker-compose-kafka.yml up -d --wait
docker compose -f docker/docker-compose-kafka.yml ps -a
```

Résultat observé :

```text
kafka       Up (healthy)   0.0.0.0:9092->9092/tcp
kafka-init  Exited (0)
```

Le log `kafka-init` confirme la création de :

```text
clearing-input   PartitionCount: 3   ReplicationFactor: 1
clearing-output  PartitionCount: 3   ReplicationFactor: 1
clearing-dlq     PartitionCount: 3   ReplicationFactor: 1
```

Le facteur 1 est réservé à ce laboratoire mono-broker. Il ne constitue pas une
configuration de production.

## Round-trip console et groupe

Un événement JSON valide, avec clé `ATH`, a été envoyé avec
`kafka-console-producer.sh`, puis relu avec
`kafka-console-consumer.sh --group marc-s15-j1 --max-messages 1`. La valeur
complète n'est pas recopiée dans cette preuve afin de ne pas conserver les IBAN
bruts dans un document ou un log de projet.

Résultat observé :

```text
clé consommée : ATH
Processed a total of 1 messages
```

La description du groupe après consommation confirme :

```text
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
marc-s15-j1     clearing-input  0          1               1               0
marc-s15-j1     clearing-input  1          0               0               0
marc-s15-j1     clearing-input  2          0               0               0
```

Cette preuve valide le socle J1. Elle ne valide pas encore la sémantique du
consumer applicatif ni l'at-least-once, qui appartiennent aux J3 et J4.
