---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 10"
footer: "Jour 4 — Le Type Try"
---

# Gérer l'imprévisible : Try
## Apprivoiser les exceptions Java Legacy

**Durée :** ~2h | **Fil Rouge :** Sécuriser les appels aux bibliothèques Java

---

# 📋 Objectifs du Jour

- Comprendre le rôle du type **Try**.
- Faire le pont entre le monde "Exceptionnel" (Java) et le monde "Typé" (Scala).
- Apprendre à encapsuler des appels risqués (Parsing, Réseau).
- Transformer un `Try` en `Either` pour l'intégrer au pipeline.

---

# 1. Le Problème des Exceptions

En Java (et Scala impératif), une exception coupe tout.
`val x = Integer.parseInt(s)` <-- Si `s` est "abc", le programme s'arrête.

### Try[T] : Le conteneur sécurisé
```scala
import scala.util.Try

val res = Try(Integer.parseInt(s))
```
- `Success(value)` : Si tout s'est bien passé.
- `Failure(exception)` : Si une erreur a été levée.

---

# 2. Utilisation Monadique

Comme `Option` et `Either`, `Try` dispose de `map` et `flatMap`.

```scala
val finalValue = Try(readFromFile())
  .map(_.trim)
  .flatMap(s => Try(s.toInt))

// result: Success(10) ou Failure(IOException/NumberFormatException)
```

> [!TIP]
> `Try` est idéal pour les frontières avec l'extérieur (Fichiers, DB, APIs Java). Pour la logique métier pure, préférez `Either`.

---

# 3. Conversion Try -> Either

Pour unifier votre moteur de clearing sur un seul type (rail d'erreur), vous devrez convertir vos `Try`.

```scala
val eitherResult = myTry.toEither.left.map(ex => TechnicalError(ex))
```

- `toEither` : Transforme `Failure(ex)` en `Left(ex)`.
- `left.map` : Transforme l'exception brute en votre `ClearingError` métier.

### Le nouveau type `TechnicalError`

Il faut enrichir la hiérarchie `ClearingError` pour y ajouter un conteneur d'exception :

```scala
// Dans ClearingError.scala
case class TechnicalError(ex: Throwable) extends HighLevelError
```

> 💡 On range `TechnicalError` avec les `HighLevelError` car c'est un problème d'infrastructure (JVM, réseau), pas un problème de données métier.

---

# 🏗️ Application : HttpClient & Crypto

Rappelez-vous la S8 : les appels HTTP et le `MessageDigest` peuvent lever des exceptions.
Nous allons les "emprisonner" dans des `Try` pour qu'ils ne fassent plus jamais planter le clearing.

---

# 🧠 Quiz Rapide

1. Quelle est la différence majeure entre `Either` et `Try` ? (`Try` capture automatiquement les exceptions levées dans son bloc).
2. Que se passe-t-il si un bloc `Try` lève une `Fatal Error` (ex: OutOfMemory) ? (Certaines erreurs fatales ne sont pas capturées par `Try` par sécurité).
3. Pourquoi transformer un `Try` en `Either` ?

---

# 📝 Résumé du Jour

- `Try` est ta ceinture de sécurité face au monde extérieur.
- Il transforme les crashs en données manipulables.
- Il s'utilise comme une monade (`for-yield`, `map`).
- Ton moteur est maintenant protégé contre les erreurs systèmes imprévues.

**Prochaine étape** : Sécuriser tes appels Java dans le TP 49 !
