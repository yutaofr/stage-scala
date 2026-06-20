# TP Jour 4 : Le Dashboard Financier

**Durée :** ~4h | **Fil Rouge :** Reporting multi-dimensionnel

---

## Point de départ

- Réutilise le schéma du **Kit 16.1**.
- Chaque nouvelle requête doit justifier une nouvelle table.
- Les écritures de projection doivent supporter une relecture Kafka.

## Exercice 1 : Double Écriture (Fan-out) (1h30)

1. Crée `pair_activity_by_day` avec `clearing_date` comme partition et la paire comme clustering key.
2. Écris la transaction dans `transactions_by_bank_day` et `clearing_history_by_day`.
3. Mets à jour la projection de paire avec une valeur calculée de façon reproductible.
4. Pour le top 10 du laboratoire, lis la partition journalière et trie les paires dans Scala. Documente qu'une forte cardinalité exigerait une projection de ranking séparée.
5. Provoque une relecture et vérifie l'absence de double comptage.

**Validation :** les deux vues contiennent les mêmes IDs source et la projection reste stable après replay.

---

## Exercice 2 : Calcul de Solde "In-App" (1h30)

1. Implémente `calculateDailyPosition(bankId, date)`.
2. Lis une seule partition `transactions_by_bank_day`.
3. Calcule débits, crédits et position nette.
4. Compare avec `bank_positions`.

**Validation :** les deux calculs produisent la même position et l'invariant global vaut zéro.

---

## Exercice 3 : Simulation Dashboard (1h)

1. Rafraîchis toutes les cinq secondes les positions d'une liste bornée de banques.
2. Utilise `Schedule.fixed`.
3. Affiche l'heure, la date de clearing et l'âge des données.
4. Interromps proprement la Fiber après trois rafraîchissements dans le test.

**Validation :** le programme s'arrête proprement et ne lance pas plusieurs rafraîchissements concurrents.

**Livrable** : Code source implémentant la double écriture et un service de reporting basique sur les données Cassandra.
