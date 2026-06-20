# Semaine 15 : Stream Processing — Apache Kafka (5 jours)

## Jour 1 — Architecture Kafka
**Cours (2h)** : Topics, Partitions, Offsets, Consumer Groups. Pourquoi Kafka est l'épine dorsale des systèmes bancaires modernes. Garanties de durabilité.
**TP (4h)** : Docker Compose : lancer un broker Kafka local en mode KRaft. Créer `clearing-input`, `clearing-output` et `clearing-dlq` via la CLI.

## Jour 2 — Producers Scala
**Cours (2h)** : Sérialisation des messages (JSON via circe ou play-json). Partitionnement intelligent (par banque émettrice). Acks et durabilité.
**TP (4h)** : Écrire un Producer qui injecte des transactions simulées dans `clearing-input` toutes les 100ms. Vérifier avec la CLI consumer.

## Jour 3 — Consumers & ZIO-Kafka
**Cours (2h)** : Consumer loop, consumer groups et commit d'offsets. Intégration progressive avec ZIO ; le TP reste d'abord sur le client Kafka Java pour rendre les garanties visibles.
**TP (4h)** : Consumer qui lit les transactions, les valide avec le moteur v2.4 et publie les résultats dans `clearing-output`.

## Jour 4 — Garanties de Livraison & Idempotence
**Cours (2h)** : At-least-once, producteur idempotent et limites de l'exactly-once. Idempotence métier et stockage durable des clés de déduplication.
**TP (4h)** : Implémenter d'abord un registre mémoire pour observer le mécanisme, puis documenter pourquoi il doit être persisté. Simuler un crash consumer et mesurer les doublons relus.

## Jour 5 — Clearing Engine v3.0
**Cours (2h)** : Revue de l'architecture event-driven.
**TP (4h)** : Le moteur v3.0 consomme un flux Kafka en temps réel, traite les virements avec ZIO, et publie les résultats. Démonstration live : lancer le producer et observer les résultats apparaître.
