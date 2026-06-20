---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 16"
footer: "Jour 1 — Modélisation Cassandra (Orientation Query)"
---

# Apache Cassandra
## Le stockage à l'échelle du pays

**Durée :** ~2h | **Fil Rouge :** Archiver des milliards de transactions

---

# 📋 Objectifs du Jour

- Comprendre quand Cassandra répond mieux qu'une base relationnelle.
- Apprendre les principes de la modélisation NoSQL (Wide-Column Store).
- Découvrir la loi d'or : **"Une Table par Requête"**.
- Créer son premier schéma de table pour l'historique du clearing.

---

# 1. Pourquoi Cassandra pour ATH ?

Nous traitons des millions de transactions par jour. Cassandra apporte :
- **Haute disponibilité** : l'architecture distribuée évite un maître unique, à condition de configurer réplication et cohérence.
- **Écritures séquentielles** : commit log et memtables favorisent un débit d'écriture élevé.
- **Scalabilité horizontale** : l'ajout de nœuds peut augmenter la capacité, après mesure et rééquilibrage.

---

# 2. Modélisation Orientée Requête

En SQL : On normalise (Jointures).
En NoSQL : On **Dénormalise**. On crée la table en fonction de ce qu'on veut afficher.

### Exemple : Historique par Banque
On veut pouvoir dire : "Donne-moi toutes les transactions de la banque CIH".
- **Partition Key** : `bank_id` (C'est ce qui définit sur quel serveur l'info est stockée).
- **Clustering Key** : `timestamp` (C'est ce qui définit l'ordre de tri sur le serveur).

---

# 3. Le langage CQL

Il ressemble au SQL mais n'autorise pas les jointures (`JOIN`).

```sql
CREATE TABLE transaction_history (
    bank_id text,
    month int,
    tx_id uuid,
    amount decimal,
    PRIMARY KEY ((bank_id, month), tx_id)
);
```

> 💡 Notez la clé de partition composée `(bank_id, month)` pour éviter d'avoir des partitions trop grosses.

---

# 🏗️ Application : Setup Cassandra

Nous allons ajouter un nœud Cassandra à notre `docker-compose.yml` et créer l'espace de noms (`Keyspace`) pour le clearing d'ATH.

---

# 🧠 Quiz Rapide

1. Puis-je faire un `JOIN` entre deux tables Cassandra ? (Non).
2. Qu'est-ce qu'une table dénormalisée ? (Une table qui contient toutes les infos nécessaires sans avoir besoin d'aller voir ailleurs).
3. Pourquoi la partition key est-elle cruciale ? (Parce qu'une mauvaise clé peut saturer un seul serveur pendant que les autres dorment).

---

# 📝 Résumé du Jour

- Cassandra est le champion de l'écriture massive.
- On modélise pour les lectures futures, pas pour la propreté théorique.
- La dénormalisation est ton amie.
- Demain, nous allons y écrire depuis Scala via ZIO.

**Prochaine étape** : Utiliser les Kits 16.0 et 16.1 dans le TP du Jour 1.
