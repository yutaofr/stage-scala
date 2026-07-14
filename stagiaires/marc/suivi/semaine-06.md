# Semaine 6 — Validation avancée et Clearing Engine v1.1

## Objectif

Rendre la validation du fil rouge plus expressive et plus robuste : classer les
erreurs par niveau, accumuler toutes les erreurs d'une ligne, reconnaître des
cas métier avec guards et extracteurs, puis produire un rapport v1.1 qui ne
plante ni sur un fichier vide ni sur une lecture impossible.

## Notions à maîtriser

- Traits scellés imbriqués et pattern matching exhaustif.
- Distinction entre erreur de fichier, erreur de ligne et erreur système.
- Guards pour exprimer une règle métier dans un `match`.
- Extracteurs personnalisés avec `unapply`.
- For-comprehension sur `Option` et filtrage par pattern.
- Companion object comme factory et constructeur privé.
- Séparation entre parsing structurel, validation métier, netting et rendu.
- Accumulation immutable de plusieurs erreurs indépendantes.

## Exercices

- [x] J1 — Hiérarchie `ClearingError`, dix codes ISO 20022, filtrage des
  `LineError` et reporter imbriqué exhaustif.
- [x] J2 — Guards montant/IBAN, `FraudDetector`, `InternationalTx`, extracteur
  d'IBAN et frais de change de 2 %.
- [x] J3 — Extraction par pattern dans un `for`, pipeline `Option`, nettoyage
  de transferts et adaptateur pédagogique transaction/erreurs.
- [x] J4 — Factories `Currency`, `Iban` et `Transaction.fromCsv`; constructeur
  d'IBAN inaccessible hors du companion.
- [x] J5 — `ClearingAppV11`, rapport 45/3/12, fichier vide, lot 100 % invalide
  et erreur de lecture.

## Premier livrable

Une version `v1.1` qui lit le CSV à huit colonnes, construit des candidats
typés, accumule les erreurs métier par numéro de ligne, signale les transactions
suspectes acceptées, calcule les positions sur les seuls succès et affiche un
rapport déterministe.

## Critères de validation

- [x] `ClearingError` distingue `HighLevelError`, `LineError` et `SystemError`.
- [x] `LineError` distingue validation et erreur métier.
- [x] Les dix codes ISO 20022 demandés sont fermés dans un enum.
- [x] Le reporter traite exhaustivement chaque branche imbriquée sans fallback.
- [x] Les guards reconnaissent montant nul, montant négatif, IBAN identiques et
  montant exact de 9 999,99 DH.
- [x] `FraudDetector` retourne une raison pour un montant supérieur à un million
  ou un IBAN source commençant par `XX`.
- [x] `InternationalTx` reconnaît une source non marocaine et le laboratoire
  applique 2 % de frais depuis les lignes CSV.
- [x] Le validateur utilise `Iban.unapply` pour relier le segment bancaire de
  l'IBAN aux banques source et destination.
- [x] Les exercices de for-comprehension enchaînent les trois fonctions `Option`
  et filtrent les sous-types d'erreur par pattern.
- [x] `Currency.fromString` est insensible à la casse.
- [x] `Iban.apply` accepte uniquement 24 caractères commençant par `MA`; son
  constructeur direct est refusé à la compilation.
- [x] `Transaction.fromCsv` rejette mauvaise arité, types invalides, type de
  transaction inconnu et devise inconnue.
- [x] Plusieurs erreurs de la même ligne sont conservées et rendues ensemble.
- [x] Le fichier de démonstration produit 45 succès, trois avertissements et
  12 rejets sur 57 lignes.
- [x] Le netting global des transactions acceptées vaut zéro.
- [x] Un fichier vide produit `EmptyFile` sans exception.
- [x] Un lot 100 % invalide ne produit aucune position.
- [x] Une lecture impossible produit `FileReadFailure` sans exception.
- [x] Tous les tests S1 à S6 passent avec Java 21 et Java 17.
- [x] Le code v1.1 passe l'audit fonctionnel et la revue mentor.

