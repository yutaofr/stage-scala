# Semaine 17 — Observabilité du Clearing Engine v3.2

## Objectifs

- Produire des logs JSON corrélés sans donnée sensible.
- Exposer des counters, histogrammes, gauges JVM et lag Kafka à Prometheus.
- Propager un contexte W3C à travers Kafka et visualiser les étapes dans
  Jaeger.
- Provisionner un dashboard Grafana sans configuration manuelle.
- Définir deux SLO et observer une alerte pending, firing puis resolved.

## Livrables

- paquet `clearing.v32` et main `runClearingAppV32`;
- `docker-compose-v32.yml` et configurations `docker/observability/`;
- dashboard `clearing-engine-v32` avec cinq panneaux;
- `slo-v32.md`, règles Prometheus, Alertmanager et webhook;
- preuves J1 à J5, rétrospective et gate `verify-v32-runtime.sh`.

## Validation

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
