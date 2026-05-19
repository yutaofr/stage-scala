# Semaine 10 : Résilience Typée — Either, Try & Railway (5 jours)

## Jour 1 — Le Type Either[L, R]
**Cours (2h)** : Pourquoi `throw` est un "goto" déguisé. `Left(error)` pour l'échec, `Right(value)` pour le succès. Conventions Scala : l'erreur est toujours à gauche.
**TP (4h)** : Remplacer `validateTransaction(...): String` par `validateTransaction(...): Either[ClearingError, Transaction]`. Adapter le pipeline.

## Jour 2 — Chainage avec FlatMap
**Cours (2h)** : `flatMap` sur Either : propagation automatique de l'erreur. For-comprehension sur Either : sequencer les étapes dépendantes. Exemple : `parse → validate → enrich` où chaque étape peut échouer.
**TP (4h)** : Workflow complet : `findAccount(iban) → checkBalance(account, amount) → reserveFunds(account, amount)`. Chaque étape retourne un `Either`. Tester le "happy path" et les scénarios d'erreur.

## Jour 3 — Railway Oriented Programming
**Cours (2h)** : Visualiser le code comme deux rails parallèles. Le rail "succès" avance ; le rail "erreur" court-circuite. Diagramme visuel du pipeline de clearing. Comparaison avec le try/catch impératif.
**TP (4h)** : Dessiner le diagramme railway du moteur de clearing. Implémenter les "switches" qui redirigent vers le rail erreur. Ajouter une fonction `recover` pour retenter en cas d'erreur temporaire.

## Jour 4 — Le Type Try
**Cours (2h)** : `Try` : wrapper pour les exceptions Java legacy. `Success(value)` / `Failure(exception)`. Conversion `Try` → `Either`. Quand utiliser `Try` vs `Either` : `Try` pour les frontières Java, `Either` pour la logique métier.
**TP (4h)** : Wrapper les appels Java (HTTP, parsing de dates) dans des `Try`. Convertir en `Either[ClearingError, T]` avec mapping d'erreurs propre.

## Jour 5 — Clearing Engine v2.1
**Cours (2h)** : Revue : plus aucune exception visible. Toutes les erreurs sont dans le type de retour.
**TP (4h)** : Le moteur v2.1 ne crashe JAMAIS, quel que soit l'input. Tester avec des fichiers CSV corrompus, des IBANs invalides, et des montants absurdes. Rapport d'erreurs propre.
