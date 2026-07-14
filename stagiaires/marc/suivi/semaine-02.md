# Semaine 2 — Entrées structurées et Clearing Engine v0.2

## Objectif

Lire des transactions CSV, les structurer dans un tuple riche, router les cas
métier et calculer les positions derrière un contrat modulaire.

## Notions à maîtriser

- Tuple riche, déstructuration et portée locale.
- For-comprehension à plusieurs générateurs et avec gardes.
- Pattern matching sur valeurs, tuples, wildcards et gardes.
- Nettoyage de chaînes, `split`, `trim` et Regex.
- `trait`, méthode abstraite, méthode concrète et mix-in.
- `object`, companion/factory et méthode `apply`.

## Exercices

- [x] J1 — transaction 5 champs, rapport bilatéral, top et matrice.
- [x] J2 — validateur par match, routeur et statistiques.
- [x] J3 — parser CSV résilient et tests de lignes invalides.
- [x] J4 — `ClearingProcessor`, implémentation simple et `Logger`.
- [x] J5 — factory `Transaction`, vrai fichier CSV et moteur `v0.2`.

## Premier livrable

Une version `v0.2` qui lit `transactions.csv`, ignore les lignes malformées avec
un message explicite, calcule les positions nettes et affiche un rapport.

## Critères de validation

- [x] Un batch de taille `n` contient exactement `n` IDs ordonnés.
- [x] Sender et receiver diffèrent dans les transactions générées.
- [x] Le top est trié par montant absolu décroissant.
- [x] La matrice bilatérale est antisymétrique.
- [x] Chaque branche du routeur possède un test.
- [x] Les formats CSV 3 et 5 colonnes sont acceptés.
- [x] Ligne vide, colonnes et nombres invalides ne font pas crasher le moteur.
- [x] Le processor implémente réellement le trait et produit des logs.
- [x] Un vrai fichier de test donne les positions calculées manuellement.
- [x] Les tests S1 et S2 passent en local et dans Docker.

## Journal d'exécution

### Cycles TDD observés

- J1-J2 : API absentes en RED, puis génération, rapports, matrice, routing et
  validator en GREEN.
- J3 : parser et factory absents en RED, puis 9 tests de parsing en GREEN.
- J4-J5 : traits, logger et orchestration absents en RED, puis tests unitaires et
  intégration sur un vrai fichier en GREEN.
- Audit TP : ajout test-first du rendu de matrice, du rapport statistique et du
  matching par type.

### Incident découvert pendant la démo

La première exécution sans argument a échoué avec
`Illegal command line: more arguments expected`. L'exécution avec chemin
explicite fonctionnait : le défaut Scala du paramètre `String` n'était pas repris
par le parseur CLI de `@main`. Un test de sélection du fichier a été écrit en
RED ; l'entrée utilise maintenant `String*` et choisit `transactions.csv` quand
la liste est vide.

### Résultat métier attendu

```text
ATH : -140 DH — DÉBITRICE
BOA : 80 DH — CRÉDITRICE
CIH : 60 DH — CRÉDITRICE
Solde net global : 0 DH
```

## Validation mentor

**Décision : acceptée.** La vérification finale du 14/07/2026 exécute 75 tests
dans 12 suites, sans échec, sur Java 21 en local puis Java 17 dans l'image
Docker pédagogique. Les deux environnements lancent également la démo sans
argument, signalent la ligne malformée, calculent les trois positions attendues
et conservent un solde global nul. L'audit ne trouve aucun `???`, `var` ou
`null`; `Double` reste confiné à `PrecisionDemo`.
