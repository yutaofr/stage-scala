---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 13"
footer: "Jour 1 — Threads JVM & Problèmes de Concurrence"
---

# Concurrence Native
## Gérer plusieurs tâches en même temps

**Durée :** ~2h | **Fil Rouge :** Le problème du solde partagé

---

# 📋 Objectifs du Jour

- Comprendre le fonctionnement des **Threads** sur la JVM.
- Identifier les dangers de l'état partagé mutable (**Race Conditions**).
- Découvrir pourquoi le modèle impératif de concurrence est difficile.
- Apprendre à utiliser les verrous (`synchronized`) avec prudence.

---

# 1. Qu'est-ce qu'un Thread ?

Un thread est un "fil d'exécution". Votre ordinateur possède plusieurs cœurs, il peut donc faire tourner plusieurs threads en parallèle.

### En Scala 3
```scala
val thread = new Thread(() => println("Travail en parallèle"))
thread.start()
thread.join() // Attend que le thread termine
```

> [!WARNING]
> Créer un thread est une opération **lourde** pour l'OS. On ne doit pas en créer des milliers sans contrôle.

---

# 2. La "Race Condition" (Course Critique)

Si deux threads modifient la même variable en même temps, le résultat final est imprévisible.

### Exemple
1. Thread A lit `solde = 100`.
2. Thread B lit `solde = 100`.
3. Thread A écrit `solde = 100 + 50 = 150`.
4. Thread B écrit `solde = 100 - 30 = 70`.
5. **Résultat** : 150 ou 70 ? Le virement de 50 ou celui de 30 a été "perdu".

---

# 3. Pourquoi la FP aide ici ?

L'immuabilité (Mois 3) élimine les **data races** : si un objet ne peut pas être modifié, il peut être partagé entre 1000 threads sans aucun danger de corruption.

> [!WARNING]
> L'immuabilité ne suffit pas à elle seule pour gérer de l'état partagé. Il faut aussi un **mécanisme de coordination** : `AtomicReference`, un acteur centralisé, ou une structure concurrente. L'immuabilité garantit la sécurité de lecture, la coordination garantit la cohérence d'écriture.

### En Scala
On combine l'immuabilité des données avec des mécanismes de coordination (Atomic, Acteurs) pour obtenir un code concurrent sûr et performant.

---

# 🏗️ Application : Le Virement Simultané

Nous allons simuler deux banques qui tentent de débiter le même compte en même temps et observer la corruption des données si nous ne faisons pas attention.

---

# 🧠 Quiz Rapide

1. Qu'est-ce qui consomme le plus de ressources : un objet ou un thread ? (Un thread).
2. Est-ce qu'un objet immutable a besoin de verrous (`synchronized`) ? (Non, c'est son super-pouvoir).
3. Pourquoi appelle-t-on cela une "Race Condition" ? (Parce que le résultat dépend de qui "arrive le premier" à l'écriture).

---

# 📝 Résumé du Jour

- La concurrence native est puissante mais dangereuse avec l'état mutable.
- Les threads JVM sont des ressources coûteuses.
- L'immuabilité est la meilleure défense contre la corruption de données.
- Demain, nous verrons comment faire de la concurrence "propre" avec les **Futures**.

**Prochaine étape** : Provoquer une race condition dans le TP du Jour 1 !
