---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 13"
footer: "Jour 2 — Les Futures (Programmation Non-Bloquante)"
---

# Scala Futures
## La concurrence asynchrone simplifiée

**Durée :** ~2h | **Fil Rouge :** Valider 1000 transactions en parallèle

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Future[T]**.
- Apprendre à lancer des calculs sans bloquer le thread principal.
- Découvrir la puissance de `flatMap`, `map` et `zip` sur les Futures.
- Réduire le temps de traitement de notre batch de clearing.

---

# 1. Qu'est-ce qu'une Future ?

Une `Future[T]` est un conteneur qui représentera une valeur de type `T` **dans le futur**. C'est un ticket de tombola : on ne sait pas encore si on a gagné, mais on peut déjà prévoir ce qu'on fera du lot.

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val f: Future[Int] = Future {
  Thread.sleep(1000)
  42
}
```

> 💡 Le code à l'intérieur du bloc `Future { ... }` s'exécute sur un autre thread immédiatement.

---

# 2. Composition Asynchrone

Le génie de Scala est de traiter les Futures comme des collections (on peut faire du `map` et `flatMap`).

```scala
val f1 = Future { fetchRate("MAD") }
val f2 = Future { fetchRate("USD") }

val combine = for
  mad <- f1
  usd <- f2
yield mad / usd
```

- `f1` et `f2` se lancent **en même temps** (parallélisme).
- On attend les deux résultats de manière non-bloquante pour calculer le ratio.

---

# 3. Ne jamais bloquer !

Il est interdit de faire `Await.result` à l'intérieur d'un programme réactif. On laisse les Futures se chaîner jusqu'à la fin de l'application.

> [!WARNING]
> **Nuance importante** : `Await.result` est **acceptable** dans les scripts `main` de test et les TPs pour observer les résultats. En **production**, on ne bloque jamais ! On utilise des callbacks (`onComplete`) ou on laisse le framework (Akka HTTP, Play) gérer la fin de la Future.

```scala
// ❌ Production : NE PAS FAIRE
val result = Await.result(myFuture, 5.seconds)

// ✅ Production : Laisser la Future se résoudre via callback
myFuture.onComplete {
  case Success(value) => println(s"Résultat: $value")
  case Failure(ex)    => println(s"Erreur: ${ex.getMessage}")
}
```

> [!TIP]
> Si ton moteur met 10 secondes pour 1000 transactions séquentiellement, il mettra peut-être 1 seconde s'il en traite 10 en parallèle via des Futures.

---

# 🏗️ Application : Le Validateur Parallèle

Nous allons modifier notre `ClearingService` pour qu'il lance la validation de chaque transaction dans une `Future`.

`List[Transaction] => Future[List[ClearingResult]]`

---

# 🧠 Quiz Rapide

1. Une `Future` bloque-t-elle le programme pendant son exécution ? (Non).
2. Quelle méthode permet de combiner deux Futures indépendantes ? (`zip` ou `for-yield`).
3. D'où viennent les threads utilisés par les Futures ? (De l'ExecutionContext).

---

# 📝 Résumé du Jour

- Les Futures permettent de faire fructifier tes cœurs CPU.
- Elles s'utilisent comme des Monades (on applique les acquis du mois 3 !).
- Elles transforment les temps d'attente (I/O, réseau) en opportunités de calcul.
- Ton moteur de clearing devient "Asynchrone".

**Prochaine étape** : Paralléliser tes validations dans le TP 62 !
