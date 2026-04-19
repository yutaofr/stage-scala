---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 6"
footer: "Jour 3 — Pattern Matching + For-yield"
---

# Pattern Matching & For-yield
## Maîtriser le pipeline de données typées

**Durée :** ~2h | **Fil Rouge :** Orchestration du pipeline de validation

---

# 📋 Objectifs du Jour

- Comprendre comment le Pattern Matching s'intègre dans les `for-yield`.
- Utiliser le filtrage implicite dans les boucles for.
- Gérer les collections d'options et de résultats.
- Créer un pipeline qui accumule les transactions valides proprement.

---

# 1. Le Filtrage Implicite dans `for`

On peut déstructurer et filtrer une collection directement dans le `for`.

```scala
val txs = List(
  Transaction(1, Some(100)),
  Transaction(2, None),        // Montant absent
  Transaction(3, Some(500))
)

// Ne traite QUE les transactions qui ont un montant
val total = for
  Transaction(id, Some(amt)) <- txs 
yield amt
// Résultat : List(100, 500)
```

---

# 2. Pattern Matching sur "Générateurs"

Si le pattern ne correspond pas, l'élément est simplement ignoré (pas d'erreur).

```scala
val errors = List(
  ValidationError("champ1", "msg1"),
  SystemError("panne")
)

// Extraire seulement les messages des erreurs de validation
val messages = for
  ValidationError(_, msg) <- errors
yield msg
```

> 💡 C'est un moyen très condensé de filtrer par type.

---

# 3. For-yield sur Options multiples

C'est la manière la plus propre de chaîner des calculs qui peuvent échouer.

```scala
val result = for
  user     <- findUser(id)
  account  <- findAccount(user)
  balance  <- getBalance(account)
  if balance > 0 // Garde (condition supplémentaire)
yield balance
```

> [!NOTE]
> Si une étape est `None`, tout s'arrête et `result` devient `None`. C'est le comportement monadique de l'Option.

---

# 🏗️ Application : Pipeline de Clearing

```scala
val processed = for
  line <- csvLines
  tx   <- Transaction.fromCsv(line) // Option[Transaction]
  if tx.amount > 0                 // Filtre métier
yield tx
```

---

# 🧠 Quiz Rapide

1. Que se passe-t-il si un élément de la liste ne correspond pas au pattern dans le `for` ? (Il est ignoré).
2. Comment ajouter une condition `if` dans une for-comprehension ?
3. Le code `for (Some(x) <- list) yield x` est équivalent à quelle méthode de collection ? (`list.flatten`).

---

# 📝 Résumé du Jour

- Le `for-yield` est bien plus qu'une boucle : c'est un langage de manipulation de données.
- Le pattern matching dans le `for` combine filtrage et déstructuration.
- C'est l'outil idéal pour nettoyer un flux de données (ex: CSV -> Transactions valides).
- Ton code gagne en concision tout en restant strictement typé.

**Prochaine étape** : Créer un pipeline de validation complexe dans le TP 28 !
