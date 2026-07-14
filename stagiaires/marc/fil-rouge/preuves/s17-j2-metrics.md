# S17 J2 — Métriques Prometheus et endpoint HTTP

## Réalisation

`ClearingMetrics` possède un `PrometheusMeterRegistry` et instrumente le port
`DurableProcessing`. Il pré-enregistre trois statuts bornés : `success`,
`duplicate` et `failure`. Chaque appel incrémente exactement un counter et un
timer, y compris lorsqu'une exception inattendue est relancée.

Les séries exposées sont :

- `clearing_transactions_processed_total{status=...}`;
- `clearing_processing_duration_seconds` avec histogramme et buckets à 10,
  50, 100, 250, 500 et 1000 ms;
- `clearing_kafka_lag_records{topic,partition}`;
- les gauges `jvm_memory_used_bytes` fournies par le binder JVM Micrometer.

Le lag vient du consumer Kafka actif. Après chaque poll, l'observateur calcule
`max(0, endOffset - position)` pour chaque partition assignée. Il conserve un
`AtomicLong` par topic-partition afin de mettre à jour la gauge sans réinscrire
le meter.

`MetricsHttpServer` utilise le serveur HTTP du JDK. Il expose `/metrics` au
format texte Prometheus et `/health` sur le même port. Le port par défaut sera
8080; les tests utilisent le port 0 pour laisser le système choisir un port
libre. Le serveur implémente `AutoCloseable`.

## Preuves automatisées

Commande exécutée sous Eclipse Temurin Java 21.0.6 :

```bash
sbt 'testOnly clearing.v32.ClearingMetricsSpec \
  clearing.v32.MetricsHttpServerSpec \
  clearing.v32.KafkaLagObserverSpec \
  clearing.v31.KafkaConsumerV31Spec'
```

Résultat : 11 tests réussis, 0 échec.

Les tests vérifient :

- un counter et une observation de durée pour chacun des trois statuts;
- une exception comptée comme `failure`, puis relancée;
- la présence des buckets Prometheus et des gauges mémoire JVM;
- l'absence de `txId` et `offset` parmi les labels;
- un lag calculé à 7 records, puis ramené à 0 lorsque la position atteint
  l'end offset;
- l'appel de l'observateur après chaque poll du consumer v3.1;
- les réponses HTTP, content types et contenus réels de `/metrics` et
  `/health`;
- le refus de connexion après fermeture du serveur.

Le gate Docker final a observé la cible `clearing-engine` successivement `up`,
`down`, puis `up`. Avant le redémarrage volontaire, Prometheus a réconcilié
exactement 500 traitements et un lag total nul. Le gate a aussi lu directement
les trois familles `clearing_*` sur `/metrics`.
