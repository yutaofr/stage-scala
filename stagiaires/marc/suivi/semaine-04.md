# Semaine 4 — Sécurité fonctionnelle et Clearing Engine v0.4

## Objectif

Clore le premier mois avec un moteur configurable qui compose les absences par
`Option`, injecte ses règles par HOF et exécute ses recherches récursives sans
débordement de pile.

## Notions à maîtriser

- Fonction d'ordre supérieur et fonction passée en paramètre.
- Currying et configuration partielle.
- Alias de fonction, `forall` et `exists`.
- `Option`, `Some`, `None`, `getOrElse`, `map`, `flatMap` et `flatten`.
- Pattern matching et for-comprehension sur `Option`.
- Récursion terminale, accumulateur, arrêt précoce et `@tailrec`.
- Séparation entre calcul pur et effets de lecture ou d'affichage.

## Exercices

- [x] J1 — filtres HOF, frais curryfiés et moteur de règles.
- [x] J2 — répertoire optionnel, chaîne IBAN-utilisateur et entrées propres.
- [x] J3 — change sûr, parser résilient et composition du workflow.
- [x] J4 — historique, incident, signature et Fibonacci avec `@tailrec`.
- [x] J5 — intégration et démonstration du prototype `v0.4`.

## Premier livrable

Une version `v0.4` qui lit un CSV mixte, compose parsing, validation et
enrichissement avec `Option`, applique les règles reçues en paramètre, calcule
les positions et compte les lignes ignorées.

## Critères de validation

- [x] Les trois filtres du TP reçoivent leur critère comme fonction.
- [x] Les frais ATH à 1 % et génériques à 2 % utilisent une fonction curryfiée.
- [x] `validateAll` emploie `forall` et `validateAny` emploie `exists`.
- [x] Banque, IBAN, utilisateur et devise absents retournent `None`.
- [x] Le workflow compose trois étapes dans une for-comprehension.
- [x] `view`, `map` et `flatten` collectent les transactions acceptées.
- [x] Les quatre algorithmes récursifs portent `@tailrec`.
- [x] L'historique traite 100 000 montants sans `StackOverflowError`.
- [x] Le seuil d'incident accepte `-500` et rejette le premier solde inférieur.
- [x] Le CSV de seize lignes produit dix acceptations et six rejets.
- [x] Une configuration sans seuil d'audit accepte une onzième transaction.
- [x] Le netting reste globalement nul dans les deux configurations.
- [x] Les tests S1 à S4 passent avec Java 21 et Java 17.
- [x] Le code ne contient aucun `var`, `null`, `???`, `while` ou collection
  mutable.

## Journal d'exécution

### Cycles TDD observés

- HOF, currying et exercices Option ont commencé par 31 erreurs de compilation,
  puis 20 tests ont passé.
- Le workflow Option a commencé par 14 erreurs sur l'API absente, puis ses neuf
  tests et les neuf tests du parser historique ont passé ensemble.
- La récursion bancaire a commencé par 12 erreurs de compilation, puis dix tests
  ont passé, dont un historique de 100 000 éléments.
- `MainV04` a commencé par cinq erreurs ciblées, puis cinq tests d'intégration
  ont passé sur le vrai fichier de démonstration.

### Décisions mentor

Le parser de S2 retournait déjà `Option[Transaction]`. S4 ne le réécrit pas : le
nouveau workflow prouve sa composition avec validation et enrichissement. Cette
décision préserve les Regex et les scénarios de non-régression existants.

Le moteur de frais reste un exercice séparé du netting. Ajouter une commission
à une seule position détruirait l'invariant de compensation. Une future règle
métier devra représenter explicitement le compte bénéficiaire des frais.

`Option` compte les lignes ignorées, mais ne conserve pas leur cause. Marc garde
ce manque visible. Le support réel de S6 approfondira d'abord les ADT, guards,
extracteurs et factories; `Either` et `Validated` viendront plus tard.

### Démonstration mensuelle

```text
=== CLEARING ENGINE v0.4 ===
Lignes reçues : 16
Acceptées : 10 | Ignorées : 6
ATH : -70 DH — DÉBITRICE
BMCE : -20 DH — DÉBITRICE
BOA : 76 DH — CRÉDITRICE
CIH : 20 DH — CRÉDITRICE
SGMB : -6 DH — DÉBITRICE
Solde net global : 0 DH
```

Avec seulement la règle `positif`, la transaction ATH vers CIH de 100 000 DH
devient valide. Le moteur accepte onze lignes, ignore cinq lignes et calcule
ATH à `-100070`, CIH à `100020`; le total reste nul.

## Validation mentor

**Décision : acceptée.** La vérification du 14/07/2026 exécute 149 tests dans
22 suites, sans échec, avec Java 21 local puis Java 17 dans l'image Docker
pédagogique. Les deux environnements reproduisent la démonstration mensuelle.
L'audit ne trouve aucun marqueur interdit ; `Double` reste confiné à
`PrecisionDemo`, l'exercice de précision S1.
