# TP Jour 1 : La Puissance de Cassandra

**Durée :** ~4h | **Fil Rouge :** Design du schéma d'archivage

---

## Point de départ

- Copie le **Kit 16.0** et le **Kit 16.1** de `starter_kit_s16.md`.
- Écris d'abord les requêtes attendues, puis vérifie que chaque requête fournit toute sa clé de partition.
- N'utilise pas `ALLOW FILTERING` pour contourner un schéma inadapté.

## Exercice 1 : Le Cluster Cassandra (Starter Kit)

> [!IMPORTANT]
> **Starter Kit Infrastructure :** Déployer Cassandra de zéro est complexe. Pour te concentrer sur la modélisation des données, utilise l'infrastructure pré-configurée.

1. Démarre Cassandra avec `docker compose -f docker/docker-compose-full.yml up -d --wait cassandra`.
2. Lance `docker compose -f docker/docker-compose-full.yml run --rm cassandra-init`.
3. Ouvre un terminal interactif :
   ```bash
   docker exec -it cassandra-node cqlsh
   ```

**Validation :** le keyspace et les quatre tables du kit apparaissent dans `DESCRIBE KEYSPACE`.

---

## Exercice 2 : Création du Keyspace & Table (1h30)

1. Pour le laboratoire local, conserve une réplication à `1` et note qu'elle n'est pas tolérante aux pannes.
2. Étudie `transactions_by_bank_day` et `clearing_history_by_day`. Explique pourquoi l'historique journalier est réparti en plusieurs buckets.
3. Insère trois transactions pour deux banques et deux dates.
4. Dessine la répartition logique des partitions.

**Validation :** la requête banque + jour ne scanne qu'une partition logique.

---

## Exercice 3 : Requêtes de Lecture (1h30)

1. Lis les transactions d'une banque pour une journée.
2. Tente la requête « top des paires » sur cette table et observe qu'elle n'est pas adaptée.
3. Propose la clé de `pair_activity_by_day`.
4. Explique le coût potentiel d'`ALLOW FILTERING`.

**Validation :** le livrable relie chaque requête à une table et à une clé de partition.

**Livrable** : Schéma CQL de la table `transactions_by_day` et captures d'écran des premiers SELECT réussis.
