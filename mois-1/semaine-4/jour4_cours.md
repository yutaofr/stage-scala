---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 4"
footer: "Jour 4 — Récursivité Avancée & @tailrec"
---

# Récursivité Avancée
## Remplacer les boucles while par l'élégance récursive

**Durée :** ~2h | **Fil Rouge :** Algorithmes de recherche et d'accumulation

---

# 📋 Objectifs du Jour

- Maîtriser la **Récursion Terminale** (Tail Recursion).
- Utiliser l'optimisation `@tailrec`.
- Comprendre quand la récursion est préférable aux fonctions de collections.
- Manipuler des structures auto-référencées (arbres - aperçu).

---

# 1. Le problème de la Récursion Simple

Chaque appel de fonction consomme un espace dans la **pile** (Stack).

```scala
def factorielle(n: Int): Int =
  if n <= 1 then 1
  else n * factorielle(n - 1) // On doit attendre le retour pour multiplier
```

### Le Risque : StackOverflow
Si `n` est très grand (ex: 100 000), le programme crashe car la pile est pleine.

---

# 2. La Récursion Terminale (Tail Call)

Une fonction est "tail-recursive" si l'appel récursif est la **toute dernière opération**.

```scala
def factorielleSafe(n: Int): Int =
  def loop(count: Int, acc: Int): Int =
    if count <= 1 then acc
    else loop(count - 1, acc * count) // Appel pur à la fin ✅

  loop(n, 1)
```

> 💡 Le compilateur transforme cela en une boucle `while` ultra-efficace sous le capot.

---

# 🛡️ L'annotation `@tailrec`

Elle demande au compilateur de vérifier que la fonction est bien optimisable.

```scala
import scala.annotation.tailrec

@tailrec
def findFirst(list: List[Int], target: Int): Boolean = ...
```

- Si ce n'est pas "tail-recursive", le code **ne compile pas**.
- C'est une sécurité indispensable en production.

---

# 3. Récursion vs Fold

Parfois, un `fold` est plus simple. Mais la récursion permet l'**arrêt précoce**.

```scala
// On s'arrête dès qu'on trouve, on ne parcourt pas toute la liste
@tailrec
def aUnVirementDouteux(txs: List[Transaction]): Boolean =
  txs match
    case Nil => false
    case head :: _ if head.amount > 1000000 => true
    case _ :: tail => aUnVirementDouteux(tail)
```

---

# 🧠 Quiz Rapide

1. Quel est l'avantage majeur de la récursion terminale ?
2. Que se passe-t-il si j'ajoute `@tailrec` sur une fonction qui n'est pas terminale ?
3. Pourquoi passer un accumulateur (`acc`) dans les paramètres ?

---

# 📝 Résumé du Jour

- La récursion est l'outil naturel de la FP pour itérer.
- `@tailrec` est ton bouclier contre les crashs de mémoire (StackOverflow).
- On utilise souvent une fonction interne (`loop`) avec un accumulateur.
- La récursion offre un contrôle plus fin qu'un `map` ou un `fold` (arrêt précoce).

**Prochaine étape** : Implémenter des relevés bancaires récursifs dans le TP 18 !
