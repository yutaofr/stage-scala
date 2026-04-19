---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 5"
footer: "Jour 3 — Pattern Matching Exhaustif sur ADT"
---

# Pattern Matching & ADT
## La garantie de sécurité du compilateur

**Durée :** ~2h | **Fil Rouge :** Traitement infaillible des statuts et erreurs

---

# 📋 Objectifs du Jour

- Comprendre le lien entre **Algebraic Data Types** (ADT) et Pattern Matching.
- Maîtriser l'**Exhaustivité** (Check d'oubli de cas).
- Apprendre à déstructurer des ADTs complexes.
- Rendre le code du moteur "incassable" face aux évolutions.

---

# 1. Qu'est-ce qu'un ADT ?

Un **Algebraic Data Type** est la combinaison de :
- **Types Produits** (`case class`) : A **ET** B ET C.
- **Types Sommes** (`enum` / `sealed trait`) : A **OU** B OU C.

### Exemple
Une `Transaction` (Produit) peut avoir un `Status` (Somme).

---

# 2. Exhaustivité Garantie

Grâce au mot-clé `sealed`, le compilateur connaît tous les enfants d'un trait.

```scala
def message(e: ClearingError): String = e match
  case InvalidAmount(a) => s"Montant invalide : $a"
  case UnknownBank(c)   => s"Banque inconnue : $c"
  // ⚠️ Si j'oublie DuplicateTransaction, 
  // le compilateur affiche un WARNING ! ✅
```

> [!TIP]
> Dans un système bancaire, un "warning" d'exhaustivité doit être traité comme une erreur bloquante. Ne laissez jamais de branche non gérée.

---

# 3. Déstructuration Avancée

On peut extraire les données imbriquées directement dans le match.

```scala
error match
  case ValidationError(champ, msg) if champ == "amount" =>
    s"Erreur critique sur le montant : $msg"
  case ValidationError(_, msg) =>
    s"Erreur de saisie : $msg"
  case _ =>
    "Autre erreur"
```

---

# 🛡️ Pourquoi c'est Vital ?

Si demain la direction d'ATH ajoute un nouveau type d'erreur `FraudDetected`, le compilateur te montrera **tous les endroits** de ton code que tu dois mettre à jour.

> 💡 C'est le contraire du style impératif (if/else) où on risque d'oublier un cas caché dans un fichier lointain.

---

# 🧠 Quiz Rapide

1. Quel mot-clé permet l'exhaustivité sur un trait ?
2. Quelle est la différence entre un "warning" et une "erreur" de compilation pour l'exhaustivité ? (Le warning permet de compiler mais prévient d'un crash potentiel).
3. Peut-on faire du pattern matching sur une `enum` ?

---

# 📝 Résumé du Jour

- Les ADTs modélisent parfaitement les données métier complexes.
- Le Pattern Matching est le moyen naturel de les consommer.
- L'exhaustivité transforme les bugs de runtime en erreurs de compilation.
- Ton code devient auto-documenté et résilient aux changements.

**Prochaine étape** : Implémenter des routeurs et reporters exhaustifs dans le TP 23 !
