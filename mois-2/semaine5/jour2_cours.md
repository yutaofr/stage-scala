---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 5"
footer: "Jour 2 — enum / sealed trait (Types Sommes)"
---

# Enums & Sealed Traits
## Modéliser l'alternance et les choix métier

**Durée :** ~2h | **Fil Rouge :** Supprimer les "Strings magiques" dangereux

---

# 📋 Objectifs du Jour

- Comprendre le danger des chaînes de caractères pour les statuts.
- Maîtriser les **enums** de Scala 3 pour les choix finis.
- Utiliser les **sealed traits** pour des hiérarchies d'erreurs extensibles.
- Découvrir les "Types Sommes" (Type A OU Type B).

---

# 1. Le Danger des "Strings magiques"

```scala
val status = "Pendding" // ❌ ERREUR DE FRAPPE !
```

### Problèmes
- Le compilateur ne voit pas l'erreur.
- Crash au runtime ou bug silencieux.
- Documentation inexistante sur les valeurs possibles.

---

# 2. Les Enums Scala 3

Une `enum` définit une liste fermée de valeurs possibles.

```scala
enum TransactionStatus:
  case Pending, Validated, Rejected

enum TransactionType:
  case Transfer, Withdrawal, Deposit
```

### Avantages
- Typage fort : Impossible de passer une valeur invalide.
- Auto-complétion dans l'IDE.
- Sécurité totale au Pattern Matching.

---

# 3. Sealed Traits (Types Sommes Avancés)

Un `sealed trait` est comme une enum, mais chaque cas peut être une `case class` avec ses propres données.

```scala
sealed trait ClearingError

case class ValidationError(field: String, msg: String) extends ClearingError
case class BusinessError(code: String) extends ClearingError
case object SystemUnavailable extends ClearingError
```

> 💡 "Sealed" signifie que tous les enfants doivent être dans le **même fichier**. Cela permet au compilateur de connaître tous les cas possibles.

---

# 🏗️ Application au Moteur

Nous allons typer nos erreurs de validation.

```scala
def valider(tx: Transaction): Option[ClearingError] =
  if tx.amount <= 0 then 
    Some(ValidationError("amount", "Doit être positif"))
  else 
    None
```

---

# 🧠 Quiz Rapide

1. Que signifie le mot-clé `sealed` ?
2. Quelle est la différence entre une `enum` simple et un `sealed trait` avec des `case class` ?
3. Comment s'appelle une structure qui représente une alternative (Type A OU Type B) ? (Type Somme).

---

# 📝 Résumé du Jour

- Les `enum` sécurisent les choix simples (statuts).
- Les `sealed trait` sécurisent les hiérarchies complexes (erreurs).
- Le compilateur devient ton allié : il vérifie que tu gères tous les cas.
- C'est la fin des bugs liés aux fautes de frappe dans les chaînes !

**Prochaine étape** : Typer tes statuts et tes erreurs dans le TP 22 !
