---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 6"
footer: "Jour 2 — Gardes et Extracteurs"
---

# Gardes et Extracteurs
## Ajouter de l'intelligence au Pattern Matching

**Durée :** ~2h | **Fil Rouge :** Validation complexe des dates et montants

---

# 📋 Objectifs du Jour

- Utiliser les **Gardes** (`if`) pour des conditions logiques dans le match.
- Découvrir les **Extracteurs** personnalisés (objet `unapply`).
- Simplifier le code de validation complexe.
- Allier la puissance du typage et de la logique booléenne.

---

# 1. Les Gardes (`if`)

Le pattern matching peut être complété par une condition arbitraire.

```scala
tx match
  case t: Transaction if t.amount > 1000000 => 
    "🚨 GROS VIREMENT — Vigilance"
  case t: Transaction if t.sender == t.receiver =>
    "⚠️ Auto-virement détecté"
  case t: Transaction =>
    "✅ Transaction standard"
```

> 💡 La garde permet de tester ce qui n'est pas exprimable par le type seul.

---

# 2. Les Extracteurs (`unapply`)

C'est "l'inverse" du constructeur. Un objet peut définir comment il se décompose.

```scala
object HighValue:
  def unapply(tx: Transaction): Option[BigDecimal] =
    if tx.amount > 50000 then Some(tx.amount) else None

// Utilisation
tx match
  case HighValue(amt) => s"Attention : $amt DH est un gros montant"
  case _              => "RAS"
```

### 🔍 Comment ça fonctionne sous le capot ?
Quand le compilateur voit `case HighValue(amt)`, il fait ceci :
1. Il appelle `HighValue.unapply(tx)` avec la valeur testée.
2. Si le résultat est `Some(valeur)`, le pattern **matche** et `amt` reçoit la valeur extraite.
3. Si le résultat est `None`, le pattern **ne matche pas** et on passe au `case` suivant.

> 💡 C'est exactement l'inverse de `apply` : `apply` **construit** un objet, `unapply` le **décompose**.


---

# 🏗️ Application : Validation de Date

On peut créer un extracteur pour les transactions tardives.

```scala
object NightTransaction:
  def unapply(tx: Transaction): Boolean =
    val hour = tx.timestamp.getHour
    hour < 6 || hour > 22

tx match
  case NightTransaction() => "Virement de nuit — Délai de validation +24h"
  case _                  => "Virement jour"
```

---

# 3. Combiner Patterns & Gardes

On peut mixer la déstructuration de case class et les gardes.

```scala
case class Transaction(id: Int, amount: BigDecimal, ...)

tx match
  case Transaction(_, amt, _) if amt < 0 => "ERREUR : Montant négatif"
  case Transaction(id, _, _) if id == 0  => "ERREUR : ID invalide"
```

---

# 🧠 Quiz Rapide

1. Où se place la clause `if` dans un `case` ? (Après le pattern, avant la flèche `=>`).
2. Quelle méthode doit-on implémenter pour créer un extracteur personnalisé ? (`unapply`).
3. Un extracteur peut-il retourner plusieurs valeurs ? (Oui, via un Tuple dans l'Option).

---

# 📝 Résumé du Jour

- Les gardes ajoutent de la logique métier fine au pattern matching.
- Les extracteurs permettent de créer nos propres "motifs" de reconnaissance.
- Le code gagne en lisibilité en séparant la structure (pattern) de la logique (garde).
- Tu peux maintenant modéliser des règles complexes (fraude, horaires) très simplement.

**Prochaine étape** : Implémenter des validateurs intelligents dans le TP 27 !
