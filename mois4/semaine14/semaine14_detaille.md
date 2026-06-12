# Semaine 14 : Orchestration des Effets — ZIO (5 jours)

## Jour 1 — Fondations de ZIO
**Cours (2h)** : `ZIO[R, E, A]` : un plan d'exécution qui a besoin de `R`, peut échouer avec `E` et produit `A`. Différence avec Future : ZIO est un "blueprint" (pas de lancement immédiat). `ZIO.succeed`, `ZIO.fail`, `ZIO.attempt`.
**TP (4h)** : Premiers programmes ZIO : Console.printLine, Random, Clock. Wrapper les appels existants du moteur dans des ZIO.

## Jour 2 — ZLayer & Dependency Injection
**Cours (2h)** : Modulariser avec `ZLayer`. Graph de dépendances résolu à la compilation. Comparaison avec Spring DI : pas d'annotations, tout est dans les types.
**TP (4h)** : Découper l'appli en couches : `RepoLayer`, `ValidationLayer`, `ClearingLayer`, `ReportLayer`. Injecter les implémentations Mock vs Réelles.

## Jour 3 — Gestion des Ressources
**Cours (2h)** : `ZIO.acquireRelease` : garantir la fermeture des ressources (fichiers, connexions). Scoped : composition de ressources multiples.
**TP (4h)** : Lire un fichier de 100 000 transactions avec garantie de fermeture, même en cas de crash au milieu du traitement.

## Jour 4 — Fibers & Concurrence ZIO
**Cours (2h)** : Fibers : des "green threads" ultra-légers (millions sur une JVM). `fork`, `join`, `interrupt`. Concurrence structurée.
**TP (4h)** : Lancer N calculs de compensation en parallèle avec des Fibers. Ajouter un timeout de 2 secondes par batch.

## Jour 5 — Retry & Circuit Breaker
**Cours (2h)** : Politiques de retry exponentielles. Pattern Circuit Breaker pour les services instables.
**TP (4h)** : Rendre les appels au service de taux de change résilients avec `ZIO.retry(Schedule.exponential)`. Tester avec un service mock qui échoue 3 fois sur 4.
