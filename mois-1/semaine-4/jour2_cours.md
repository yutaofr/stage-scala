---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 4"
footer: "Jour 2 — Le Type Option (Adieu Null !)"
---

# Le Type Option
## Éradiquer le crash "NullPointerException"

**Durée :** ~2h | **Fil Rouge :** Gestion des données manquantes dans le clearing

---

# 📋 Objectifs du Jour

- Comprendre pourquoi `null` est une erreur de conception.
- Découvrir le type `Option[A]` : `Some` vs `None`.
- Manipuler les absences de valeurs avec `getOrElse`, `map` et `flatMap`.
- Rendre le code résilient par le typage.

---

# 1. Le Drame du `null`

En Java/C++, `null` est partout. C'est la "milliarde de dollars mistake".

```java
// Java : Risque constant
User user = findUser(id);
String city = user.getAddress().getCity(); // 💥 CRASH si address est null
```

### La Philosophie Scala
On n'utilise **jamais** `null`. On utilise un conteneur qui dit explicitement : "La valeur peut être absente".

---

# 📦 Le Type `Option[A]`

C'est une boîte qui contient soit un élément (`Some`), soit rien (`None`).

```scala
val maybeAmount: Option[BigDecimal] = Some(BigDecimal("100.0"))
val noAmount: Option[BigDecimal]    = None
```

### Pourquoi c'est génial ?
Le compilateur te **force** à gérer le cas où la valeur est absente. Tu ne peux pas "oublier" de vérifier.

---

# 🛠️ Manipuler les Options

### 1. Sécurité par défaut (`getOrElse`)
```scala
val montant = maybeAmount.getOrElse(BigDecimal("0"))
```

### 2. Transformation (`map`)
```scala
// Si présent, multiplier par 1.2, sinon reste None
val avecTva = maybeAmount.map(_ * 1.2)
```

---

# 🔗 Chaînage d'Options (`flatMap`)

Que faire si on a plusieurs étapes qui peuvent échouer ?

```scala
def findBank(id: String): Option[Bank] = ...
def getBalance(b: Bank): Option[BigDecimal] = ...

// Si bank existe ET balance existe -> Some(balance)
// Sinon -> None
val balance = findBank("001").flatMap(bank => getBalance(bank))
```

---

# 🏗️ Application au Clearing

- Rechercher un IBAN qui n'existe pas.
- Lire une ligne CSV vide.
- Appeler un service de taux de change indisponible.

> 💡 Dans tous ces cas, on retourne `Option` au lieu de `null` ou de lancer une exception.

---

# 🧠 Quiz Rapide

1. Quels sont les deux sous-types de `Option` ?
2. Quelle méthode permet de fournir une valeur de secours ?
3. Pourquoi `Option` est-il préférable à un `try/catch` pour les cas métier normaux ?

---

# 📝 Résumé du Jour

- `null` est banni en Scala.
- `Option` rend l'absence de valeur explicite dans le type.
- `map` et `flatMap` permettent de manipuler les options de manière fluide.
- Un code qui utilise `Option` est un code qui ne crashe jamais au runtime.

**Prochaine étape** : Éradiquer les `null` dans le TP 16 !
