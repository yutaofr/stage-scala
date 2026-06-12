# TP Jour 1 : La Puissance de Cassandra

**Durée :** ~4h | **Fil Rouge :** Design du schéma d'archivage

---

## Exercice 1 : Le Cluster Cassandra (Starter Kit)

> [!IMPORTANT]
> **Starter Kit Infrastructure :** Déployer Cassandra de zéro est complexe. Pour te concentrer sur la modélisation des données, utilise l'infrastructure pré-configurée.

1. Démarre l'infrastructure via : `docker-compose -f docker-compose.infra.yml up -d cassandra`
2. Ce fichier contient déjà les *Healthchecks* nécessaires et installe l'outil CLI interne.
3. Attends 1 minute, puis ouvre un terminal interactif dans le conteneur :
   ```bash
   docker exec -it cassandra-node cqlsh
   ```

---

## Exercice 2 : Création du Keyspace & Table (1h30)

1. Crée un Keyspace nommé `ath_clearing` avec une réplication simple.
2. Crée la table `transactions_by_day` optimisée pour répondre à la question : "Quelles sont les transactions de telle banque pour tel jour ?".
3. Insère manuellement 3 lignes via `INSERT INTO`.

---

## Exercice 3 : Requêtes de Lecture (1h30)

1. Effectue un `SELECT` filtré sur une banque.
2. Tente de filtrer sur un champ qui n'est pas dans la clé primaire. Observe l'erreur (ou l'obligation d'utiliser `ALLOW FILTERING`).
3. **Réflexion** : Pourquoi est-il dangereux d'utiliser `ALLOW FILTERING` en production ?

**Livrable** : Schéma CQL de la table `transactions_by_day` et captures d'écran des premiers SELECT réussis.
