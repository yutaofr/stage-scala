# Semaine 15 : Stream Processing — Kafka ou Flink (5 jours)

## Jour 1 — Architecture Kafka
**Cours (2h)** : Topics, Partitions, Offsets, Consumer Groups. Pourquoi Kafka est l'épine dorsale des systèmes bancaires modernes. Garanties de durabilité.
**TP (4h)** : Docker Compose : lancer un cluster Kafka + Zookeeper. Créer les topics `clearing-input` et `clearing-output` via la CLI.

## Jour 2 — Producers Scala
**Cours (2h)** : Sérialisation des messages (JSON via circe ou play-json). Partitionnement intelligent (par banque émettrice). Acks et durabilité.
**TP (4h)** : Écrire un Producer qui injecte des transactions simulées dans `clearing-input` toutes les 100ms. Vérifier avec la CLI consumer.

## Jour 3 — Consumers & ZIO-Kafka
**Cours (2h)** : Consumer loop. Commit d'offsets (automatique vs manuel). Intégration avec ZIO : transformer le flux Kafka en ZStream.
**TP (4h)** : Consumer qui lit les transactions, les valide avec le moteur v2.3, et publie les résultats dans `clearing-output`.

## Jour 4 — Garanties de Livraison & Idempotence
**Cours (2h)** : At-least-once vs Exactly-once. Idempotence métier : pourquoi c'est vital pour l'argent. Pattern "Deduplication Key" (Transaction ID).
**TP (4h)** : Implémenter un cache de Transaction IDs pour détecter les doublons. Simuler un crash consumer et vérifier la reprise sans double-comptage.

## Jour 5 — Clearing Engine v3.0
**Cours (2h)** : Revue de l'architecture event-driven.
**TP (4h)** : Le moteur v3.0 consomme un flux Kafka en temps réel, traite les virements avec ZIO, et publie les résultats. Démonstration live : lancer le producer et observer les résultats apparaître.
