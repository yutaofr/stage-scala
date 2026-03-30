---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 1"
footer: "Jour 4 — Fonctions & Méthodes"
---

# Fonctions & Méthodes
## Les briques de base du moteur de compensation

**Durée :** ~2h | **Fil Rouge :** Calcul de frais et validation

---

# 📋 Objectifs du Jour

- Définir des fonctions avec la syntaxe Scala 3.
- Utiliser les paramètres par défaut et nommés.
- Comprendre la différence entre méthodes et lambdas.
- Introduction à la récursivité (FP style).

---

# 1. Définition de Fonctions

### Syntaxe de Base
```scala
def nom(p1: Type1, p2: Type2): TypeRetour =
  // corps (dernière expression = retour)
  expression
```

### Exemple Bancaire : Calcul de Frais
```scala
def calculerFrais(montant: BigDecimal, taux: BigDecimal): BigDecimal =
  montant * taux
```

---

# ⚙️ Paramètres par Défaut & Nommés

### Valeur par défaut
```scala
def valider(montant: BigDecimal, seuil: BigDecimal = 1000): Boolean =
  montant < seuil
```

### Appel Nommé (Clarté)
```scala
valider(1500, seuil = 5000)
```

---

# 🔘 Fonctions sur une ligne

Si le corps est court, on peut l'écrire directement :

```scala
def estPositif(m: BigDecimal): Boolean = m > 0
```

### Pourquoi pas de `return` ?
En Scala, on privilégie le style expression. La dernière valeur du bloc est automatiquement retournée.

---

# 🔄 Introduction à la Récursivité

En FP, on remplace souvent les boucles par des appels récursifs pour traiter des listes.

```scala
def sommer(montants: List[Int]): Int =
  montants match
    case Nil          => 0
    case head :: tail => head + sommer(tail)
```

> 💡 On décompose : la tête (`head`) + le reste (`tail`).

---

# 🛡️ Tail Recursion (Optimisation)

Pour éviter de saturer la pile (StackOverflow), on utilise `@tailrec`.

```scala
import scala.annotation.tailrec

@tailrec
def loop(remaining: List[Int], acc: Int): Int =
  remaining match
    case Nil          => acc
    case head :: tail => loop(tail, acc + head)
```

> [!TIP]
> `@tailrec` force le compilateur à optimiser la récursion en une boucle efficace.

---

# ⚡ Fonctions Anonymes (Lambdas)

Idéal pour les opérations courtes à la volée.

```scala
// Version explicite
val double = (x: Int) => x * 2

// Version courte (Placeholder _)
val double: Int => Int = _ * 2
```

---

# 🧠 Quiz Rapide

1. Comment Scala sait quelle valeur une fonction doit retourner si on n'utilise pas `return` ?
2. À quoi sert l'annotation `@tailrec` ?
3. Syntaxe : Comment définir un paramètre optionnel ?

---

# 📝 Résumé du Jour

- `def` définit une méthode réutilisable.
- On évite `return` pour rester dans le style expression.
- La récursion est l'alternative fonctionnelle aux boucles.
- Les lambdas (`=>`) permettent de manipuler des fonctions comme des données.

**Prochaine étape** : Créer tes premières fonctions métier dans le TP 4 !
