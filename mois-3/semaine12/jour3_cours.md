---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 12"
footer: "Jour 3 — For-Comprehension & Sucre Syntaxique"
---

# For-Comprehension
## Derrière le sucre : La puissance des Monades

**Durée :** ~2h | **Fil Rouge :** Nettoyer les chainages complexes du Clearing Engine

---

# 📋 Objectifs du Jour

- Comprendre que le `for` de Scala n'est pas une boucle `for` classique.
- Découvrir la transformation du `for` en `map`, `flatMap` et `withFilter`.
- Apprendre à lire et déboguer des chaînes monadiques complexes.
- Utiliser le `for-yield` sur nos propres types (Logger, Box).

---

# 1. Le "Mensonge" du For

En Scala, quand vous écrivez :
```scala
for
  x <- listA
  y <- listB
yield x + y
```

Le compilateur le transforme réellement en :
```scala
listA.flatMap(x => listB.map(y => x + y))
```

> 💡 C'est pour cela que n'importe quelle structure possédant `map` et `flatMap` (une Monade) peut être utilisée dans un `for`.

---

# 2. Les Gardes et `withFilter`

Si vous ajoutez un `if` dans le `for`, le compilateur utilise `withFilter`.

```scala
for
  x <- list
  if x > 0
yield x
```
devient `list.withFilter(_ > 0).map(x => x)`.

---

# 3. Pourquoi c'est Vital ?

Le `for-yield` permet de garder un style de programmation "linéaire" et lisible même quand on manipule des contextes complexes (erreurs, asynchronicité).

### Sans for-yield (Pyramide de la mort)
```scala
step1.flatMap(a => step2(a).flatMap(b => step3(b).map(c => combine(a,b,c))))
```

### Avec for-yield ✅
```scala
for
  a <- step1
  b <- step2(a)
  c <- step3(b)
yield combine(a, b, c)
```

---

# 🏗️ Application : Orchestration v2.3

Nous allons réécrire tout le pipeline principal de démarrage du clearing en utilisant un seul grand `for-yield` sur notre monade `Either` ou `Logger`.

---

# 🧠 Quiz Rapide

1. Puis-je utiliser une `case class` normale dans un `for` ? (Seulement si je lui ajoute les méthodes `map` et `flatMap`).
2. Quelle méthode est appelée pour la dernière instruction `yield` ? (`map`).
3. Quelle méthode est appelée pour toutes les instructions `<-` intermédiaires ? (`flatMap`).

---

# 📝 Résumé du Jour

- Le `for-yield` est un magnifique sucre syntaxique sur les monades.
- Il transforme des chaînes de fonctions complexes en récit linéaire.
- Tant que tes types respectent l'interface monadique, ils "gagnent" le droit d'utiliser le `for`.
- Ton code de clearing devient d'une lisibilité cristalline.

**Prochaine étape** : Réécrire tes pipelines dans le TP 58 !
