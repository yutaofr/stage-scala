# Semaine 14 : Orchestration des Effets — ZIO (5 jours)

## Jour 1 — Fondations de ZIO
**Cours (2h)** : `ZIO[R, E, A]` décrit un programme qui demande `R`, peut échouer avec `E` et produit `A`. Différence avec `Future` : un effet ZIO est une description paresseuse, exécutée par le runtime. `ZIO.succeed`, `ZIO.fail`, `ZIO.attempt`, erreurs typées et défauts.
**TP (4h)** : Premiers programmes ZIO avec `Console`, `Random` et `Clock`. Encapsuler les appels existants du moteur sans masquer les erreurs métier.

## Jour 2 — ZLayer & Dependency Injection
**Cours (2h)** : Modulariser avec `ZLayer`. Graph de dépendances résolu à la compilation. Comparaison avec Spring DI : pas d'annotations, tout est dans les types.
**TP (4h)** : Découper l'appli en couches : `RepoLayer`, `ValidationLayer`, `ClearingLayer`, `ReportLayer`. Injecter les implémentations Mock vs Réelles.

## Jour 3 — Gestion des Ressources
**Cours (2h)** : `ZIO.acquireRelease` : garantir la fermeture des ressources (fichiers, connexions). Scoped : composition de ressources multiples.
**TP (4h)** : Lire un fichier de 100 000 transactions avec garantie de fermeture, même en cas de crash au milieu du traitement.

## Jour 4 — Fibers & Concurrence ZIO
**Cours (2h)** : Fibers : unités d'exécution légères gérées par le runtime ZIO. `fork`, `join`, `interrupt`, concurrence structurée et parallélisme borné.
**TP (4h)** : Lancer plusieurs calculs de compensation avec une limite de parallélisme. Ajouter un timeout de 2 secondes par batch et vérifier l'interruption.

## Jour 5 — Retry & Circuit Breaker
**Cours (2h)** : Retry borné, backoff exponentiel, jitter et distinction entre erreur transitoire et erreur définitive. Pattern Circuit Breaker pour les services instables.
**TP (4h)** : Rendre les appels au service de taux de change résilients avec un retry borné. Tester avec un service déterministe qui échoue sur les premières tentatives.
