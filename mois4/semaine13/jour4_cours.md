---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 13"
footer: "Jour 4 — Modèle d'Acteurs (Akka/Pekko)"
---

# Introduction aux Acteurs
## Programmer avec des entités vivantes

**Durée :** ~2h | **Fil Rouge :** Modéliser les banques du système interbancaire

---

# 📋 Objectifs du Jour

- Comprendre les limites du modèle d'objet classique face à la concurrence.
- Découvrir le **Modèle d'Acteurs** (L'héritage d'Erlang).
- Apprendre les principes : "Tout est acteur", "Envoi de message asynchrone".
- Créer son premier acteur pour gérer un solde bancaire.

---

# 1. Pourquoi des Acteurs ?

Dans le monde réel, les banques ne "s'appellent" pas mutuellement en modifiant leurs variables. Elles **s'envoient des messages**.

### Le problème des verrous (`synchronized`)
C'est un goulot d'étranglement. Un acteur, lui, traite ses messages **un par un** (File d'attente), garantissant qu'il n'y a jamais de corruption sans avoir besoin de verrous complexes.

---

# 2. Qu'est-ce qu'un Acteur ?

Un acteur possède :
1. **Un État** (Privé, jamais partagé).
2. **Un Comportement** (Comment il réagit aux messages).
3. **Une Mailbox** (Dépôt des messages entrants).

```scala
case class Deposit(amount: BigDecimal)

class BankAccount extends Actor:
  var balance = BigDecimal(0)
  
  def receive =
    case Deposit(amt) => balance += amt
```

---

# 3. Envoi de Message : "Fire and Forget"

On n'appelle pas de méthode. On utilise l'opérateur `!`.

```scala
bankAccount ! Deposit(100.0)
```
- L'appel est **immédiat** et non-bloquant.
- L'acteur traitera le message quand il sera libre.
- C'est l'essence même de la distribution.

---

# 🏗️ Application : Le Réseau de Banques

Nous allons modéliser chaque banque comme un acteur indépendant. Le moteur de clearing enverra des messages aux acteurs banques pour mettre à jour leurs soldes après le netting.

---

# 🧠 Quiz Rapide

1. Est-ce que plusieurs threads peuvent entrer dans la méthode `receive` d'un acteur en même temps ? (Non, un seul message à la fois).
2. Comment un acteur communique-t-il son résultat ? (En envoyant un message en retour à l'expéditeur).
3. Puis-je lire la variable `balance` d'un acteur depuis l'extérieur ? (Non, l'état est encapsulé).

---

# 📝 Résumé du Jour

- Les acteurs sont parfaits pour modéliser des entités du monde réel.
- Ils gèrent l'état mutable de manière sûre (pas de verrous).
- Ils sont l'unité de base des systèmes hautement distribués.
- Demain, nous verrons comment les faire survivre aux pannes.

**Prochaine étape** : Créer ton premier acteur de banque dans le TP 64 !
