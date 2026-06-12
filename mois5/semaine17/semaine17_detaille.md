# Semaine 17 : Observabilité (5 jours)

## Jour 1 — Les 3 Piliers (Logs, Métriques, Traces)
**Cours (2h)** : Pourquoi les `println` ne suffisent plus en prod. Logs = "Ce qui s'est passé". Métriques = "L'état en ce moment". Traces = "Le chemin d'une requête individuelle".
**TP (4h)** : Configurer `Logback` pour produire des logs JSON structurés. Ajouter un champ `correlationId` à chaque log de transaction.

## Jour 2 — Métriques Prometheus
**Cours (2h)** : Types de métriques (Counter, Gauge, Histogram). Exposer un endpoint `/metrics`. Définir les SLOs du clearing (latence < 500ms, taux d'erreur < 0.1%).
**TP (4h)** : Instrumenter le moteur : `transactions_processed_total` (Counter), `clearing_batch_duration_seconds` (Histogram), `kafka_consumer_lag` (Gauge).

## Jour 3 — Tracing Distribué (OpenTelemetry)
**Cours (2h)** : Spans, Traces, Context Propagation. Suivre un virement de Kafka à Cassandra. Visualisation avec Jaeger ou Zipkin.
**TP (4h)** : Intégrer OpenTelemetry. Créer un span par étape du pipeline (parsing, validation, netting, persistence). Visualiser dans Jaeger.

## Jour 4 — Dashboards Grafana
**Cours (2h)** : Création de tableaux de bord visuels. PromQL basics. Alerting.
**TP (4h)** : Créer un dashboard "Clearing Operations" : débit en temps réel, latence P99, taux d'erreur. Configurer une alerte si le lag Kafka dépasse 1000.

## Jour 5 — Automatisation CI/CD
**Cours (2h)** : GitHub Actions : Build → Test → Publish. Docker images optimisées.
**TP (4h)** : Écrire un workflow GitHub Actions complet. Build l'image Docker du moteur et la pousser dans un registry.
