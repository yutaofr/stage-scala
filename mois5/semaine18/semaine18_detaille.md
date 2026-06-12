# Semaine 18 : Robustesse & Performance (5 jours)

## Jour 1 — Tests de Charge (Gatling)
**Cours (2h)** : Introduction à Gatling (framework Scala). Modélisation de la charge. Ramp-up, plateau, peak. Métriques clés : throughput, latence P50/P95/P99.
**TP (4h)** : Écrire un scénario Gatling qui injecte 100 000 transactions par minute dans le topic Kafka. Observer le comportement du moteur sous charge.

## Jour 2 — Chaos Engineering
**Cours (2h)** : Principes de résilience. Hypothèse → Expérience → Observation. Simuler des pannes : latence réseau, perte de broker Kafka, Cassandra indisponible.
**TP (4h)** : Couper un broker Kafka pendant le traitement. Vérifier que le consumer reprend sans perte de données. Injecter de la latence sur Cassandra et observer l'impact.

## Jour 3 — Profiling JVM
**Cours (2h)** : VisualVM / Async-Profiler. Thread Dumps. Flame Graphs. Identifier les "hot methods".
**TP (4h)** : Profiler l'application sous charge. Trouver la fonction la plus consommatrice en CPU. L'optimiser et mesurer le gain.

## Jour 4 — Tuning GC & JVM
**Cours (2h)** : Comment fonctionne G1GC. Paramétrage de la Heap (-Xmx, -Xms). Réduction du "Stop The World".
**TP (4h)** : Comparer les performances avec différentes configs GC. Documenter la configuration optimale.

## Jour 5 — High Availability
**Cours (2h)** : Scaling horizontal. Consumer Groups Kafka. Load Balancing.
**TP (4h)** : Lancer 3 instances du moteur. Vérifier que Kafka répartit bien les partitions entre elles. Tuer une instance et observer le rééquilibrage.
