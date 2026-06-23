# Semaine 14 : Observer ZIO autour du fil rouge personnel

Cette semaine prolonge le Clearing Engine que chaque stagiaire a construit de S1 à S13. Il n'existe pas de projet fil rouge prêt à importer depuis le support. Le stagiaire ajoute un seul module d'observation dans son propre projet, puis observe ZIO autour de son domaine, de sa validation, de son `Either`, et de son netting.

## Fil conducteur

- Le projet du stagiaire reste la source de vérité.
- Le cœur Scala de base reste intact.
- Le module S14 ne contient pas de logique métier nouvelle.
- Les TPs modifient seulement le module d'observation ZIO.
- Chaque exercice dure 5 à 10 minutes et se termine par une sortie console, une erreur contrôlée, ou une erreur de compilation lisible.
- Si un mapping de noms bloque, le tuteur donne le mapping et revient à l'observation.

## Jour 1 - Effet et comparaison Scala de base

**Cours :** `ZIO[R, E, A]` décrit un programme qui demande `R`, peut échouer avec `E`, et produit `A`. La description n'est exécutée qu'au bord du programme.

**TP :** remplir la fiche de correspondance, créer `ZioEffectObservation`, puis comparer un pipeline `Either` existant avec `ZIO.fromEither`.

## Jour 2 - Canal `R` et configuration locale

**Cours :** le canal `R` rend les dépendances explicites. Ici, on utilise une petite `ObservationConfig`, pas un nouveau service métier.

**TP :** fournir la configuration avec `ZLayer.succeed`, retirer temporairement `provide`, puis observer l'erreur de compilation.

## Jour 3 - Ressources et Scope

**Cours :** `ZIO.acquireRelease` et `ZIO.scoped` bornent la durée de vie d'une ressource.

**TP :** lire un audit en mémoire depuis le batch de référence, observer `audit acquire`, `audit release`, puis la fermeture en cas d'erreur volontaire.

## Jour 4 - Parallèle borné et timeout

**Cours :** les fibers permettent de lancer des effets en parallèle avec une borne locale.

**TP :** comparer `foreach` et `foreachPar`, changer `withParallelism`, puis observer `timeout`.

## Jour 5 - Retry borné

**Cours :** `Schedule` décrit une politique de retry. On retente seulement les erreurs temporaires.

**TP :** brancher `Schedule` sur le type d'erreur du stagiaire, compter les retries, puis vérifier qu'une erreur métier n'est pas retentée.

## Limite volontaire

Pas de projet prêt à copier, pas de vrai client HTTP, pas de repository, pas de circuit breaker industriel, pas de refonte du moteur. La semaine 14 sert à observer ZIO autour d'un terrain déjà connu.
