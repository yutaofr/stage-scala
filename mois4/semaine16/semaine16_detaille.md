# Semaine 16 : Persistance NoSQL — Cassandra (5 jours)

## Jour 1 — Modélisation Cassandra
**Cours (2h)** : Orientation Query : dessiner les requêtes avant le schéma. Clés de partition (distribution) vs clés de clustering (tri). Quand dénormaliser.
**TP (4h)** : Concevoir le schéma `clearing_history` (partitionné par date, clustered par bank_id). Concevoir `bank_positions` (partitionné par bank_id).

## Jour 2 — Drivers Asynchrones & ZIO
**Cours (2h)** : Drivers non-bloquants. Intégration dans l'écosystème ZIO (wrapper le driver Java). Gestion des connexions.
**TP (4h)** : Créer un `ClearingRepository` ZIO qui insère les résultats de compensation dans Cassandra.

## Jour 3 — Pipeline Complet (Kafka → ZIO → Cassandra)
**Cours (2h)** : Assemblage final de la chaîne de données distribuée. Backpressure : ne pas saturer Cassandra.
**TP (4h)** : Connecter le consumer Kafka du v3.0 au `ClearingRepository`. Chaque batch de clearing est automatiquement archivé.

## Jour 4 — Dénormalisation & Requêtes Dashboard
**Cours (2h)** : Créer une vue dénormalisée pour le dashboard interbancaire. Requêtes par date, par banque, par statut.
**TP (4h)** : Implémenter les requêtes : "Solde net de la banque ATH pour la journée du 15/07/2025". "Top 10 des paires de banques les plus actives".

## Jour 5 — Clearing Engine v3.1 & Démo Mois 4
**Cours (2h)** : Bilan Mois 4 : Le système est distribué, résilient et persistant. Revue de l'architecture complète.
**TP (4h)** : Le moteur v3.1 est le pipeline complet : les virements entrent par Kafka, sont compensés par ZIO, et les résultats sont archivés dans Cassandra. Test de résilience : couper Cassandra, observer la reprise. **Démonstration au tuteur.**
