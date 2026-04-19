---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 5"
footer: "Jour 1 — case class (Types Produits)"
---

# case class : Le Type Produit
## Modéliser le domaine avec clarté et sécurité

**Durée :** ~2h | **Fil Rouge :** Sortir de l'anonymat des Tuples

---

# 📋 Objectifs du Jour

- Comprendre les limites des Tuples pour le métier.
- Maîtriser la syntaxe et les avantages des **case class**.
- Apprendre la comparaison structurelle et la copie d'objets.
- Migrer les premières briques du moteur vers des types nommés.

---

# 1. Pourquoi les Tuples ne suffisent plus ?

Jusqu'à présent, nous utilisions :
`("ATH", "CIH", BigDecimal("500"))`

### Les Problèmes
- **Ambiguïté** : Qui est l'expéditeur ? `_1` ou `_2` ?
- **Boilerplate** : Pas de méthodes métier intégrées.
- **Documentation** : Le type `(String, String, BigDecimal)` ne dit rien du métier.

---

# 2. Les Case Classes

Une `case class` est faite pour transporter des données immuables de manière lisible.

```scala
case class Transaction(
  id: Int,
  sender: String,
  receiver: String,
  amount: BigDecimal,
  txType: String
)

val tx = Transaction(1, "ATH", "CIH", 5000, "VIR")
println(tx.sender) // "ATH" ✅ — C'est clair !
```

---

# 🛡️ Les "Super-Pouvoirs" Gratuits

1. **Pas de `new`** : On instancie directement `Transaction(...)`.
2. **Immuabilité** : Les champs sont `val` par défaut.
3. **Comparaison Structurelle** : `tx1 == tx2` compare les valeurs, pas les adresses mémoire.
4. **Copy** : Créer un nouvel objet avec une modification mineure est simple :
   ```scala
   val updatedTx = tx.copy(amount = 6000)
   ```

---

# 3. Case Class vs Class

| Fonctionnalité | `class` classique | `case class` |
|---|---|---|
| Instanciation | `new MyClass()` | `MyClass()` |
| `equals`/`hashCode` | Manuels / Référence | **Automatiques / Valeurs** |
| Pattern Matching | Non | **Oui (natif)** |
| Usage type | Logique avec état | **Données pures (ADT)** |

---

# 🏗️ Application au Domaine

Nous allons transformer nos structures "anonymes" en structures "nommées".

```scala
case class Bank(code: String, name: String)
case class Account(iban: String, bank: Bank, balance: BigDecimal)
```

> 💡 On peut maintenant manipuler des objets complexes et imbriqués avec une grande lisibilité.

---

# 🧠 Quiz Rapide

1. Comment s'appelle le type formé par la combinaison de plusieurs champs dans une `case class` ? (Type Produit).
2. Est-il possible de modifier un champ d'une `case class` après sa création ?
3. Quelle méthode permet de créer un clone d'une `case class` avec une seule valeur modifiée ?

---

# 📝 Résumé du Jour

- Les `case class` remplacent avantageusement les Tuples.
- Elles apportent la sécurité du typage et la lisibilité du domaine.
- Elles sont immuables et supportent la comparaison par valeur.
- C'est la brique de base de la modélisation en Scala.

**Prochaine étape** : Migrer le moteur vers les case classes dans le TP 21 !
