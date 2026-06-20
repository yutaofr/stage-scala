# Semaine 17 : Observabilité (5 jours)

## Jour 1 — Signaux d'observabilité et logs structurés
**Cours (2h)** : Pourquoi les `println` ne suffisent plus. Logs, métriques et traces sont des signaux complémentaires ; leur corrélation repose sur un contexte propagé.
**TP (4h)** : Configurer des logs JSON et ajouter `txId`, partition et offset avec des annotations compatibles avec les Fibers.

## Jour 2 — Métriques Prometheus
**Cours (2h)** : Counter, Gauge et Histogram, cardinalité des labels et endpoint `/metrics`. Définir des SLI mesurables avant de choisir les objectifs.
**TP (4h)** : Instrumenter le moteur : `transactions_processed_total` (Counter), `clearing_batch_duration_seconds` (Histogram), `kafka_consumer_lag` (Gauge).

## Jour 3 — Tracing Distribué (OpenTelemetry)
**Cours (2h)** : Spans, Traces, Context Propagation. Suivre un virement de Kafka à Cassandra. Visualisation avec Jaeger ou Zipkin.
**TP (4h)** : Intégrer OpenTelemetry via OTLP. Créer un span par étape et propager le contexte W3C dans les headers Kafka. Visualiser dans Jaeger.

## Jour 4 — Dashboards Grafana
**Cours (2h)** : Tableaux de bord, PromQL, unités et fenêtres temporelles.
**TP (4h)** : Créer un dashboard "Clearing Operations" : débit, latence P99, taux d'erreur et lag Kafka.

## Jour 5 — Alerting & SLOs (Clearing Engine v3.2)
**Cours (2h)** : SLI, SLO, error budget, alertes actionnables et burn rate.
**TP (4h)** : Définir des objectifs justifiés, écrire les règles Prometheus et vérifier leur routage dans Alertmanager.
