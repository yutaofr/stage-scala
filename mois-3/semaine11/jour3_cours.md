---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 11"
footer: "Jour 3 — Extension Methods"
---

# Extension Methods
## Ajouter des super-pouvoirs à vos objets

**Durée :** ~2h | **Fil Rouge :** Une syntaxe fluide pour le Clearing Engine

---

# 📋 Objectifs du Jour

- Comprendre le concept de "pimp-my-lib" (enrichissement de classes).
- Maîtriser la syntaxe des `extension methods` en Scala 3.
- Allier Type Classes et Extension Methods.
- Améliorer l'expérience de lecture du code métier.

---

# 1. Pourquoi des extensions ?

Parfois, on aimerait appeler une méthode directement sur un objet, mais cette méthode n'est pas dans la classe (ex: issue d'une lib externe ou d'une séparation technique).

### Avant (Style Utils)
`Serializer.toCsv(transaction)`

### Après (Style Extension)
`transaction.toCsv` ✅ — C'est plus naturel !

---

# 2. Syntaxe Scala 3

C'est simple, on définit la méthode "en dehors" de la classe.

```scala
extension (tx: Transaction)
  def isDomestic: Boolean = tx.sender.startsWith("MA")
  def isHighValue: Boolean = tx.amount > 100000
```

On peut maintenant coder :
```scala
if tx.isDomestic && tx.isHighValue then ...
```

---

# 3. Le duo gagnant : Type Class + Extension

On peut créer une méthode d'extension qui utilise automatiquement une Type Class présente dans le contexte.

```scala
extension [T](item: T)(using serializer: ClearingSerializable[T])
  def toText: String = serializer.toText(item)

// Utilisation finale :
transaction.toText
bank.toText
```

> 💡 C'est ainsi que fonctionnent `.toJson` ou `.show` dans les bibliothèques professionnelles comme **Cats**.

---

# 🏗️ Application : Fluent Validation

Nous allons ajouter des extensions à nos types de base (String, BigDecimal) pour rendre la validation plus lisible.

```scala
extension (s: String)
  def isIban: Boolean = s.startsWith("MA") && s.length == 24
```

---

# 🧠 Quiz Rapide

1. Une extension method modifie-t-elle le bytecode de la classe d'origine ? (Non, c'est du sucre syntaxique compilé comme un appel statique).
2. Peut-on ajouter une extension à un type primitif comme `Int` ? (Oui !).
3. Quel est l'avantage de `tx.toText` par rapport à `Serializer.toText(tx)` ? (Meilleure auto-complétion et lecture "sujet-verbe").

---

# 📝 Résumé du Jour

- Les `extension methods` permettent d'écrire du code plus "orienté objet" tout en restant purement fonctionnel.
- Elles sont le pont final entre tes Type Classes et ton code métier.
- Ton DSL (Domain Specific Language) de clearing devient extrêmement fluide.
- "Transaction . to . Json" -> On ne peut pas faire plus clair !

**Prochaine étape** : Enrichir tes modèles dans le TP 53 !
