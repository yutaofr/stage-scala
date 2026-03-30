---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 2"
footer: "Jour 1 — Expressions Avancées & Tuples"
---

# Expressions Avancées & Tuples
## Manipuler des structures de données simples

**Durée :** ~2h | **Fil Rouge :** Enrichir les données de clearing

---

# 📋 Objectifs du Jour

- Regrouper des données hétérogènes avec les **Tuples**.
- Comprendre la déstructuration de données.
- Maîtriser les **For-Comprehensions** complexes (combinaisons & filtres).
- Découvrir la portée (scope) des blocs d'expressions.

---

# 1. Les Tuples

### Regrouper sans définir de classe
Un tuple permet de lier plusieurs valeurs de types différents dans une seule variable.

```scala
val transaction = ("ATH", "CIH", BigDecimal("5000.00"))

// Accès par position (1-indexé)
val source = transaction._1  // "ATH"
val montant = transaction._3 // 5000.00
```

---

# 🔓 Déstructuration

C'est la manière la plus propre et la plus lisible d'extraire les données d'un tuple.

```scala
val (banqueSource, banqueDest, montant) = transaction

println(s"De $banqueSource vers $banqueDest : $montant DH")
```

> 💡 On utilise des noms explicites au lieu de `_1`, `_2`.

---

# ⚠️ Limites des Tuples

- **Anonymat** : On ne sait pas ce que représente `_1` sans regarder la définition.
- **Maintenance** : Si on ajoute un champ au milieu, tout le code de déstructuration casse.
- **Usage recommandé** : Uniquement pour des regroupements temporaires et locaux.

---

# 2. For-Comprehensions Avancées

C'est un outil puissant pour combiner plusieurs sources de données.

```scala
val banques = List("ATH", "CIH")
val devises = List("MAD", "EUR")

val paires = for
  b <- banques
  d <- devises
yield (b, d)

// Résultat : List((ATH,MAD), (ATH,EUR), (CIH,MAD), (CIH,EUR))
```

---

# 🔍 Filtrage Intégré (Gardes)

On peut filtrer directement dans le `for` pour ne traiter que ce qui nous intéresse.

```scala
val montants = List(100, -50, 300, -200)

val creditsValides = for
  a <- montants
  if a > 0        // Filtre 1
  if a > 100      // Filtre 2
yield a

// Résultat : List(300)
```

---

# 3. Blocs d'Expressions & Scope

Un bloc `{ }` n'est pas juste un conteneur de code, c'est une **expression**.

```scala
val resultat = {
  val x = 10
  val y = 20
  x + y  // Dernière valeur = retour du bloc
}
```

> 💡 Les variables `x` et `y` n'existent QUE dans le bloc. Elles sont invisibles à l'extérieur.

---

# 🏗️ Application au Clearing

On peut isoler des calculs complexes dans un bloc pour garder le code principal propre.

```scala
val positionNet = {
  val totalCredits = listTx.filter(_ > 0).sum
  val totalDebits  = listTx.filter(_ < 0).sum
  totalCredits + totalDebits
}
```

---

# 🧠 Quiz Rapide

1. Comment accéder au deuxième élément d'un tuple `t` de manière positionnelle ?
2. Quelle est la syntaxe pour déstructurer un tuple de 3 éléments ?
3. Est-ce qu'une variable définie à l'intérieur d'un bloc `{}` est accessible après le bloc ?

---

# 📝 Résumé du Jour

- Les tuples regroupent des données rapidement mais sans nom.
- La déstructuration améliore la lisibilité.
- Les for-comprehensions peuvent combiner des listes et filtrer des données.
- Les blocs permettent d'isoler des calculs et de limiter la portée des variables.

**Prochaine étape** : Manipuler des tuples complexes dans le TP 6 !
