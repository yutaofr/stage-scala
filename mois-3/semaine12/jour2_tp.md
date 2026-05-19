# TP Jour 2 : Ma Première Monade (MonadicLogger)

**Durée :** ~4h | **Fil Rouge :** Tracer le clearing de manière pure

---

## Exercice 1 : La structure MonadicLogger (1h)

1. Crée une case class `MonadicLogger[A](value: A, logs: List[String])`.
2. Implémente une méthode `map` qui transforme la valeur mais conserve les logs.
3. Implémente une méthode `flatMap` qui transforme la valeur ET fusionne les nouveaux logs avec les anciens.

> ⚠️ **Nommage** : N'appelle pas ta classe simplement `Logger` si ton projet contient déjà un `Logger` (utils, framework Spring, etc.). Utilise `MonadicLogger` pour éviter toute ambiguïté de résolution.

> 💡 **Astuce** : Tu peux implémenter `map` en termes de `flatMap` et `pure` au lieu de l'écrire manuellement. C'est la preuve que toute Monade est un Functor.

---

## Exercice 2 : Utilisation dans le Pipeline (1h30)

1. Crée une fonction `logParse(line: String): MonadicLogger[Transaction]`.
2. Crée une fonction `logValidate(tx: Transaction): MonadicLogger[Transaction]`.
3. Chaîne-les en utilisant `flatMap`.
4. Vérifie qu'à la fin, tu as bien l'objet `Transaction` final et la liste des deux logs ("Parsing OK", "Validation OK").

---

## Exercice 3 : La Type Class Monad (1h30)

1. Définis le trait `Monad[F[_]] extends Functor[F]`.
2. Implémente l'instance `given Monad[MonadicLogger]` dans le **companion object** de `MonadicLogger`.
3. Écris une fonction générique `chain[F[_], A, B, C](fa: F[A], f1: A => F[B], f2: B => F[C])(using Monad[F]): F[C]`.
4. Utilise-la pour faire tourner ton petit "Logger d'orchestration".

> 💡 Placer le `given` dans le companion object permet la résolution automatique via `import MonadicLogger.given`.

**Livrable** : Implémentation complète de la monade MonadicLogger et démonstration de son usage pour accumuler des logs sans effets de bord.
