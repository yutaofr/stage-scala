---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 10"
footer: "Jour 1 — Le Type Either[L, R]"
---

# Résilience Typée : Either
## Ne plus jamais cacher ses erreurs

**Durée :** ~2h | **Fil Rouge :** Gestion robuste des échecs de validation

---

# 📋 Objectifs du Jour

- Comprendre le concept de type disjoint : **Either**.
- En finir avec les exceptions (`throw`) qui cassent le flux.
- Apprendre à coder pour le cas de succès **ET** le cas d'erreur.
- Découvrir la convention Left (Erreur) / Right (Succès).

---

# 1. Pourquoi Either ?

Jusqu'à présent, nous utilisions `Option[Transaction]`.
- Si c'est `None`, on ne sait pas **pourquoi** ça a échoué (Montant nul ? Iban invalide ?).

### Either[L, R] apporte la réponse
- `Left` : Contient l'objet d'erreur (généralement à Gauche).
- `Right` : Contient la valeur de succès (Right is "right" ✅).

```scala
def parse(line: String): Either[ClearingError, Transaction] = ...
```

---

# 2. Syntaxe & Création

```scala
val success: Either[String, Int] = Right(42)
val failure: Either[String, Int] = Left("Une erreur est survenue")

// Utilisation typique
def divide(a: Int, b: Int): Either[String, Int] =
  if b == 0 then Left("Division par zéro")
  else Right(a / b)
```

---

# 3. Pattern Matching sur Either

C'est la manière naturelle de consommer le résultat.

```scala
divide(10, 0) match
  case Right(res) => println(s"Résultat : $res")
  case Left(err)  => println(s"Erreur : $err")
```

### Pourquoi c'est mieux que try/catch ?
- C'est **explicite** dans la signature de la fonction.
- Le compilateur nous oblige à gérer l'erreur (Exhaustivité).
- C'est de la donnée, pas une interruption brutale du CPU.

---

# 🏗️ Application au Clearing

Toutes nos fonctions qui pouvaient renvoyer `None` vont maintenant renvoyer un `Either[ClearingError, T]`.

```scala
// Moteur v1.3 : (String) => Option[Transaction]
// Moteur v2.1 : (String) => Either[ParsingError, Transaction]
```

---

# 🔄 Avant / Après : Ton Code v2.0 → v2.1

### Avant (v2.0 — Tuple Pattern) ❌
```scala
val (parsedTxs, parseErrors) = parse(lines) // Tuple (bon sac, sac poubelle)
val (validTxs, rejectedTxs) = filter(parsedTxs) // Encore un tuple...
// Problème : on trimballe 2 listes séparées à chaque étape
```

### Après (v2.1 — Either Pattern) ✅
```scala
val results: List[Either[ClearingError, Transaction]] = lines.map(parseLine)
// Chaque élément SAIT s'il est un succès ou une erreur
val (errors, transactions) = results.partitionMap(identity) // Un seul split à la fin
```

> 💡 C'est **exactement** la réponse à ta frustration de la rétro S9 !

---

# 🧠 Quiz Rapide

1. Que signifie le jeu de mot "Right is Right" ?
2. Quelle information de plus que `Option` apporte `Either` ? (La raison de l'échec).
3. Une fonction qui renvoie un `Either` peut-elle quand même lancer une exception ? (En théorie oui, mais en FP pure on évite absolument !).

---

# 📝 Résumé du Jour

- `Either` est le standard de l'industrie pour la gestion d'erreurs fonctionnelle.
- `Left` pour l'erreur, `Right` pour le succès.
- Les erreurs deviennent des citoyens de première classe de ton code.
- Plus aucune exception cachée ne viendra planter ton batch de nuit.

**Prochaine étape** : Migrer tes validateurs vers Either dans le TP 46 !
