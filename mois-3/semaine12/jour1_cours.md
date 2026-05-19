---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 12"
footer: "Jour 1 — Qu'est-ce qu'un Functor ?"
---

# Functors
## Le concept mathématique derrière `.map`

**Durée :** ~2h | **Fil Rouge :** Transformer les flux sans peine

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Functor** (issu de la Théorie des Catégories).
- Identifier le comportement commun entre `List`, `Option`, et `Either`.
- Apprendre que `map` est une loi mathématique universelle.
- Savoir quand un type est (ou n'est pas) un Functor.

---

# 1. La Métaphore de la Boîte

Un Functor est une structure (une "boîte") qui contient une ou plusieurs valeurs, et qui permet d'appliquer une fonction sur ces valeurs **tout en restant dans la boîte**.

### En Scala
Si j'ai un `F[A]` et une fonction `A => B`, le Functor me permet d'obtenir un `F[B]`.

```scala
trait Functor[F[_]]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]
```

> 💡 On utilise les **extension methods** de Scala 3 pour définir `map` directement dans le trait. Ça permet d'écrire `fa.map(f)` au lieu de `Functor.map(fa)(f)`.

---

# 2. Les Lois des Functors

Pour être un vrai Functor, deux règles doivent être respectées :
1. **Identité** : `fa.map(a => a) == fa`.
2. **Composition** : `fa.map(f andThen g) == fa.map(f).map(g)`.

> 💡 Ces lois garantissent que `map` transforme les données mais ne modifie pas la "structure" de la boîte (ex: une liste de 3 éléments aura toujours 3 éléments après un map).

---

# 3. Application au Domaine

`List`, `Option`, `Either`, `Future`... Toutes ces structures sont des Functors.
Elles partagent la même interface de transformation.

```scala
val txs: List[Transaction] = ...
val ids = txs.map(_.id) // Liste restée Liste ✅

val opt: Option[Transaction] = ...
val optId = opt.map(_.id) // Option restée Option ✅
```

---

# 4. Instances `given` et Inférence de Type

### Piège classique : `Some` vs `Option`
Quand tu crées un `Some(42)` dans un test, le compilateur infère le type `Some[Int]`, qui n'est **pas** `Option[Int]`. Si ton instance `Functor[Option]` attend un `Option`, le compilateur ne la trouve pas.

```scala
// ❌ Ne compile pas : il cherche Functor[Some], pas Functor[Option]
val result = Some(42).map(_ + 1)

// ✅ Force le type parent
val result = (Option(42)).map(_ + 1)
```

> 🎯 Règle d'or : dans tes tests, utilise **`Option(...)`** au lieu de `Some(...)` pour que l'inférence de type résout correctement l'instance `given`.

---

# 🏗️ La Type Class Functor

Nous allons implémenter notre propre Type Class Functor pour des types personnalisés et observer comment `map` devient une abstraction puissante.

---

# 🧠 Quiz Rapide

1. Une fonction `Int => String` est-elle un Functor ? (Non, c'est une fonction).
2. Si je map une liste vide, qu'est-ce que j'obtiens ? (Une liste vide du nouveau type).
3. Pourquoi est-il important de respecter les "Lois" ? (Pour rendre le code prédictible et optimisable par le compilateur).
4. **Nouveau** : Pourquoi `Some(42).map(...)` peut échouer avec un Functor custom ? (Parce que le type inféré est `Some`, pas `Option`).

---

# 📝 Résumé du Jour

- Un Functor est tout ce qui possède une méthode `map` cohérente.
- Cela permet de traiter des conteneurs de manière uniforme.
- Ton cerveau commence à voir des motifs (patterns) là où il voyait du code spécifique.
- Tu comprends enfin la "magie" derrière les collections Scala.
- ⚠️ Attention à l'inférence de type : préférer `Option(x)` à `Some(x)` dans les contextes génériques.

**Prochaine étape** : Manipuler les Functors dans le TP 56 !
