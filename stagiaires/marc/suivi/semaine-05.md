# Semaine 5 — Modèle métier et Clearing Engine v1.0

## Objectif

Remplacer la représentation positionnelle du prototype par un modèle métier
nommé, fermer les états avec des enums et représenter chaque échec de validation
par un ADT exhaustif.

## Notions à maîtriser

- Case class, égalité structurelle, immutabilité, champs nommés et `copy`.
- Enum Scala 3, compagnon, valeur associée et conversion depuis une entrée.
- Trait scellé, types somme et produit, ADT et pattern matching exhaustif.
- Déstructuration d'une erreur métier et accumulation dans une `List`.
- Composition de case classes imbriquées pour un lot et son résultat.
- Frontière entre parser syntaxique, règles métier, netting pur et affichage.

## Exercices

- [x] J1 — `Bank`, `Transaction`, `Account`, génération et netting nommés.
- [x] J2 — enums de statut/type, hiérarchie `ClearingError` et validateur typé.
- [x] J3 — routeur exhaustif, `Suspicious` et reporter d'erreurs.
- [x] J4 — `ClearingBatch`, `ClearingResult`, traitement et description du lot.
- [x] J5 — migration de la chaîne complète et démonstration `v1.0`.

## Premier livrable

Une version `v1.0` qui lit un CSV, construit des `Transaction` nommées, sépare
les transactions valides et invalides, accumule les erreurs métier, calcule les
positions sur les seules transactions valides et affiche un rapport complet.

## Critères de validation

- [x] Les entités métier sont des case classes immuables avec des champs nommés.
- [x] `isHighValue` applique la limite stricte de 50 000 DH.
- [x] `TransactionStatus` couvre quatre états, dont `Suspicious`.
- [x] Un test de compilation négatif interdit un statut `String` arbitraire.
- [x] `TransactionType` couvre `VIR`, `PRE` et `CHQ` sans code magique dans la
  logique métier.
- [x] `ClearingError` couvre les montants, banques, doublons et règles de champ.
- [x] Le routeur et le reporter utilisent des matchs exhaustifs.
- [x] La validation peut conserver plusieurs erreurs pour une transaction.
- [x] Le premier ID est conservé et chaque occurrence suivante est rejetée.
- [x] Les lignes historiques sans ID reçoivent des identifiants distincts.
- [x] `ClearingBatch.id` respecte le contrat `Int` du TP.
- [x] Le netting v1.0 manipule `Transaction` et emploie `foldLeft`.
- [x] Le CSV de seize lignes produit dix valides, quatre invalides métier et
  deux lignes malformées.
- [x] Les dix transactions valides donnent un solde net global nul.
- [x] Les paquets `clearing.model` et `clearing.v10` ne manipulent aucun tuple
  anonyme et n'appellent aucun module v0.x.
- [x] Les tests S1 à S5 passent avec Java 21 et Java 17.
- [x] Le code v1.0 ne contient aucun `var`, `null`, `???`, `while`, collection
  mutable ou `Double`.

## Journal d'exécution

### Cycles TDD observés

- Le modèle a commencé par 22 erreurs de compilation sur les types absents;
  huit tests ont ensuite validé les case classes, enums et résultats nommés.
- L'entrée typée a commencé par dix erreurs sur les modules absents; cinq tests
  ont ensuite validé le parser, le générateur et les quatre routes de statut.
- La validation a commencé par dix erreurs sur l'API absente; sept tests ont
  ensuite validé l'accumulation, les doublons et le reporter exhaustif.
- Le traitement du lot a commencé par onze erreurs sur l'API absente; neuf
  tests ont ensuite validé le netting, les résultats et l'intégration CSV.

### Décisions mentor

La nouvelle chaîne vit dans `clearing.model` et `clearing.v10`. Les prototypes
v0.x restent compilés et testés pour prouver la non-régression, mais v1.0 ne les
appelle pas. Le critère « sans tuple anonyme » s'applique à la chaîne v1.0;
supprimer les anciens exercices effacerait la progression pédagogique.

Les banques de `Transaction` restent des codes `String`, conformément à la
signature du TP. Le validateur vérifie les deux extrémités contre le
référentiel. `Bank` sert aux comptes et au futur enrichissement du référentiel.

`Suspicious` fait partie du modèle final. Marc a d'abord étudié les trois états
du jour 2, puis a ajouté le quatrième état et sa branche de routage comme le
demande l'exercice d'exhaustivité du jour 3.

Le parser conserve `Option`. Les erreurs de syntaxe restent donc seulement
comptées; les erreurs métier, elles, sont précises et cumulables. S6 introduira
`Either` et `Validated` à partir de cette limite observable.

La revue avant jalon a corrigé trois écarts : `ClearingBatch.id` est maintenant
un `Int`, plusieurs lignes à trois colonnes reçoivent des IDs distincts et
`ClearingAppV10` transmet son unique `ValidationSummary` au calcul du résultat.
Le pipeline ne valide donc plus le même lot deux fois.

### Revue de code du vendredi

Les trois points forts présentés par Marc sont :

1. La frontière `clearing.model` / `clearing.v10` rend la migration lisible et
   préserve les prototypes comme preuves de progression.
2. Les enums et le trait scellé déplacent les statuts, types et erreurs vers le
   système de types; les matchs exhaustifs rendent un oubli visible.
3. Le parser, la validation, le netting et le rendu ont des responsabilités
   séparées; un seul `ValidationSummary` alimente le calcul et le rapport.

La zone d'amélioration est le résultat du parser. `Option` indique seulement
qu'une ligne est absente; S6 devra conserver la cause avec `Either`, puis
accumuler plusieurs erreurs indépendantes avec `Validated`.

## Validation mentor

**Décision : acceptée.** La vérification du 14/07/2026 exécute 181 tests dans
27 suites, sans échec, avec Java 21 local puis Java 17 dans l'image Docker
pédagogique. Les deux environnements compilent 37 sources de production et 27
sources de test, puis reproduisent la démonstration v1.0.

L'audit limité à `clearing.model` et `clearing.v10` ne trouve aucun tuple
anonyme, accès positionnel, marqueur interdit ou dépendance vers un module
v0.x. La chaîne produit dix transactions valides, quatre invalides métier, deux
lignes malformées et un solde net global de 0 DH.

### Démonstration v1.0

```text
=== CLEARING ENGINE v1.0 ===
Lignes reçues : 16
Parsées : 14 | Malformées : 2
Valides : 10 | Invalides : 4
ATH : -70 DH — DÉBITRICE
BMCE : -20 DH — DÉBITRICE
BOA : 76 DH — CRÉDITRICE
CIH : 20 DH — CRÉDITRICE
SGMB : -6 DH — DÉBITRICE
Transaction 13 : Montant invalide : -5 DH
Transaction 14 : Banque inconnue : UNKNOWN
Transaction 15 : Validation receiver : doit être différente de la banque source
Transaction 10 : Transaction dupliquée
Résultat : Échec partiel (4 erreurs)
Solde net global : 0 DH
```
