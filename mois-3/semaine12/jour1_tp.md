# TP Jour 1 : Voyage au pays des Functors

**Durée :** ~4h | **Fil Rouge :** Abstraire la transformation de données

---

## Exercice 1 : Preuve par l'exemple (1h)

1. Prends une `List(1, 2, 3)`.
2. Applique la loi d'Identité : `list.map(identity) == list`.
3. Applique la loi de Composition :
   `list.map(f).map(g) == list.map(f andThen g)`.
4. Fais de même pour un `Option(42)`.

> ⚠️ Attention : utilise `Option(42)` et non `Some(42)` sinon le type inféré sera `Some[Int]` et ton instance `Functor[Option]` ne sera pas trouvée par le compilateur.

---

## Exercice 2 : La Type Class Functor (1h30)

1. Définis le trait `Functor[F[_]]` avec `map` en **extension method** (syntaxe Scala 3).
2. Crée une instance `given` pour `List`.
3. Crée une instance `given` pour `Option`.
4. Écris une fonction générique `transform[F[_], A, B](fa: F[A], f: A => B)(using Functor[F]): F[B]`.
5. Utilise-la pour transformer indifféremment une Liste ou une Option.

---

## Exercice 3 : Mon propre Functor (1h30)

1. Crée une case class `Box[A](value: A)`.
2. Implémente l'instance de `Functor` pour `Box`.
3. Vérifie que tu peux maintenant faire `box.map(_ + 1)` (via l'extension method définie dans le trait `Functor`).

> 💡 Tu n'as pas besoin d'ajouter une extension method supplémentaire si ton trait `Functor` utilise déjà la syntaxe `extension [A](fa: F[A]) def map...`.

**Livrable** : Code source démontrant la compréhension des Functors et leur implémentation via Type Classes.
