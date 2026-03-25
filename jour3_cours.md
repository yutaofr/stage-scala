---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 1"
footer: "Jour 3 — Structures de Contrôle & Expressions"
---

# Structures de Contrôle & Expressions
## La logique de décision en Scala

**Durée :** ~2h | **Fil Rouge :** Validation d'un virement

---

# 📋 Objectifs du Jour

- Comprendre pourquoi tout est **expression** en Scala.
- Maîtriser le **Pattern Matching** (Super-Switch).
- Apprendre à transformer des données avec **For-Yield**.
- Différencier les instructions des expressions.

---

# 1. Tout est Expression

### La Règle d'Or
En Scala, presque tout **retourne une valeur**. Il n'est pas nécessaire d'utiliser `return`.

### Comparaison Java vs Scala
```java
// Java : Instruction (Statement)
String status;
if (amount > 0) status = "OK"; else status = "KO";
```

```scala
// Scala : Expression (Retourne une valeur)
val status = if amount > 0 then "OK" else "KO"
```

---

# 🔘 Le Type `Unit`

Quand une expression ne retourne "rien" d'utile (ex: un print), elle retourne `Unit`.

```scala
val result: Unit = println("Log envoyé")
```

- Équivalant au `void` de Java.
- Une valeur unique possible : `()`.

---

# 2. Pattern Matching Introductif

Le `match` est le "Super-Switch" de Scala. Il est expressif et sécurisé.

```scala
val txType = "VIREMENT"

val label = txType match
  case "VIREMENT"     => "Interbancaire"
  case "PRELEVEMENT"  => "Automatique"
  case "CHEQUE"       => "Paiement physique"
  case other          => s"Inconnu : $other"
```

---

# 🛡️ Pattern Matching avec Gardes

On peut ajouter des conditions (`if`) directement dans les cases.

```scala
def verifier(montant: BigDecimal): String =
  montant match
    case m if m < 0      => "Montant invalide ❌"
    case m if m > 100000 => "Alerte Fraude ⚠️"
    case _               => "Transaction acceptée ✅"
```

---

# 3. Boucles & Transformations

### Le `for` pour itérer
```scala
for i <- 1 to 3 do
  println(s"Tentative #$i")
```

### Le `yield` pour transformer
Le mot-clé `yield` transforme une boucle en une nouvelle collection.

```scala
val montants = List(100, 200, 300)
val taxes = for m <- montants yield m * 0.20
// taxes = List(20.0, 40.0, 60.0)
```

---

# 🔍 For avec Filtres (Gardes)

```scala
val montants = List(50, 500, 1000)

val grosMontants = for
  m <- montants
  if m > 200
yield m

// Résultat : List(500, 1000)
```

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre une instruction et une expression ?
2. Quel est le type de retour d'un `println` ?
3. À quoi sert le mot-clé `yield` ?

---

# 📝 Résumé du Jour

- `if/else` est une expression qui renvoie une valeur.
- `match` remplace avantageusement les suites de `if/else`.
- `for-yield` est l'outil de base pour transformer des données.

**Prochaine étape** : Appliquer la logique métier dans le TP 3 !
```
