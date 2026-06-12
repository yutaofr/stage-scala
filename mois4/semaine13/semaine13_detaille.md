# Semaine 13 : Concurrence Native (5 jours)

## Jour 1 — Threads JVM & Race Conditions
**Cours (2h)** : Le modèle de mémoire partagée. Pourquoi les variables mutables + threads = bugs invisibles. Simulation d'une race condition sur le solde d'une banque. Locks et Synchronized (et pourquoi c'est fragile).
**TP (4h)** : Simuler un bug de concurrence : deux threads modifient le même `var balance`. Observer l'incohérence. Comprendre pourquoi l'immuabilité FP élimine ce problème.

## Jour 2 — Les Futures
**Cours (2h)** : `Future[T]` : un calcul qui se termine "plus tard". `map`, `flatMap`, `recover` sur les Futures. La promesse : ne jamais bloquer le thread principal.
**TP (4h)** : Lancer 5 validations de transactions en parallèle. Récupérer les résultats avec `Future.sequence`. Implémenter un timeout global.

## Jour 3 — Execution Contexts & Thread Pools
**Cours (2h)** : Pourquoi `ExecutionContext.global` est dangereux en production. Pools dédiés : nombre fixe pour le CPU, nombre variable pour les I/O. Pattern Bulkhead.
**TP (4h)** : Créer deux pools : un pour le calcul de netting (CPU), un pour la lecture de fichiers (I/O). Benchmark : comparer les performances.

## Jour 4 — Modèle d'Acteurs (Akka/Pekko)
**Cours (2h)** : Philosophie "Let it Crash". Un acteur = un état privé + une boîte aux lettres. Pas de mémoire partagée. Communication par messages.
**TP (4h)** : Créer un acteur `BankVault` qui gère le solde d'une banque et traite les `Credit`/`Debit` de manière séquentielle.

## Jour 5 — Supervision & Hiérarchie
**Cours (2h)** : Stratégies de supervision (Restart, Stop, Escalate). L'arbre d'acteurs.
**TP (4h)** : Créer un superviseur qui redémarre les acteurs de validation en cas de crash.
