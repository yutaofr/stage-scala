# Plan d'implémentation — Marc, semaine 3

## Étape 1 — Référentiel et indexation

1. Écrire les tests du répertoire, de la valeur inconnue, des IBANs uniques et
   de l'index par émetteur.
2. Vérifier l'échec des tests.
3. Implémenter `ReferenceData` avec `Map`, `Set` et `groupBy`.
4. Relancer les tests ciblés.

## Étape 2 — Transformations de collections

1. Écrire les tests d'enrichissement, de filtrage, d'aplatissement et de
   normalisation.
2. Vérifier l'échec des tests.
3. Implémenter `TransactionPipelineV3` avec `map`, `filter` et `flatMap`.
4. Relancer les tests ciblés.

## Étape 3 — Netting avec `foldLeft`

1. Écrire les tests de somme, moyenne, positions, statistiques et liste vide.
2. Vérifier l'échec des tests.
3. Étendre `NettingCalculator` avec `foldLeft` et `groupBy`.
4. Faire déléguer `SimpleClearingProcessor.calculate` au nouveau calculateur.
5. Relancer les tests S1 à S3.

## Étape 4 — Performance

1. Écrire les tests d'équivalence entre `List`, `Vector`, pipeline strict et
   pipeline `view`.
2. Vérifier l'échec des tests.
3. Implémenter `PerformanceLab` et son point d'entrée.
4. Exécuter le laboratoire avec 100 000 transactions et consigner les mesures.

## Étape 5 — Intégration v0.3

1. Ajouter un CSV de dix transactions et un test d'intégration rouge.
2. Implémenter `MainV03` et son rapport trié.
3. Mettre la version SBT et le README à `0.3`.
4. Exécuter le scénario nominal et le scénario de rejet.

## Étape 6 — Validation mentor

1. Exécuter `sbt clean test` avec Java 21 local.
2. Exécuter `sbt clean test` dans l'image Java 17 du cours.
3. Lancer `MainV03` localement et dans Docker.
4. Lancer le laboratoire sur 100 000 transactions.
5. Auditer les marqueurs interdits et les collections mutables.
6. Renseigner `suivi/semaine-03.md` avec les preuves observées.
