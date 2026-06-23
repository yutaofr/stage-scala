# Semaine 14 : Orchestration des Effets avec ZIO

Cette semaine sert à observer ZIO sur le fil rouge. Les TPs ne sont pas des katas de reconstruction : chaque exercice dure 5 à 10 minutes et produit un constat visible.

## Jour 1 - Fondations de ZIO

**Cours :** `ZIO[R, E, A]` décrit un programme qui demande `R`, peut échouer avec `E`, et produit `A`. Une valeur ZIO reste une description tant que le runtime ne l'exécute pas.

**TP :** observer `Console`, une erreur typée, et `zipPar` dans un programme court.

## Jour 2 - ZLayer et injection

**Cours :** le canal `R` rend les dépendances explicites. `ZLayer` fournit ces dépendances au bord du programme.

**TP :** lire les dépendances dans le type, retirer une couche, et observer l'erreur de compilation.

## Jour 3 - Ressources et Scope

**Cours :** `ZIO.acquireRelease` et `ZIO.scoped` bornent la durée de vie d'une ressource.

**TP :** observer `acquire`, `release`, puis la fermeture en cas d'erreur contrôlée.

## Jour 4 - Fibers et concurrence

**Cours :** les fibers permettent de lancer des effets en parallèle, avec interruption et bornes de parallèle.

**TP :** comparer `foreach` et `foreachPar`, changer `withParallelism`, puis observer `timeout`.

## Jour 5 - Retry et résilience

**Cours :** `Schedule` décrit une politique de retry. Un circuit ouvert évite d'appeler un service déjà en panne.

**TP :** compter les retries, distinguer une erreur définitive, et observer un appel court-circuité.
