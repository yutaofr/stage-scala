# Semaine 1 — Fondations Scala et Clearing Engine v0.1

## Objectif

Construire un premier programme Scala 3 qui génère, valide, catégorise et agrège
des transactions, puis prouver son comportement avec des tests.

## Notions à maîtriser

- Projet SBT, compilation, exécution et tests.
- `val`, inférence de type, expressions et interpolation.
- `BigDecimal` pour les montants financiers.
- `if`, pattern matching avec gardes, `for` et `yield`.
- Fonctions, paramètres, lambdas et récursion simple.
- Structure et lecture d'un test ScalaTest.

## Exercices

- [x] J1 — environnement, `val`/`var`, premier calcul et premier test.
- [x] J2 — précision, conversion de devises et intérêts composés.
- [x] J3 — catégories, génération de batch et filtres métier.
- [x] J4 — validation, position nette et recherches récursives.
- [x] J5 — assemblage et démonstration de la version `v0.1`.

## Premier livrable

Un projet `fil-rouge` contenant le code source, les tests et une commande de
démonstration. Le rapport doit montrer le nombre de transactions, les rejets,
les positions par banque et le solde global.

## Critères de validation

- [x] `sbt clean test` termine avec zéro échec.
- [x] Le projet utilise `BigDecimal` pour l'argent hors démonstration de précision.
- [x] Chaque fonction de calcul possède au moins un test pertinent.
- [x] Les cinq catégories de montant et les codes connus/fallback sont testés.
- [x] Les validateurs couvrent montant, IBAN et banque connue.
- [x] Le netting couvre liste vide, cas unique et cas mixte.
- [x] La démonstration affiche les cas créditeur et débiteur.
- [x] Marc explique trois limites qui guideront les semaines suivantes.

## Journal d'exécution

### Environnement

- L'image pédagogique annonce SBT `1.7.1`, Scala `3.2.0` et Java `17.0.4.1`.
- Le projet verrouille SBT `1.10.11` et Scala `3.3.8`, comme les projets récents
  du dépôt.
- Le projet compile et teste aussi dans l'image pédagogique Docker.

### Cycles TDD observés

- J1-J2 : échec de compilation attendu sur quatre API absentes, puis 10 tests verts.
- J3 : échec attendu sur les modules de pipeline ; après correction d'un matcher
  ScalaTest mal choisi, 9 tests verts.
- J4 : échec attendu sur validation, netting et recherche, puis 16 tests verts.
- J5 : échec attendu sur `ClearingEngine`, puis 6 tests d'intégration verts.
- Audit : `Basics.resume` manquait d'un RED direct ; l'implémentation a été
  retirée, le test ajouté et le cycle RED→GREEN rejoué.

### Démonstration vérifiée

La vérification finale du 14/07/2026 exécute 42 tests dans 8 suites, sans échec,
sur Java 21 en local puis Java 17 dans l'image Docker pédagogique.

```text
=== MOTEUR DE COMPENSATION v0.1 ===
Transactions reçues : 6
Valides : 4 | Rejetées : 2
ATH : -140 DH — DÉBITRICE
BOA : 80 DH — CRÉDITRICE
CIH : 60 DH — CRÉDITRICE
Solde net global : 0 DH
```

### Limites identifiées par Marc

1. Un tuple n'explique pas ses champs et permet de confondre sender et receiver.
2. Les codes banque et transaction sont des chaînes non contrôlées à la compilation.
3. Un message d'erreur unique ne permet ni typage ni accumulation des erreurs.

## Validation mentor

**Décision : acceptée.** La vérification finale fraîche conserve zéro échec, la
démo prouve les positions créditrices et débitrices, et le netting global vaut
zéro. S2 partira de cette version ; elle ne remplacera pas le projet par un
starter complet.
