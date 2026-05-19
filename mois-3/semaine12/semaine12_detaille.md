# Semaine 12 : Monades & Tests de Propriétés (5 jours)

## Jour 1 — Qu'est-ce qu'un Functor ?
**Cours (2h)** : Un Functor est "un conteneur sur lequel on peut appliquer `map`". `List`, `Option`, `Either` sont des Functors. Les lois du Functor : identité et composition. Pas de maths abstraites — juste des exemples concrets.
**TP (4h)** : Prouver par tests que `List`, `Option`, `Either` respectent les lois du Functor. Écrire un `map` générique.

## Jour 2 — Qu'est-ce qu'une Monade ?
**Cours (2h)** : Une Monade ajoute `flatMap` (et `pure`/`unit`) au Functor. Le "M-word" démystifié : c'est juste un pattern de chainage. Exemples concrets : `Option.flatMap` (chaîner des recherches), `Either.flatMap` (chaîner des validations).
**TP (4h)** : Créer une mini-monade `Logger[A]` qui accumule des traces tout en calculant. Le `flatMap` combine les traces et les valeurs. Utiliser cette monade pour tracer les étapes du clearing.

## Jour 3 — For-Comprehension = Sucre Monadique
**Cours (2h)** : Traduction de la for-comprehension : `for { a <- ma; b <- mb } yield f(a, b)` → `ma.flatMap(a => mb.map(b => f(a, b)))`. Pourquoi c'est visuellement "impératif" mais réellement "monadique".
**TP (4h)** : Réécrire les pipelines complexes de validation en for-yield propres et lisibles. Comparer la version `flatMap` imbriquée avec la version for-yield.

## Jour 4 — Property-Based Testing (ScalaCheck)
**Cours (2h)** : Sortir des tests d'exemples ("pour cette entrée, je m'attends à cette sortie"). Tester des propriétés universelles ("pour n'importe quel batch de transactions, la somme des positions nettes est 0"). ScalaCheck : générateurs et propriétés.
**TP (4h)** : Configurer ScalaCheck. Écrire des générateurs de `Transaction` aléatoires mais valides. Tester la propriété de conservation du clearing : `∀ batch, sum(netPositions) = 0`.

## Jour 5 — Clearing Engine v2.3 & Démo Mois 3
**Cours (2h)** : Bilan Mois 3 : Le code est pur, typé, polymorphe et testé par propriétés. C'est le "coeur indestructible" du système.
**TP (4h)** : Le moteur v2.3 est démontré avec 10 000 scénarios aléatoires générés par ScalaCheck. Aucun crash, aucune incohérence. **Démonstration au tuteur : "mon moteur est mathématiquement correct".**
