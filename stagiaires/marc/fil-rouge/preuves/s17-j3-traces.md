# S17 J3 — Traces OpenTelemetry et propagation Kafka

## Réalisation

Les anciennes versions reçoivent deux ports no-op par défaut :

- `RailwayStageObserver` entoure les vrais blocs `parse`, `validate` et
  `netting` du railway v2.2;
- `DurableStageObserver` entoure la machine d'état durable après le calcul de
  la décision v3.1.

Les adapters v3.2 créent un span `clearing.consume` de type `CONSUMER`, puis les
quatre spans enfants `parse`, `validate`, `netting` et `persist`. Le transaction
ID reste l'attribut `tx.id`; il n'entre dans aucun nom de span. Topic,
partition et offset sont des attributs de trace, pas des labels Prometheus.

`TracingKafkaDecisionPublisher` réutilise le publisher Kafka v3.0. Un injector
ajoute le contexte W3C actif aux headers du vrai `ProducerRecord` avant
`send().get()`. Le consumer extrait le contexte depuis `RecordEnvelope.headers`.
Un message sans contexte valide démarre une nouvelle trace.

## Preuves automatisées

Commande exécutée sous Eclipse Temurin Java 21.0.6 :

```bash
sbt 'testOnly clearing.v32.ClearingTracingSpec \
  clearing.v32.KafkaTracePropagationSpec \
  clearing.v22.* clearing.v30.* clearing.v31.*'
```

Résultat : 130 tests réussis, 0 échec, 1 test Cassandra live annulé par défaut.

L'exporteur OpenTelemetry mémoire vérifie :

- les noms exacts `clearing.consume`, `parse`, `validate`, `netting` et
  `persist`;
- le même trace ID pour les cinq spans;
- le span ID de `clearing.consume` comme parent direct des quatre étapes;
- `tx.id=1` comme attribut et aucun ID dans les noms;
- le statut `ERROR` et l'événement `exception` lors d'une exception inattendue;
- un header `traceparent` W3C valide sur le record Kafka produit;
- la continuation du trace ID et du parent span ID lors de la consommation;
- un nouveau contexte racine valide en l'absence de header.

Le gate Docker final a exporté par OTLP vers le Collector puis Jaeger. L'API
Jaeger a retourné 20 traces récentes et les cinq opérations attendues :
`clearing.consume`, `parse`, `validate`, `netting` et `persist`. La réponse
brute est conservée pendant le gate dans `target/v32-runtime/jaeger-traces.json`.
