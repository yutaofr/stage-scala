---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 13"
footer: "Jour 5 — Supervision & Résilience"
---

# Résilience des Acteurs
## "Let it Crash" - La philosophie de la survie

**Durée :** ~2h | **Fil Rouge :** Un système bancaire auto-réparateur

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Supervision**.
- Apprendre pourquoi il vaut mieux laisser un acteur mourir et le redémarrer.
- Découvrir les stratégies de supervision (Restart, Stop, Resume).
- Rendre le système de clearing insensible aux pannes logicielles.

---

# 1. La Hiérarchie des Acteurs

Les acteurs sont organisés comme une entreprise. Chaque acteur a un parent (le superviseur) qui l'a créé.

### Responsabilité
Si un "ouvrier" (acteur enfant) rencontre une erreur (exception), il s'arrête et demande à son "manager" (parent) : **"Que dois-je faire ?"**.

---

# 2. Stratégies de Supervision

Le superviseur décide du sort de l'enfant en fonction de l'erreur :

1. **Restart** : Créer une nouvelle instance de l'acteur (état réinitialisé). C'est le plus commun.
2. **Resume** : Continuer avec la même instance (si l'erreur est mineure).
3. **Stop** : Arrêter l'acteur définitivement.

> 💡 Cette approche permet de compartimenter les erreurs : une erreur dans une banque ne doit pas faire tomber tout le système de clearing.

---

# 3. Auto-Réparation

Grâce à la supervision, votre système peut se "soigner" tout seul. Si un acteur responsable d'une banque plante à cause d'un bug de calcul, le superviseur le redémarre, et il est prêt à traiter le message suivant.

> [!CAUTION]
> **Perte d'état au Restart** : Quand un acteur redémarre, son état interne (`var balance`) est réinitialisé. En production, on résout ce problème avec l'**Event Sourcing** : chaque message traité est persisté dans un journal (Kafka, base de données). Au redémarrage, l'acteur rejoue tous les événements pour reconstruire son état exact. Ce sera un sujet futur.

---

# 4. ⚠️ Piège Scala 3 : Trait avec Paramètres

Si vous ajoutez un paramètre à un trait (même avec une valeur par défaut), les classes qui en héritent doivent utiliser `()` :

```scala
// Déclaration du trait
trait SimpleActor[T](strategy: SupervisorStrategy = SupervisorStrategy.Stop)

// ❌ Ne compile PAS (Parameterized trait lacks argument list)
class MyActor extends SimpleActor[String]:

// ✅ Compile
class MyActor extends SimpleActor[String]():
class MyActor extends SimpleActor[String](SupervisorStrategy.Restart):
```

---

# 🏗️ Application : Le Manager Solide

Nous allons implémenter un superviseur pour nos acteurs `BankVault`. Si l'un d'eux lève une `ArithmeticException` (ex: solde négatif interdit), le manager le redémarrera proprement.

---

# 🧠 Quiz Rapide

1. Qui est responsable de la survie d'un acteur ? (Son parent superviseur).
2. Pourquoi ne pas utiliser `try/catch` à l'intérieur de l'acteur ? (Parce que la supervision permet une séparation plus propre des responsabilités et une gestion globale des pannes).
3. Que perdrons-nous si nous redémarrons (`Restart`) un acteur ? (Son état interne actuel : `var balance`).
4. Comment un système de production récupère-t-il l'état perdu ? (Event Sourcing : rejeu du journal d'événements).

---

# 📝 Résumé de la Semaine 13

| Jour | Concept | Apport pour le Moteur |
|------|---------|----------------------|
| J1 | Threads & Race Conditions | Comprendre le danger de `var` partagé |
| J2 | Futures | Parallélisme non-bloquant (`Future.sequence`) |
| J3 | Execution Contexts | Isolation CPU / IO (Pattern Bulkhead) |
| J4 | Modèle d'Acteurs | Modélisation par messages, état confiné |
| J5 | Supervision | Résilience "Let it Crash", auto-réparation |

**Prochaine étape** : Tester la résilience dans le TP du Jour 5 !
