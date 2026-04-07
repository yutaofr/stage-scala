---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 3"
footer: "Jour 4 — Chainage de Collections & Lazy"
---

# Chainage & Performance
## Optimiser les pipelines de traitement

**Durée :** ~2h | **Fil Rouge :** Traitement de gros volumes de transactions

---

# 📋 Objectifs du Jour

- Apprendre à chaîner proprement les opérations de collection.
- Comprendre le coût mémoire des copies de collections.
- Découvrir les collections "Lazy" (paresseuses) pour les gros volumes.
- Apprendre à choisir entre `List` et `Vector`.

---

# 1. Chainage Fluide

L'un des grands plaisirs de Scala est de lire le code comme une phrase.

```scala
val rapport = transactions
  .filter(_.amount > 100)
  .map(_.amount * 1.20)
  .sortBy(identity)
  .reverse
```

### Le problème caché
À chaque étape (`filter`, `map`, `sortBy`), Scala crée une **nouvelle copie** de la liste en mémoire.

---

# 2. Lazy Collections : `LazyList` & `View`

Si on a 1 million de transactions, créer 4 copies est inefficace.

```scala
val grosPipeline = transactions
  .view                    // On passe en mode paresseux
  .filter(_.amount > 100)
  .map(_.amount * 1.20)
  .toList                  // On n'exécute Tout qu'ici
```

### Pourquoi `view` ?
- Pas de collection intermédiaire.
- Un seul passage sur les données.
- Idéal pour les pipelines longs sur de grosses données.

---

# 3. List vs Vector

| Caractéristique | List | Vector |
|---|---|---|
| Structure | Liste chaînée | Arbre compact (32-wide) |
| Accès à la tête | **O(1)** (Très rapide) | O(log32 N) (Rapide) |
| Accès par index | O(N) (Lent) | **O(log32 N)** (Rapide) |
| Usage type | Récursion, petits flux | Gros volumes, accès aléatoire |

---

# 🏗️ Performance : Le Piège à Éviter

```scala
// ❌ TRÈS LENT (O(N^2))
var list = List()
for (i <- 1 to 10000) list = list :+ i // :+ (append) est lent sur List

// ✅ TRÈS RAPIDE (O(N))
var list = List()
for (i <- 1 to 10000) list = i :: list // :: (prepend) est rapide sur List
list.reverse // optionnel
```

---

# 🧠 Quiz Rapide

1. Que se passe-t-il si je chaîne 3 `map` sur une `List` classique ?
2. Quel est l'effet de la méthode `.view` ?
3. Quelle collection privilégier pour l'accès par index (ex: `list(500)`) ?

---

# 📝 Résumé du Jour

- Le chaînage rend le code expressif.
- `.view` permet d'éviter les copies intermédiaires coûteuses.
- Choisissez la bonne structure : `List` pour la récursion, `Vector` pour les gros accès indexés.
- Attention aux opérations en O(N) sur les listes (append).

**Prochaine étape** : Stress-test de ton moteur dans le TP 14 !
