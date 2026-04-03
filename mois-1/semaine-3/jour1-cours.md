---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 3"
footer: "Jour 1 — List, Map, Set en Profondeur"
---

# Collections en Profondeur
## List, Map & Set : Les piliers du stockage fonctionnel

**Durée :** ~2h | **Fil Rouge :** Registre des banques et indexation des flux

---

# 📋 Objectifs du Jour

- Comprendre la hiérarchie des collections Scala.
- Maîtriser la manipulation des **List** (immutables).
- Utiliser les **Map** pour l'accès rapide par clé.
- Gérer l'unicité avec les **Set**.
- Découvrir l'immuabilité par défaut.

---

# 1. Hiérarchie des Collections

Scala propose deux packages : `immutable` (par défaut) et `mutable`.

### Pourquoi l'immuabilité ?
- Sécurité en multi-threading.
- Partage efficace de la mémoire (structural sharing).
- Code plus facile à raisonner : "Une collection ne change jamais".

---

# 📜 La Liste Scala (`List`)

Une liste chaînée immutable optimisée pour l'accès à la tête (`head`).

```scala
val banques = List("ATH", "CIH", "BOA")

// Ajouter au début (cons) - Très rapide O(1)
val newBanques = "SGMB" :: banques 

// Concaténer deux listes
val all = banques ++ List("BMCE")
```

> ⚠️ Accéder au dernier élément ou à l'index N est lent sur une `List` (O(N)).

---

# 🗺️ Le Map (Dictionnaire)

Idéal pour stocker des paires Clé -> Valeur.

```scala
val codes = Map("001" -> "ATH", "002" -> "CIH")

// Accéder en sécurité
val name = codes.getOrElse("003", "Banque Inconnue")

// Ajouter un élément (crée une nouvelle Map)
val updated = codes + ("003" -> "BOA")
```

---

# 🛡️ Le Set (Ensemble)

Garantit l'unicité des éléments.

```scala
val ibans = Set("MA123", "MA456", "MA123")
// Résultat : Set("MA123", "MA456") - L'un des "MA123" est ignoré

ibans.contains("MA123") // true
```

---

# 🏗️ Indexation avec `groupBy`

Une des méthodes les plus puissantes pour organiser des transactions.

```scala
val txs = List(("ATH", 100), ("CIH", 200), ("ATH", 50))

val parBanque = txs.groupBy(tuple => tuple._1)
// Résultat : Map("ATH" -> List(("ATH",100), ("ATH",50)), "CIH" -> List(("CIH",200)))
```

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre `List` et `Set` ?
2. Comment s'appelle l'opération `::` ?
3. Pourquoi privilégie-t-on les collections immutables ?

---

# 📝 Résumé du Jour

- Les collections Scala sont immutables par défaut.
- `List` est parfaite pour les flux séquentiels.
- `Map` permet de créer des indexations rapides.
- `groupBy` est le couteau suisse pour organiser les données interbancaires.

**Prochaine étape** : Créer le registre des banques dans le TP 11 !
