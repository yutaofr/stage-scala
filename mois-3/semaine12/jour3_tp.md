# TP Jour 3 : Le Sucre des Monades

**Durée :** ~4h | **Fil Rouge :** Clarifier l'orchestration du moteur

---

## Exercice 1 : Désucrage manuel (1h30)

1. Prends le `for-yield` sur `Either` que tu as écrit en S10.
2. Réécris-le entièrement en utilisant uniquement des appels explicites à `flatMap` et `map`.
3. Vérifie que le résultat est **strictement identique** (utilise `shouldBe` dans un test).
4. Constate la différence de lisibilité.

> 💡 Rappel de la règle du compilateur :
> - Toutes les lignes `<-` intermédiaires → `flatMap`
> - La dernière ligne `yield` → `map`

---

## Exercice 2 : For sur le MonadicLogger (1h30)

1. Utilise ta monade `MonadicLogger` du Jour 2.
2. Écris un pipeline de traitement complet (Parse -> Validate -> Save) en utilisant la syntaxe `for { ... } yield ...`.
3. Vérifie que les logs s'accumulent bien "automatiquement" entre chaque étape grâce au `flatMap` qui tourne sous le capot du `for`.

> 💡 C'est ici que tu vois la puissance du pattern : le pipeline se lit comme du code impératif classique, mais **aucune variable mutable** n'est utilisée !

---

## Exercice 3 : Mixage Interdit (1h)

1. Tente de mixer un `Option` et un `Either` dans le même `for-comprehension`.
2. Observe l'erreur de compilation.
3. **Conclusion** : On ne peut pas facilement mixer des monades différentes dans un seul `for` (on verra les "Monad Transformers" plus tard si besoin).

> 💡 **Astuce** : Utilise `assertTypeError(""" code """)` de ScalaTest pour prouver dans un test que ce code ne compilera jamais. C'est une technique puissante pour documenter les contraintes du système de types.

```scala
assertTypeError("""
  for
    x <- Right(10)   // Either
    y <- Some(5)     // Option  ← INTERDIT !
  yield x + y
""")
```

**Livrable** : Code source montrant l'usage élégant du for-yield sur différentes monades (Either, MonadicLogger).
