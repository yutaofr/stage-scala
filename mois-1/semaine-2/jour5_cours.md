---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 2"
footer: "Jour 5 — Objets Compagnon & Apply"
---

# Objets Compagnon & Apply
## Design Patterns & Construction d'Objets en Scala

**Durée :** ~2h | **Fil Rouge :** Création élégante de transactions depuis le CSV

---

# 📋 Objectifs du Jour

- Comprendre le rôle des **Objects** (Singletons).
- Maîtriser le concept de **Companion Object**.
- Simplifier la création d'objets avec la méthode `apply`.
- Implémenter le pattern Factory à la sauce Scala.

---

# 1. Le Singleton Naturel : `object`

En Scala, pas besoin de `static`. On utilise `object`.

```scala
object Config:
  val SeuilValidation = 1000000
  val DeviseDefaut = "MAD"
```

- Une seule instance créée automatiquement par la JVM.
- Remplace les méthodes et constantes statiques de Java.

---

# 🤝 L'Objet Compagnon (Companion Object)

C'est un `object` qui porte le **même nom** qu'une `class` ou un `trait` dans le même fichier.

```scala
trait Transaction:
  def amount: BigDecimal

object Transaction:
  // C'est ici qu'on placera les méthodes "statiques"
  def defaultAmount = BigDecimal("0.0")
```

> 💡 Ils partagent un lien spécial et peuvent accéder aux membres privés l'un de l'autre.

---

# 🏗️ La Magie de `apply`

La méthode `apply` est une méthode spéciale invoquée sans son nom.

```scala
object Transaction:
  def apply(source: String, amt: BigDecimal): Transaction = 
    // Logique de construction...
```

### Appel sans `new`
```scala
val tx = Transaction("ATH", 100) // Appelle Transaction.apply(...)
```
> 💡 C'est le standard en Scala 3 pour construire des instances.

---

# 🏭 Pattern Factory avec `apply`

Trés utile pour transformer une entrée "sale" (CSV) en objet propre.

```scala
object Transaction:
  def fromCsv(line: String): Transaction =
    val parts = line.split(",")
    // Création...
```

---

# 🧠 Quiz Rapide

1. Comment appelle-t-on un `object` ayant le même nom qu'une classe ?
2. Quel est l'avantage majeur d'utiliser `apply` ?
3. Est-il nécessaire d'utiliser le mot-clé `new` en Scala 3 pour les objets compagnons ?

---

# 📝 Résumé du Jour

- `object` est un singleton qui remplace `static`.
- Un compagnon regroupe la logique liée à un type mais sans instance.
- `apply` permet une syntaxe de construction fluide et concise.
- C'est la fin du Mois 1 - Semaine 2 : Ton moteur commence à avoir fière allure !

**Prochaine étape** : Assembler le moteur v0.2 dans le TP 10 !
