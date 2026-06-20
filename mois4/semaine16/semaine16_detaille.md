# Semaine 16 : Persistance NoSQL — Cassandra (5 jours)

## Jour 1 — Modélisation Cassandra
**Cours (2h)** : Orientation Query : dessiner les requêtes avant le schéma. Clés de partition (distribution) vs clés de clustering (tri). Quand dénormaliser.
**TP (4h)** : Concevoir `processing_state`, `transactions_by_bank_day`, `clearing_history_by_day`, `bank_positions` et `pair_activity_by_day` à partir des requêtes du fil rouge.

## Jour 2 — Drivers Asynchrones & ZIO
**Cours (2h)** : Driver Java asynchrone, `CompletionStage`, `ZIO.fromCompletionStage`, requêtes préparées et session partagée.
**TP (4h)** : Créer un `ClearingRepository` ZIO qui persiste les transactions et les positions sans bloquer les threads ZIO.

## Jour 3 — Pipeline Complet (Kafka → ZIO → Cassandra)
**Cours (2h)** : Assemblage final de la chaîne de données distribuée. Backpressure : ne pas saturer Cassandra.
**TP (4h)** : Connecter le consumer Kafka du v3.0 au `ClearingRepository`. Persister avant le commit et rendre la reprise observable.

## Jour 4 — Dénormalisation & Requêtes Dashboard
**Cours (2h)** : Créer une vue dénormalisée pour le dashboard interbancaire. Requêtes par date, par banque, par statut.
**TP (4h)** : Implémenter les requêtes : position d'une banque pour une journée et top 10 des paires de banques pour une journée, avec une table dédiée par requête.

## Jour 5 — Clearing Engine v3.1 & Démo Mois 4
**Cours (2h)** : Bilan Mois 4 : Le système est distribué, résilient et persistant. Revue de l'architecture complète.
**TP (4h)** : Le moteur v3.1 est le pipeline complet : les virements entrent par Kafka, sont compensés par ZIO, et les résultats sont archivés dans Cassandra. Test de résilience : couper Cassandra, observer la reprise. **Démonstration au tuteur.**
