# Semaine 17 — Observabilité du Clearing Engine v3.2

## Objectifs

- Produire des logs JSON corrélés sans donnée sensible.
- Exposer des counters, histogrammes, gauges JVM et lag Kafka à Prometheus.
- Propager un contexte W3C à travers Kafka et visualiser les étapes dans
  Jaeger.
- Provisionner un dashboard Grafana sans configuration manuelle.
- Définir deux SLO et observer une alerte pending, firing puis resolved.

## Notions à maîtriser

- complémentarité des logs, métriques et traces ; corrélation par contexte ;
- logs JSON, redaction des données sensibles et contexte MDC ;
- counter, gauge, histogramme, cardinalité des labels, SLI et SLO ;
- trace, span, propagation W3C et export OTLP ;
- PromQL, dashboard provisionné, seuil d'alerte, error budget et burn rate.

## Exercices couverts

1. Produire des logs JSON corrélés contenant `txId`, partition et offset sans
   exposer d'IBAN brut.
2. Instrumenter le traitement avec un compteur, un histogramme et un gauge de
   lag Kafka ; vérifier les métriques exposées.
3. Créer les spans de traitement, propager le contexte W3C dans Kafka et suivre
   l'opération dans Jaeger.
4. Provisionner un dashboard Grafana présentant débit, p99, taux d'erreur et
   lag Kafka.
5. Définir deux SLO, déclencher une alerte et vérifier son cycle pending,
   firing, puis resolved.

## Premier livrable — Jour 1

Un extrait de logs JSON corrélés, accompagné d'un test démontrant la présence
de `txId`, partition et offset, ainsi que l'absence d'IBAN brut. Ce livrable
est inclus dans le paquet `clearing.v32` et dans les preuves J1 de la semaine.

## Livrables

- paquet `clearing.v32` et main `runClearingAppV32`;
- `docker-compose-v32.yml` et configurations `docker/observability/`;
- dashboard `clearing-engine-v32` avec cinq panneaux;
- `slo-v32.md`, règles Prometheus, Alertmanager et webhook;
- preuves J1 à J5, rétrospective et gate `verify-v32-runtime.sh`.

## Critères de validation et résultats

État : validée le 14/07/2026.

Le gate de contrat `testOnly clearing.v32.*` couvre les API et fichiers. Le
gate réel repart de volumes propres, traite 500 records, réconcilie les
métriques, lit les cinq opérations Jaeger, vérifie la datasource et les cinq
panneaux Grafana, provoque `EngineDown`, contrôle le webhook et recherche les
IBAN bruts.

Résultats :

- gate réel corrigé : `V32_RUNTIME_OK records=500 lag=0 pending firing
  resolved`, avec 485 sorties, 5 DLQ, 970 IBAN d'entrée scannés et deux
  notifications de panne corrélées par le même `startsAt`;
- Cassandra live : 1/1 test réussi;
- Java 21.0.6 : 594 tests réussis, 1 test live annulé par défaut;
- Java 17.0.14 : 594 tests réussis, 1 test live annulé par défaut;
- revue senior finale : 0 Critical, 0 Important, 0 Minor;
- limite documentaire : la capture visuelle Grafana reste à refaire, car la
  connexion navigateur de l'environnement a échoué; les API runtime sont
  validées.
