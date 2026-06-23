# Semaine 14 : Observer ZIO autour du moteur fil rouge

Cette semaine prolonge le Clearing Engine construit de S1 à S13. Elle n'est pas une migration du moteur vers ZIO. Le stagiaire ajoute un seul petit module `distributed.zio.ZioClearingModule`, puis observe les effets, les dépendances, les ressources, le parallèle et le retry.

## Fil conducteur

- Le domaine `clearing.model` reste la source de vérité.
- Le cœur `clearing.core` reste en Scala de base.
- `ValidationService` et `NettingService` enveloppent le cœur existant.
- Les TPs modifient seulement le module d'observation ZIO.
- Chaque exercice dure 5 à 10 minutes et se termine par une sortie console, une erreur contrôlée, ou une erreur de compilation lisible.

## Jour 1 - Effet et comparaison Scala de base

**Cours :** `ZIO[R, E, A]` décrit un programme qui demande `R`, peut échouer avec `E`, et produit `A`. La description n'est exécutée qu'au bord du programme.

**TP :** copier `ZioClearingModule`, comparer `baseScalaNetting` et `validateAndNetting`, puis vérifier que les résultats restent cohérents.

## Jour 2 - ZLayer et injection

**Cours :** le canal `R` rend les dépendances explicites. `ZLayer` fournit les services au bord du programme.

**TP :** lire `ValidationService & NettingService`, retirer temporairement une couche, puis observer une erreur métier issue du cœur existant.

## Jour 3 - Ressources et Scope

**Cours :** `ZIO.acquireRelease` et `ZIO.scoped` bornent la durée de vie d'une ressource.

**TP :** observer `audit acquire`, `audit release`, puis la fermeture en cas d'erreur contrôlée.

## Jour 4 - Parallèle borné et timeout

**Cours :** les fibers permettent de lancer des effets en parallèle avec une borne locale.

**TP :** comparer `foreach` et `foreachPar`, changer `withParallelism`, puis observer `timeout`.

## Jour 5 - Retry borné

**Cours :** `Schedule` décrit une politique de retry. On retente seulement les erreurs temporaires.

**TP :** compter les retries sur une publication de clearing, observer que `UNKNOWN_BANK` n'est pas retenté, puis raccourcir la politique.

## Limite volontaire

Pas de vrai client HTTP, pas de repository, pas de circuit breaker industriel, pas de refonte du moteur. Ces sujets pourront venir plus tard; la semaine 14 sert d'abord à observer ZIO sur un terrain connu.