## Journal d'exécution

### Cycles TDD observés

- La hiérarchie d'erreurs a commencé par 21 erreurs de compilation sur les
  types absents. Les tests ciblés ont ensuite validé les catégories, les codes
  ISO, les requêtes et chaque branche du reporter.
- Les factories ont commencé par 18 erreurs de compilation. La première passe
  verte a aussi révélé le renommage attendu de l'ancienne erreur S5; la suite
  v1.0 a été adaptée sans changer son comportement.
- Les guards et extracteurs ont commencé par 13 erreurs de compilation, puis
  huit tests ont validé les classifications, les erreurs cumulées et les frais.
- Les for-comprehensions ont commencé par 13 erreurs de compilation. Le
  compilateur a imposé la syntaxe `for case` pour les patterns réfutables; six
  tests ont ensuite couvert les pipelines pédagogiques.
- L'intégration v1.1 a commencé par huit erreurs sur l'application absente.
  Six tests sur sept ont réussi au premier passage; le dernier a conduit à
  normaliser `0.00` en `0` dans le rapport, sans changer le calcul.
- La revue mentor a fait ajouter trois cycles ciblés : l'extracteur d'IBAN dans
  la validation, les frais internationaux depuis le CSV et le préfixe
  `[BLOCKING]` pour `EmptyFile`. Chaque test a échoué pour la raison attendue
  avant sa correction.

### Décisions mentor

Le support réel de S6 ne porte pas sur `Either` ou `Validated`, contrairement à
une ancienne ligne de route. Le programme suit les cinq fichiers de cours de la
semaine : ADT imbriqués, guards, extracteurs, for-comprehensions et factories.
Les documents S4/S5 et la table de suivi ont été corrigés pour ne pas propager
l'ancienne prévision.

`Iban` représente une valeur dont la validité marocaine est prouvée par la
factory. `Transaction` conserve néanmoins les chaînes d'entrée brutes : le
validateur doit pouvoir expliquer un IBAN `XX` ou international avant son
rejet. Le parsing contrôle donc la structure; la validation contrôle le métier.
Le pattern `Iban(_, bankSegment, _)` relie aussi l'IBAN à la banque annoncée.

`InternationalFeePipeline` applique les 2 % demandés à partir du CSV, avant la
validation marocaine. Cette démonstration reste séparée de `ClearingAppV11` :
le moteur principal rejette les IBAN internationaux et ne doit jamais les
inclure dans les positions nettes.

La production utilise `TransactionAssessment`, un résultat nommé. Le tuple
`(Transaction, List[ClearingError])` reste uniquement dans
`BatchValidationLab`, car le TP le demande comme exercice de pattern matching.

Une transaction rejetée ne contribue ni au netting ni aux compteurs de fraude
acceptée. Son identifiant reste toutefois mémorisé : une occurrence ultérieure
du même ID doit être signalée comme doublon, même si la première était invalide.

## Validation mentor

**Décision : acceptée.** La vérification du 14/07/2026 exécute 216 tests dans
32 suites, sans échec, avec Java 21 local puis Java 17 dans l'image Docker
pédagogique. Les deux environnements compilent 42 sources de production et 32
sources de test, puis reproduisent la démonstration v1.1.

L'audit ne trouve aucun `var`, `null`, `???`, `while`, collection mutable ou
`Double` dans `clearing.model` et `clearing.v11`. L'unique type tuple du paquet
v1.1 est l'adaptateur imposé par le TP dans `BatchValidationLab`. La seconde
revue mentor ne conserve aucun point critique, important ou mineur.

### Démonstration v1.1

```text
=== CLEARING ENGINE v1.1 ===
[INPUT] 57 lignes reçues.
[SUCCESS] 45 transactions traitées avec succès.
[WARNING] 3 transactions suspectes (Fraud Signal).
[ERROR] 12 lignes ignorées :
[REPORT] Solde net global : 0 DH
```
