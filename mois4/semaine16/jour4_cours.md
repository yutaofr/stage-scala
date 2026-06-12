---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 16"
footer: "Jour 4 — Dénormalisation & Reporting"
---

# Reporting Big Data
## Extraire de la valeur de milliards de lignes

**Durée :** ~2h | **Fil Rouge :** Dashboard des banques en temps réel

---

# 📋 Objectifs du Jour

- Apprendre à créer des "Vues" dénormalisées dans Cassandra.
- Comprendre le concept de **Materialized Views** (et ses limites).
- Utiliser Scala pour agrégatuer des données de reporting.
- Simuler un dashboard financier temps réel.

---

# 1. Multiplier les tables pour multiplier les vues

Si on veut voir l'historique par **Banque** ET l'historique par **Date**, on crée deux tables !

1. `transactions_by_bank` (Partition key: bank_id).
2. `transactions_by_date` (Partition key: day_id).

> 💡 Le coût du stockage (Disk) est faible par rapport au coût du calcul (CPU). On préfère écrire deux fois la donnée plutôt que de faire un scan lent.

---

# 2. L'Agrégation Côté Application

Cassandra ne sait pas faire de `SUM()` ou de `AVG()` complexes sur des millions de lignes sans index.
C'est souvent l'application Scala qui récupère les données et fait le calcul (ou on utilise Spark).

```scala
val allTxs: ZIO[Any, Throwable, List[Transaction]] = repo.findAllByBank(bankId)
val total = allTxs.map(_.map(_.amount).sum)
```

---

# 3. Réaliser un Dashboard

Pour un dashboard temps réel, on utilise souvent une table de "Compteurs" ou de "Positions Actuelles".

```sql
CREATE TABLE current_positions (
    bank_id text PRIMARY KEY,
    net_position decimal
);
```

On met à jour cette table à chaque fois qu'une transaction passe dans le flux Kafka.

---

# 🏗️ Application : Le Dashboard d'ATH

Nous allons ajouter une étape à notre pipeline : après l'archivage dans `transaction_history`, nous mettrons à jour une table `bank_balances` qui contient le solde vivant de chaque banque.

---

# 🧠 Quiz Rapide

1. Pourquoi ne pas utiliser une seule table et faire des `INDEX` ? (Parce que les index Cassandra sont peu performants sur de gros volumes de données).
2. Est-ce grave d'avoir la même donnée dans 3 tables différentes ? (Non, c'est le principe de la dénormalisation).
3. Quel service est responsable de l'écriture dans ces multiples tables ? (Le moteur de clearing, en mode "Fan-out").

---

# 📝 Résumé du Jour

- La dénormalisation est la clé de la rapidité NoSQL.
- On écrit plus, pour lire plus vite.
- Ton application est maintenant capable de fournir des réponses instantanées à la direction financière.
- Tu as complété la vision "Temps Réel + Historique" (Architecture Lambda/Kappa).

**Prochaine étape** : Créer tes tables de reporting dans le TP 79 !
