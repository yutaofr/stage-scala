---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 3"
footer: "Jour 5 — Revue & Bonnes Pratiques Collections"
---

# Revue & Bonnes Pratiques
## Écrire du code collection-centric propre et performant

**Durée :** ~2h | **Fil Rouge :** Clearing Engine v0.3 — 100% Fonctionnel

---

# 📋 Objectifs du Jour

- Synthétiser les connaissances sur les collections.
- Apprendre à éviter les pièges classiques (`head` sur vide, etc.).
- Comprendre quand utiliser quel type de collection.
- Rendre le moteur de clearing entièrement fonctionnel et pur.

---

# 1. Choisir la Bonne Collection

| Besoin | Solution |
|---|---|
| Stockage ordonné simple | **List** |
| Accès fréquent par index | **Vector** |
| Unicité des éléments | **Set** |
| Recherche par clé | **Map** |
| Très gros volumes (> 1M) | **LazyList / View** |

---

# ⚠️ Pièges Classiques & Sécurité

### L'ennemi : `.head` et `.reduce` sur liste vide
```scala
val list = List()
list.head    // 💥 CRASH : NoSuchElementException
list.reduce(_ + _) // 💥 CRASH
```

### La solution : Méthodes sécurisées
```scala
list.headOption // Option[A] (None si vide) ✅
list.foldLeft(0)(_ + _) // 0 si vide ✅
```

---

# 🏗️ Vers le "Zéro Boucle"

Le but de cette semaine était de supprimer les boucles `for/while` impératives.

### Style Impératif (À proscrire)
```scala
var total = 0
for (t <- txs) { total += t.amt }
```

### Style Fonctionnel (Recommandé)
```scala
val total = txs.map(_.amt).sum
// ou
val total = txs.foldLeft(0)(_ + _.amt)
```

---

# 📝 Checklist de Qualité S3

1. **Pureté** : Est-ce que mes fonctions transforment sans modifier l'existant ?
2. **Immuabilité** : Ai-je utilisé des collections `immutable` ?
3. **Chaînage** : Mon pipeline est-il lisible étape par étape ?
4. **Sécurité** : Ai-je géré les cas de listes vides ?

---

# 🧠 Quiz de Fin de Semaine

1. Quel outil utiliser pour grouper des transactions par devise ?
2. Pourquoi `Vector` est-il souvent préférable à `List` pour les gros datasets ?
3. Comment transformer une `List[List[Int]]` en `List[Int]` ?

---

# 📝 Résumé de la Semaine

- Tu as découvert la puissance des collections fonctionnelles.
- Tu sais filtrer, transformer et agréger des données sans boucles.
- Ton moteur de clearing est passé de la logique simple au pipeline de données.

**Prochaine étape** : Le passage aux fonctions d'ordre supérieur et au type `Option` en Semaine 4 !
