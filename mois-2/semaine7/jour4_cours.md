---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 7"
footer: "Jour 4 — Performances & Big Data"
---

# Performance & Scale
## Traiter des millions de transactions sans faiblir

**Durée :** ~2h | **Fil Rouge :** Stress-test du moteur de clearing

---

# 📋 Objectifs du Jour

- Comprendre le coût algorithmique des opérations de collection.
- Maîtriser l'usage des `Parallel Collections` (aperçu).
- Optimiser les pipelines pour éviter les itérations inutiles.
- Appréhender le traitement de flux (Streaming vs Batch).

---

# 1. Complexité temporelle (Rappel)

Toutes les collections ne se valent pas pour les gros volumes.

| Opération | List | Vector |
|---|---|---|
| Ajouter au début (`::`) | **O(1)** | O(log32 N) |
| Ajouter à la fin (`:+`) | O(N) ❌ | **O(log32 N)** ✅ |
| Accès par index (`apply`) | O(N) ❌ | **O(log32 N)** ✅ |
| Parcours (`foreach`) | O(N) | O(N) |

> 💡 Pour le Clearing Engine, utilisez **Vector** pour stocker les transactions finales.

---

# 2. Le Pipeline Efficace

Évitez les étapes qui recréent des collections entières sans raison.

### ❌ Inefficace (3 copies)
```scala
list.filter(...).sortBy(...).map(...) 
```

### ✅ Efficace
Utilisez `.view` pour fusionner les opérations, ou faites les transformations complexes dans un seul `foldLeft`.

```scala
list.view.filter(...).map(...).toVector
```

---

# 3. Parallélisation Sûre

Si vous avez 8 cœurs, pourquoi n'en utiliser qu'un ?

```scala
import scala.collection.parallel.CollectionConverters._

val result = txs.par.map(complexValidation)
```

- Facile à mettre en place.
- **Attention** : À n'utiliser que si la fonction de transformation est lourde en calcul (CPU-bound) et sans effets de bord.

---

# 🏗️ Application : 1 Million de Lignes

Nous allons simuler le volume quotidien d'un pays.
- Génération massive.
- Parsing optimisé (pas de regex complexes si possible).
- Netting via un seul passage (`fold`).

---

# 🧠 Quiz Rapide

1. Quelle collection choisir pour une liste de transactions de 5 millions d'éléments consultée aléatoirement ?
2. Quel est l'effet de `.par` sur une collection ?
3. Pourquoi un `foldLeft` est-il souvent plus rapide qu'une suite de `filter.map.sum` ?

---

# 📝 Résumé du Jour

- Le choix de la structure de données (`Vector` vs `List`) change tout à grande échelle.
- `.view` est ton allié pour la mémoire.
- La parallélisation est puissante mais doit être utilisée avec discernement.
- Ton moteur est maintenant prêt à passer en production "Industrielle".

**Prochaine étape** : Benchmark de ton moteur dans le TP 34 !
