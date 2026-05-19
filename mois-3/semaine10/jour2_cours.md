---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 10"
footer: "Jour 2 — FlatMap sur Either (Chainage)"
---

# Le Chaînage Typé
## Composer des opérations qui peuvent échouer

**Durée :** ~2h | **Fil Rouge :** Pipeline de clearing sans "if/else"

---

# 📋 Objectifs du Jour

- Utiliser `flatMap` et `map` sur le type `Either`.
- Comprendre le concept de "Short-circuiting" (arrêt au premier échec).
- Chaîner plusieurs étapes (Parsing -> Validation -> Enrichissement).
- Remplacer les compositions `andThen` manuelles par des pipelines monadiques.

---

# 1. Map vs FlatMap sur Either

### `map`
Applique une transformation sur la valeur de **Succès** (`Right`).
L'erreur (`Left`) est ignorée et passée telle quelle.
```scala
val res: Either[Error, Int] = Right(10).map(_ * 2) // Right(20)
```

### `flatMap`
Applique une fonction qui retourne elle-même un `Either`.
C'est la clé du chaînage !
```scala
def step1: Either[String, Int] = ...
def step2(i: Int): Either[String, Int] = ...

val result = step1.flatMap(step2)
```

---

# 2. Le Pipeline Monadique d'ATH

Imagine notre moteur v2.1 :

```scala
def process(line: String) =
  parse(line)                 // Either[ParsingError, RawTx]
    .flatMap(validate)         // Either[ClearingError, ValidTx]
    .flatMap(calculateFees)    // Either[FeeError, FinalTx]
    .map(anonymize)            // Transformation pure terminale
```

- Si `parse` échoue, `validate` et `calculateFees` ne sont **jamais exécutées**.
- Le résultat final sera le `Left` du parser.
- C'est propre, sûr et extrêmement lisible.

---

# 3. For-Comprehension sur Either

Scala offre un sucre syntaxique pour rendre `flatMap` encore plus lisible.

```scala
val fullTx = for
  raw     <- parse(line)
  valid   <- validate(raw)
  withFee <- calculateFees(valid)
yield anonymize(withFee)
```

> 💡 C'est le Graal de la lisibilité en Scala. On dirait une suite d'instructions impératives, mais c'est du code purement fonctionnel et sécurisé.

---

# 🧠 Quiz Rapide

1. Que se passe-t-il si j'appelle `map` sur un `Left` ? (Rien, le `Left` est retourné tel quel).
2. Quelle est la différence entre `andThen` (S9) et `flatMap` (S10) ? (`andThen` compose des fonctions $A \to B$. `flatMap` compose des fonctions $A \to Either[L, B]$).
3. Pourquoi appelle-t-on cela du "Short-circuiting" ?

---

# 📝 Résumé du Jour

- `map` et `flatMap` sont les outils pour faire avancer les données dans le tunnel du succès.
- La `for-comprehension` rend le chaînage d'erreurs élégant.
- Ton code n'a plus besoin de structures de contrôle complexes pour gérer l'échec.
- Le flux de données est devenu un pipeline linéaire et prévisible.

**Prochaine étape** : Chaîner tout ton moteur avec flatMap dans le TP 47 !
