# Semaine 3 — Collections et Clearing Engine v0.3

## Objectif

Transformer le moteur en pipeline fonctionnel fondé sur des collections
immuables, puis vérifier sa sûreté et son comportement sur 100 000
transactions.

## Notions à maîtriser

- `List`, `Map`, `Set`, `Vector` et choix d'une structure adaptée.
- `groupBy`, `map`, `filter`, `flatMap` et for-comprehension.
- `reduceOption`, `foldLeft`, agrégations et collection vide.
- Pipeline strict, `view`, matérialisation et coût de `sortBy`.
- Complexité d'accès d'une `List` et d'un `Vector`.
- Récursion non terminale, `StackOverflowError` et parcours sûr.

## Exercices

- [x] J1 — répertoire de banques, IBANs uniques et index par émetteur.
- [x] J2 — enrichissement, audit, filtrage et éclatement de lots.
- [x] J3 — somme, moyenne, statistiques et netting avec `foldLeft`.
- [x] J4 — comparaison `List`/`Vector`, `view` et stress test à 100 000.
- [x] J5 — intégration CSV et démonstration du moteur `v0.3`.

## Premier livrable

Une version `v0.3` qui lit un CSV, construit les tuples avec
`Transaction.apply`, filtre le flux, calcule les positions avec `foldLeft` et
affiche les banques dans l'ordre alphabétique.

## Critères de validation

- [x] Le répertoire contient cinq banques et gère un code inconnu.
- [x] Les exercices emploient `Set`, `groupBy`, `map`, `filter` et `flatMap`.
- [x] `reduceOption` sécurise le cas vide et `foldLeft` calcule le netting.
- [x] L'émetteur est débité, le bénéficiaire crédité et le total vaut zéro.
- [x] `List` et `Vector` produisent les mêmes sommes et positions.
- [x] Les pipelines strict et `view` produisent le même résultat.
- [x] Le stress test traite 100 000 transactions.
- [x] La somme d'un million d'entiers passe avec `foldLeft`.
- [x] Le scénario CSV de dix transactions produit les positions attendues.
- [x] Les tests S1, S2 et S3 passent sur Java 21 et Java 17.
- [x] Le code de production ne contient ni `var`, ni `null`, ni `???`.

## Journal d'exécution

### Cycles TDD observés

- Les cinq nouveaux modules ont commencé par 30 erreurs de compilation ciblant
  les API absentes.
- Les tests du répertoire, du pipeline, du netting, de la performance et de
  l'intégration sont ensuite passés au vert.
- Un test supplémentaire a révélé que le laboratoire convertissait la `List`
  avant de mesurer l'accès indexé. Le laboratoire compare maintenant la vraie
  `List` au `Vector`.
- Les exercices manquants du TP — audit supérieur à 5 000 DH, `reduceOption`,
  parcours complet et récursion non terminale — ont suivi leur propre cycle
  RED puis GREEN.

### Incidents et apprentissages

Scala 3.3 ne fournit pas `sortBy` sur une `View` générique. Le pipeline reste
paresseux jusqu'à la dernière étape, puis appelle `toVector` avant le tri. Marc
retient qu'un tri global doit connaître tous les éléments et force donc la
matérialisation.

Un test d'intégration utilisait `vir` en minuscules. Le parser S2 impose une
Regex en majuscules et rejette la ligne avant le pipeline S3. Le test a été
corrigé pour préserver ce contrat déjà validé.

### Mesures locales Java 21

Mesures du 14/07/2026 sur 100 000 transactions :

```text
Netting List : 31 ms
Netting Vector : 16 ms
Parcours somme List : 9 ms
Parcours somme Vector : 7 ms
Accès tête List : 1 ms
Accès fin List : 254 ms
Accès médian List : 126 ms
Accès médian Vector : 0 ms
Pipeline strict : 49 ms
Pipeline view : 99 ms
Sommes identiques : true
Résultats netting identiques : true
Solde global : 0.00 DH
```

Ces valeurs décrivent une exécution, pas une garantie. `view` évite les
collections intermédiaires, mais le tri et les coûts de JVM peuvent rendre ce
pipeline plus lent. La variante Java 17 Docker conserve les mêmes résultats et
un solde nul.

La démonstration de pile observe un `StackOverflowError` avec la récursion non
terminale sur un million d'entiers. `foldLeft` retourne ensuite
`500000500000` sans débordement de pile.

### Démonstration de dix transactions

```text
=== CLEARING ENGINE v0.3 ===
Transactions valides : 10
ATH : -70 DH — DÉBITRICE
BMCE : -20 DH — DÉBITRICE
BOA : 76 DH — CRÉDITRICE
CIH : 20 DH — CRÉDITRICE
SGMB : -6 DH — DÉBITRICE
Solde net global : 0 DH
```

## Validation mentor

**Décision : acceptée.** La vérification finale du 14/07/2026 exécute 105 tests
dans 17 suites, sans échec, avec Java 21 local puis Java 17 dans l'image Docker
pédagogique. Les deux environnements traitent le vrai CSV de dix transactions.
L'audit ne trouve aucun `var`, `null`, `???` ou collection mutable ; `Double`
reste confiné à `PrecisionDemo` pour l'exercice de précision de S1.
