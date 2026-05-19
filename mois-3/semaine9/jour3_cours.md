---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 9"
footer: "Jour 3 — Composition (andThen, compose)"
---

# Composition de Fonctions
## Construire des cathédrales avec des briques simples

**Durée :** ~2h | **Fil Rouge :** Un pipeline de clearing modulaire

---

# 📋 Objectifs du Jour

- Comprendre le concept de composition de fonctions.
- Utiliser les opérateurs `andThen` et `compose`.
- Apprendre à briser une logique complexe en micro-fonctions.
- Créer un pipeline de traitement fluide et lisible.

---

# 1. Qu'est-ce que la Composition ?

En FP, on ne crée pas de gros blocs de code. On crée de petites fonctions que l'on assemble comme des LEGOs.

### La métaphore mathématique
Si on a $f(x)$ et $g(x)$, alors $(g \circ f)(x) = g(f(x))$.

### En Scala
```scala
val f: Int => Int = _ + 1
val g: Int => Int = _ * 2

val h = f andThen g // h(x) = (x + 1) * 2
val i = f compose g // i(x) = (x * 2) + 1
```

---

# 2. `andThen` vs `compose`

- **`andThen`** : Suis l'ordre de lecture naturel (Gauche -> Droite). 
  *Ex: Faire A puis faire B.*
- **`compose`** : Suis l'ordre mathématique (Droite -> Gauche).
  *Ex: Faire B sur le résultat de A.*

> [!TIP]
> Préférez `andThen` pour la lisibilité des pipelines de données Industriels.

---

# 3. Le Pipeline de Clearing Modulaire

Transformons notre grosse fonction de traitement en une chaîne de fonctions.

```scala
val parse: String => Transaction = ...
val validate: Transaction => Transaction = ...
val anonymize: Transaction => Transaction = ...

val fullProcess = parse andThen validate andThen anonymize
```

- Chaque fonction est **indépendante**.
- On peut tester chaque brique séparément.
- On peut ré-agencer le pipeline sans tout casser.

---

# 🏗️ Application : Nettoyage de Données

Nous allons créer des micro-fonctions de nettoyage :
`trimSpaces`, `upperCaseBankCode`, `roundAmount`.
Et les composer pour préparer nos transactions.

---

# 🧠 Quiz Rapide

1. Si `f` transforme un String en Int, et `g` transforme un Int en Boolean. Quel est le type de `f andThen g` ? (`String => Boolean`).
2. Pourquoi la composition est-elle plus facile avec des fonctions pures ? (Pas d'états cachés qui pourraient interférer entre les briques).
3. `andThen` est-il disponible sur les méthodes (`def`) directement ? (Non, il faut d'abord transformer la méthode en fonction via `val f = method _`).

---

# 📝 Résumé du Jour

- La composition est l'âme de la programmation fonctionnelle.
- Briser la complexité en petites fonctions pures est la clé de la maintenabilité.
- `andThen` crée des pipelines fluides et explicites.
- Ton moteur devient un assemblage de services spécialisés et interchangeables.

**Prochaine étape** : Créer ton premier pipeline composé dans le TP 43 !
