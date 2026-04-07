---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 3"
footer: "Jour 2 — Map, Filter, FlatMap (Le Triptyque d'Or)"
---

# Le Triptyque d'Or
## Map, Filter & FlatMap : Transformer les flux de données

**Durée :** ~2h | **Fil Rouge :** Nettoyage et enrichissement des transactions

---

# 📋 Objectifs du Jour

- Maîtriser les trois fonctions fondamentales de la FP.
- Apprendre à transformer des données avec `map`.
- Sélectionner des données avec `filter`.
- Comprendre le concept d'aplatissement avec `flatMap`.
- Faire le lien avec les `for-comprehensions`.

---

# 1. `map` : La Transformation

`map` applique une fonction à **chaque élémént** d'une collection.

```scala
val montants = List(100, 200, 300)

// Ajouter la TVA
val avecTva = montants.map(m => m * 1.20)
// List(120.0, 240.0, 360.0)

// Transformer en noms de banques via une Map
val ids = List("001", "002")
val noms = ids.map(id => repertoire.getOrElse(id, "INCONNU"))
```

---

# 2. `filter` : La Sélection

`filter` conserve uniquement les éléments qui satisfont un **prédicat** (Boolean).

```scala
val transactions = List(1500, -50, 4000, 10, 8000)

// Garder les transactions significatives
val importantes = transactions.filter(amt => amt > 5000)
// List(8000)

// Supprimer les erreurs (montants négatifs)
val valides = transactions.filter(_ >= 0)
```

---

# 3. `flatMap` : Transformer & Aplatir

`flatMap` est utile quand la fonction de transformation retourne elle-même une collection.

```scala
val banques = List("ATH", "CIH")

def getComptes(b: String): List[String] = List(s"$b-01", s"$b-02")

// Avec map -> List(List("ATH-01", "ATH-02"), List("CIH-01", "CIH-02"))
// Avec flatMap -> List("ATH-01", "ATH-02", "CIH-01", "CIH-02") ✅
val tousLesComptes = banques.flatMap(b => getComptes(b))
```

---

# 🔗 Lien avec les For-Comprehensions

Le compilateur Scala traduit les `for` en appels à `map`, `flatMap` et `withFilter`.

```scala
// Ce code...
for
  b <- banques
  c <- getComptes(b)
yield c

// ...est équivalent à :
banques.flatMap(b => getComptes(b).map(c => c))
```

---

# 🧠 Quiz Rapide

1. Quelle est la différence de résultat entre `map` et `flatMap` ?
2. Comment s'appelle une fonction qui retourne un Boolean dans un `filter` ?
3. Peut-on chaîner `filter` et `map` ?

---

# 📝 Résumé du Jour

- `map` transforme un à un.
- `filter` trie selon une condition.
- `flatMap` évite les collections imbriquées non désirées.
- Ces trois fonctions permettent d'écrire des pipelines de traitement complexes et lisibles sans boucles explicites.

**Prochaine étape** : Créer un pipeline de transformation dans le TP 12 !
