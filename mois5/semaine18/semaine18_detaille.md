# Semaine 18 : Robustesse & Performance (5 jours)

## Jour 1 — Tests de Charge (Gatling)
**Cours (2h)** : Introduction à Gatling (framework Scala). Modélisation de la charge. Ramp-up, plateau, peak. Métriques clés : throughput, latence P50/P95/P99.
**TP (4h)** : Écrire un scénario Gatling contre l'adaptateur HTTP de charge fourni, lequel publie dans Kafka. Monter progressivement jusqu'à la limite mesurée.

## Jour 2 — Chaos Engineering
**Cours (2h)** : Principes de résilience. Hypothèse → Expérience → Observation. Simuler des pannes : latence réseau, perte de broker Kafka, Cassandra indisponible.
**TP (4h)** : Couper Cassandra puis une instance du moteur. Un test de panne Kafka exige plusieurs brokers, une réplication adaptée et des préconditions vérifiées.

## Jour 3 — Profiling JVM
**Cours (2h)** : VisualVM / Async-Profiler. Thread Dumps. Flame Graphs. Identifier les "hot methods".
**TP (4h)** : Profiler l'application sous charge. Trouver la fonction la plus consommatrice en CPU. L'optimiser et mesurer le gain.

## Jour 4 — Tuning GC & JVM
**Cours (2h)** : G1GC, ZGC, heap, logs GC et objectifs de pause. Les paramètres sont des hypothèses à comparer.
**TP (4h)** : Comparer plusieurs configurations avec la même charge et documenter le compromis observé.

## Jour 5 — High Availability
**Cours (2h)** : Scaling horizontal. Consumer Groups Kafka. Load Balancing.
**TP (4h)** : Lancer trois instances du moteur, observer l'affectation des partitions, arrêter une instance et mesurer le rééquilibrage.
