---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 3"
footer: "Jour 3 — Fold, Reduce & Agrégations"
---

# Fold, Reduce & Agrégations
## L'agrégation universelle en programmation fonctionnelle

**Durée :** ~2h | **Fil Rouge :** Calcul des positions nettes interbancaires

---

# 📋 Objectifs du Jour

- Comprendre le concept de réduction d'une collection.
- Maîtriser `reduce` pour les agrégations simples.
- Utiliser `foldLeft`, l'outil universel de parcours et d'accumulation.
- Combiner `groupBy` et `mapValues` pour des statistiques par banque.

---

# 1. `reduce` : Condenser une collection

`reduce` combine tous les éléments d'une collection en utilisant une fonction binaire.

```scala
val montants = List(100, 200, 300)

// Faire la somme
val total = montants.reduce((acc, current) => acc + current) // 600

// Version courte
val totalSimple = montants.reduce(_ + _)
```

> ⚠️ Attention : `reduce` crashe sur une liste vide !

---

# 2. `foldLeft` : L'outil universel

`foldLeft` est plus puissant que `reduce` car il accepte une **valeur initiale** (graine).

```scala
val transactions = List(100, -50, 300)

val soldeFinal = transactions.foldLeft(BigDecimal("0")) { (acc, tx) =>
  acc + tx
}
```

### Pourquoi c'est l'outil ultime ?
- Marche sur les listes vides (retourne la valeur initiale).
- Permet de changer de type (ex: de `List[Tx]` vers un `Map[String, Int]`).

---

# 3. Agrégations par Catégorie

Combiner `groupBy` et les fonctions d'agrégation.

```scala
val txs = List(("ATH", 100), ("CIH", 50), ("ATH", 75))

val parBanque = txs.groupBy(_._1) 
// Map("ATH" -> List(...), "CIH" -> List(...))

val totaux = parBanque.mapValues(list => list.map(_._2).sum)
// Map("ATH" -> 175, "CIH" -> 50)
```

---

# 🏗️ Exemple : Le "Vrai" Netting

Calculer le solde net global d'une banque en un seul passage.

```scala
val transactions = List(100, -200, 50, -10)

val soldeCompense = transactions.foldLeft(BigDecimal("0"))(_ + _)
```

> 💡 En FP, on n'utilise jamais de variable `var total = 0` avec une boucle `for`. On utilise `foldLeft`.

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre `reduce` et `foldLeft` ?
2. Que se passe-t-il si j'appelle `reduce` sur une liste vide ?
3. On peut transformer une `List` en un seul `Int` avec quel outil ?

---

# 📝 Résumé du Jour

- `reduce` agrège si la liste n'est pas vide.
- `foldLeft` est plus sûr et plus flexible grâce à sa valeur initiale.
- On peut transformer n'importe quelle collection en une valeur unique (somme, moyenne, etc.) sans aucun effet de bord.

**Prochaine étape** : Réécrire le moteur de netting avec `foldLeft` dans le TP 13 !
