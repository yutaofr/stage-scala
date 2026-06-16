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
1. **Un État** (Privé, jamais partagé — géré par récursion immuable).
2. **Un Comportement** (`Behavior[T]` : comment il réagit aux messages de type `T`).
3. **Une Mailbox** (Dépôt des messages entrants, traités un par un).

```scala
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

// Les messages que l'acteur comprend
sealed trait Command
case class Deposit(amount: BigDecimal) extends Command
case class GetBalance() extends Command

// L'acteur : un comportement récursif qui porte son état
def bankAccount(balance: BigDecimal): Behavior[Command] =
  Behaviors.receive { (context, message) =>
    message match
      case Deposit(amt) =>
        val newBalance = balance + amt
        context.log.info(s"Dépôt de $amt → Solde : $newBalance")
        bankAccount(newBalance) // Nouvel état via récursion
      case GetBalance() =>
        context.log.info(s"Solde actuel : $balance")
        Behaviors.same // Pas de changement d'état
  }
```

> [!TIP]
> Remarque : pas de `var` ! L'état est porté par le paramètre de la fonction récursive `bankAccount(balance)`. C'est la même philosophie FP qu'au Mois 3.

---

# 3. Envoi de Message : "Tell" (Fire and Forget)

On n'appelle pas de méthode. On utilise l'opérateur `!` (**tell**).

```scala
import org.apache.pekko.actor.typed.ActorRef

val bankRef: ActorRef[Command] = ??? // Créé par le système
bankRef ! Deposit(100.0)
```
- L'appel est **immédiat** et non-bloquant.
- L'acteur traitera le message quand il sera libre.
- Le typage `ActorRef[Command]` garantit à la compilation qu'on ne peut envoyer que des `Command`.
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

**Prochaine étape** : Créer ton premier acteur de banque dans le TP du Jour 4 !
