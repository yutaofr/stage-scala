---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 12"
footer: "Jour 2 — Qu'est-ce qu'une Monade ?"
---

# Monades
## La structure de contrôle universelle

**Durée :** ~2h | **Fil Rouge :** Le Logger Monadique d'ATH

---

# 📋 Objectifs du Jour

- Démystifier le mot "Monade".
- Comprendre le lien entre `flatMap`, `pure` et l'enchaînement d'actions.
- Découvrir pourquoi les monades sont partout en Scala.
- Créer une mini-monade `Logger` pour tracer le clearing sans effets de bord.

---

# 1. Définition Simplifiée

Une Monade est un Functor avec deux capacités supplémentaires :
1. **pure (ou unit)** : Mettre une valeur simple dans la boîte.
   *Ex: `Option(42)`, `List(1)`, `Right(x)`.*
2. **flatMap (ou bind)** : Chaîner deux boîtes ensemble.
   *Ex: Si j'ai `Option[A]` et une fonction `A => Option[B]`, j'obtiens `Option[B]`.*

---

# 2. Le rôle du flatMap

`flatMap` est l'aiguillage du Railway Oriented Programming (S10). Il permet de gérer la suite du programme en fonction d'un contexte (échec, pluralité, asynchronisme).

```scala
trait Monad[F[_]] extends Functor[F]:
  def pure[A](a: A): F[A]
  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    // On peut DÉRIVER map à partir de flatMap + pure !
    def map[B](f: A => B): F[B] = flatMap(a => pure(f(a)))
```

> 💡 **Clé** : Toute Monade est automatiquement un Functor, car `map` peut être défini en termes de `flatMap` et `pure`. C'est l'une des preuves mathématiques les plus élégantes de la FP. Tu n'as besoin d'implémenter que `pure` et `flatMap`, et tu "gagnes" `map` gratuitement !

---

# 3. Une Monade pour le Logging

Comment loguer proprement sans `println` (side effect) ?
On peut emballer la valeur et ses logs dans une structure `Writer` (ou `Logger`).

```scala
case class MonadicLogger[A](value: A, logs: List[String])

// flatMap permet d'accumuler les logs au fur et à mesure 
// que l'on transforme la valeur.
```

> ⚠️ **Piège de nommage** : Si ton projet contient déjà un trait ou objet `Logger` (par ex. dans un package `utils`), le compilateur peut confondre les deux. Choisis un nom explicite et unique comme `MonadicLogger` pour éviter les collisions de noms.

---

# 4. Gestion des Noms et Imports

En Scala 3, quand tu multiplies les abstractions (Type Classes, extensions, given), la gestion des imports devient critique.

### Règle pratique
```scala
// Importer les instances "given" d'un objet :
import MonadicLogger.given

// Importer les extensions (méthodes ajoutées) :
import Extensions.*

// Tu ne peux PAS écrire : import Extensions.given
// si les extensions ne sont pas des "given" mais des "extension methods"
```

> 🎯 Distinguer `import Obj.*` (tout le contenu) et `import Obj.given` (uniquement les instances implicites). En cas de doute, `import Obj.*` fonctionne toujours.

---

# 🏗️ Application : Traçabilité Pure

Nous allons transformer le pipeline de clearing pour qu'il renvoie non seulement le résultat, mais aussi tout l'historique de ce qu'il a fait, de manière pure et immuable.

---

# 🧠 Quiz Rapide

1. Est-ce qu'une Monade est toujours un Functor ? (Oui, car `map` se dérive de `flatMap` + `pure`).
2. Quelle méthode de la monade permet de sortir de la "pyramide de callbacks" ? (`flatMap`).
3. Quelle est la méthode "pure" de la classe `Option` ? (`Some.apply` ou `Option.apply`).
4. **Nouveau** : Pourquoi faut-il faire attention aux noms de classes comme `Logger` ? (Risque de collision avec d'autres `Logger` dans le projet).

---

# 📝 Résumé du Jour

- Une monade permet de chaîner des calculs dans un contexte donné.
- Elle offre un moyen de gérer des effets (erreurs, logs, asynchronicité) sans briser la pureté.
- C'est l'outil de composition ultime de la programmation fonctionnelle.
- `map` est un "cadeau gratuit" quand on implémente `flatMap` + `pure`.
- ⚠️ Attention aux collisions de noms dans les gros projets : privilégier des noms explicites.
- Tu maîtrises maintenant le concept le plus célèbre (et craint) de la FP.

**Prochaine étape** : Créer ta monade Logger dans le TP 57 !
