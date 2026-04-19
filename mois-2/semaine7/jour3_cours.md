---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 7"
footer: "Jour 3 — SortBy, Partition, Sliding"
---

# Collections Avancées
## Manier les flux avec précision et élégance

**Durée :** ~2h | **Fil Rouge :** Tri et fenêtrage des transactions

---

# 📋 Objectifs du Jour

- Ordonner les données avec `sortBy` et `sortWith`.
- Séparer les flux en deux avec `partition`.
- Analyser des séquences avec `sliding` (fenêtres glissantes).
- Regrouper par paquets de taille fixe avec `grouped`.

---

# 1. Le Tri : `sortBy` vs `sortWith`

### `sortBy` : Tri simple sur une propriété
```scala
// Trier par montant croissant
val trie = txs.sortBy(_.amount)

// Trier par date décroissante
val recent = txs.sortBy(_.timestamp).reverse
```

### `sortWith` : Logique de comparaison personnalisée
```scala
val complexe = txs.sortWith((a, b) => 
  if a.sender == b.sender then a.amount > b.amount
  else a.sender < b.sender
)
```

---

# 2. `partition` : Diviser pour régner

`partition` sépare une collection en deux listes selon un prédicat.

```scala
val (valides, invalides) = txs.partition(_.status == Validated)
```

- C'est **beaucoup plus efficace** que de faire deux `filter` (un seul parcours).
- Très utile pour séparer le flux principal des anomalies à traiter plus tard.

---

# 3. Fenêtrage : `sliding` & `grouped`

### `grouped(n)` : Découper en paquets de taille N
Utile pour le traitement par lots (Batch processing).
```scala
val batches = txs.grouped(100) // Iterator[List[Transaction]]
```

### `sliding(size, step)` : Fenêtre glissante
Utile pour les statistiques (ex: moyenne mobile).
```scala
val fenetres = txs.sliding(3) // Groupes de 3 successifs
```

---

# ⚠️ Note Métier : Date de Valeur

> [!NOTE]
> En production, les fenêtres de `sliding` seraient basées sur la **Date de Valeur** (cut-off horaire de la Banque Centrale), et non sur l'ordre d'arrivée des transactions. Notre moteur simplifie cet aspect en triant par ID ou timestamp arbitraire. C'est un compromis acceptable pour se concentrer sur la maîtrise de l'outil `sliding`.

---

# 🏗️ Application : Détection de Burst

On peut utiliser `sliding` pour détecter si une banque envoie trop de transactions en un temps très court.

```scala
txs.sortBy(_.timestamp).sliding(10).foreach { window =>
  val duration = durationBetween(window.head, window.last)
  if duration < seconds(1) then println("🚨 BURST DETECTED")
}
```

---

# 🧠 Quiz Rapide

1. Quelle méthode utiliser pour séparer une liste en "Transactions > 0" et "Transactions <= 0" en une seule passe ?
2. Quelle est la différence entre `grouped` et `sliding` ?
3. Comment trier une liste de banques par leur nom de Z à A ?

---

# 📝 Résumé du Jour

- `sortBy` organise tes données.
- `partition` segmente tes flux proprement.
- `grouped` et `sliding` préparent le traitement par lots et l'analyse de flux.
- Tu as maintenant une maîtrise totale sur la forme de tes collections de données.

**Prochaine étape** : Segmenter et analyser tes flux dans le TP 33 !
