---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 7"
footer: "Jour 2 — Algorithme de compensation multilatérale"
---

# Netting Multilatéral
## Optimiser les transferts de fonds globaux

**Durée :** ~2h | **Fil Rouge :** Le cœur du moteur de clearing

---

# 📋 Objectifs du Jour

- Comprendre la différence entre netting bilatéral et multilatéral.
- Modéliser une position nette globale par banque.
- Implémenter l'algorithme de réduction des flux.
- Vérifier l'équilibre du système (Somme des positions = 0).

---

# 1. Bilatéral vs Multilatéral

### Bilatéral (Hier)
Deux banques s'arrangent entre elles.
- A doit 100 à B.
- B doit 40 à A.
- Reste : A doit 60 à B.

### Multilatéral (Aujourd'hui)
On regarde la banque par rapport à **tout le système**.
- A doit à B, B doit à C, C doit à A.
- On calcule un seul montant par banque : le **Solde Net Global**.

---

# 2. Algorithme de Position Nette

Pour chaque banque, on calcule ses **Inbound** (reçus) et ses **Outbound** (envoyés).

```scala
case class BankPosition(bank: String, balance: BigDecimal)

// Formule magique du Clearing
val netBalance = totalReceived - totalSent
```

- Si `netBalance` > 0 : La banque est **Créditrice** (reçoit de l'argent du système).
- Si `netBalance` < 0 : La banque est **Débitrice** (doit payer au système).

---

# 3. L'Équilibre du Netting

C'est la règle d'or inviolable de la compensation :

> [!IMPORTANT]
> Dans un système fermé, la somme de toutes les positions nettes doit être EXACTEMENT égale à **zéro**.

Si `somme != 0`, il y a une "perte" ou une "création" d'argent fantôme dans le moteur.

---

# ⚠️ Compromis Métier du Stage

> [!NOTE]
> Notre moteur utilise des **simplifications acceptables** par rapport à la réalité.

| Aspect | Réalité Bancaire | Notre Moteur (Stage) |
|---|---|---|
| **Devise** | Multi-devises (MAD, EUR, USD) avec conversion | **Mono-devise (MAD)** — on additionne directement |
| **Frais** | La chambre de compensation prélève des frais par transaction | **Pas de frais** — la Règle du Zéro Somme reste pure |
| **Collatéral** | Chaque banque doit déposer une garantie à la Banque Centrale | **Non modélisé** — on suppose que chaque banque peut toujours payer |
| **Date de Valeur** | Compensation par cycles (cycle de midi, cycle de fin de journée) | **Par batch arbitraire** — on traite tout d'un coup |

> 💡 Ces simplifications permettent de se concentrer sur l'algorithmique et la programmation fonctionnelle. Chaque point pourrait être un projet à part entière en production.

---

# 🏗️ Implémentation Fonctionnelle

```scala
val positions = allTxs.foldLeft(Map.empty[String, BigDecimal]) { (acc, tx) =>
  val accWithSender = acc.updatedWith(tx.sender)(_.map(_ - tx.amount).orElse(Some(-tx.amount)))
  accWithSender.updatedWith(tx.receiver)(_.map(_ + tx.amount).orElse(Some(tx.amount)))
}
```

> 💡 `updatedWith` est une méthode puissante de Scala 2.13+ pour modifier une valeur dans une Map de manière atomique.

---

# 🔍 Pas-à-Pas Visuel : `foldLeft` + `updatedWith`

Prenons 2 transactions : `A → B (100)` et `B → C (60)`.

| Étape | Transaction | Accumulateur (Map) |
|---|---|---|
| Init | — | `{}` |
| 1a | A envoie 100 | `{A: -100}` |
| 1b | B reçoit 100 | `{A: -100, B: +100}` |
| 2a | B envoie 60 | `{A: -100, B: +40}` |
| 2b | C reçoit 60 | `{A: -100, B: +40, C: +60}` |

**Vérification** : -100 + 40 + 60 = **0** ✅

> 💡 L'accumulateur est une `Map` qui se met à jour à chaque transaction. Chaque appel à `updatedWith` crée une **nouvelle** Map (immuabilité !).

---

# 🧠 Quiz Rapide

1. Pourquoi le netting multilatéral est-il plus efficace que le bilatéral ? (Réduit encore plus le nombre de transferts réels).
2. Que signifie une position nette de -500 000 DH pour une banque ?
3. Quelle est la somme théorique de toutes les positions nettes d'un clearing ?

---

# 📝 Résumé du Jour

- Le netting multilatéral réduit chaque banque à un seul chiffre.
- Le système doit toujours être à l'équilibre (Somme = 0).
- `foldLeft` combiné à `updatedWith` permet un calcul propre et performant.
- Tu viens d'implémenter l'algorithme central d'ATH !

**Prochaine étape** : Développer le calculateur multilatéral dans le TP 32 !
