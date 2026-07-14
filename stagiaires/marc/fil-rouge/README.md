# Clearing Engine de Marc — v0.4

Ce projet est le fil rouge construit par Marc pendant son stage. La version
`v0.4` clôt le premier mois du stage. Cette version conserve les fondations S1
à S3 et ajoute des règles configurables, la composition avec `Option` et des
algorithmes récursifs terminaux.

## Prérequis

- Java 17 ou 21.
- SBT 1.10.11, ou Docker.

## Lancer les tests

```bash
sbt clean test
```

Dans l'image pédagogique Docker :

```bash
docker run --rm -v "$PWD:/app" -w /app \
  sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
  sbt test
```

## Lancer la démonstration

```bash
sbt run
```

Un autre fichier peut être fourni explicitement :

```bash
sbt "run chemin/transactions.csv"
```

Le format final contient cinq colonnes :

```text
id,sender,receiver,amount,type
```

Le parser accepte aussi le format pédagogique à trois colonnes. `MainV04`
compose parsing, validation et enrichissement avec `Option`. Les règles par
défaut rejettent les montants non positifs ou supérieurs ou égaux à 100 000 DH.
Le workflow rejette aussi les banques inconnues et les virements internes. Pour
chaque transfert retenu, le moteur débite l'émetteur et crédite le bénéficiaire ;
la somme des positions reste nulle.

## Modules de la semaine

- `Basics` : valeurs immuables, expressions et résumé créditeur/débiteur.
- `PrecisionDemo` : comparaison volontaire entre `Double` et `BigDecimal`.
- `CurrencyConverter` et `InterestCalculator` : calculs financiers précis.
- `TransactionCategorizer`, `TransactionGenerator` et `TransactionFilter` :
  pattern matching, génération et transformations.
- `Validator`, `NettingCalculator` et `TransactionSearch` : fonctions
  réutilisables et récursion.
- `ClearingEngine` : assemblage du livrable `v0.1`.

## Modules ajoutés en S2

- `TransactionV2` : tuple `(id, sender, receiver, amount, type)`.
- `ReportGenerator` : flux bilatéraux, top, matrice et rendu tabulaire.
- `TransactionRouter` : matching sur tuples, valeurs, types et gardes.
- `CsvParser` : formats 3/5 colonnes, Regex et isolation des erreurs.
- `Transaction` : factory utilisant la syntaxe `Transaction(line)`.
- `ClearingProcessor` : contrat abstrait et méthode concrète `process`.
- `SimpleClearingProcessor with Logger` : lecture, validation, calcul et rapport.

## Modules ajoutés en S3

- `ReferenceData` : `Map` de banques, IBANs uniques avec `Set` et indexation par
  `groupBy`.
- `TransactionPipelineV3` : enrichissement, audit, `map`, `filter` et
  `flatMap` sur des lots.
- `NettingCalculator` : somme, moyenne, statistiques et positions nettes avec
  `reduceOption` et `foldLeft`.
- `PerformanceLab` : comparaison `List`/`Vector`, pipeline strict/`view` et
  démonstration de sûreté de pile.
- `MainV03` : lecture, parsing, filtrage, calcul et rapport trié de bout en bout.

## Modules ajoutés en S4

- `ValidationRules` : HOF, alias `Rule`, `forall`, `exists` et configurateur de
  frais curryfié.
- `OptionTools` : recherches sûres, chaînage avec `flatMap`, nettoyage et
  aplatissement d'Options.
- `TransactionWorkflowV4` : for-comprehension sur parsing, validation et
  enrichissement, puis collecte avec `view`.
- `BankingRecursion` : historique de solde, détection d'incident, recherche de
  signature et Fibonacci avec `@tailrec`.
- `MainV04` : règles injectées, netting `foldLeft`, compteurs d'acceptation et
  rapport trié.

## Chemin d'une donnée

```text
CSV -> List[String] -> view -> Transaction.apply
    -> Option validation -> Option enrichissement -> flatten
    -> List[Transaction] -> NettingCalculator.calculate
    -> Map[banque, position] -> rapport trié
```

Une ligne malformée, une règle refusée ou une banque absente produit `None`.
`flatten` retire ces absences et le rapport compte les lignes ignorées. S6
remplacera cette information binaire par des erreurs métier typées.

## Lancer les laboratoires S3

```bash
sbt "runMain clearing.runPerformanceLab 100000"
sbt "runMain clearing.runStackSafetyLab 1000000"
```

Les temps dépendent de la machine et de la JVM. Une `view` évite les collections
intermédiaires, mais une opération globale comme `sortBy` force la matérialisation
du flux ; elle ne garantit donc pas un temps inférieur.

## Ce que Marc retient

1. `val` interdit la réaffectation ; `var` autorise un état modifiable. Marc
   préfère `val` pour rendre le calcul plus facile à raisonner et à tester.
2. `Double` ne représente pas exactement des décimales comme `0.1`. Les montants
   utilisent `BigDecimal`, et le générateur produit directement des centimes.
3. En Scala, `if`, `match`, `for` et les blocs produisent des valeurs.
4. Une fonction de calcul retourne une valeur ; le bord du programme l'affiche.
5. Un test décrit un comportement attendu et protège les prochains refactorings.

## Limites volontaires de v0.4

- Les transferts restent des tuples : l'ordre des champs peut être inversé.
- Les banques et catégories restent des `String` : une faute de frappe compile.
- `Option` distingue succès et absence, mais ne conserve pas la cause d'un
  rejet et ne permet pas d'accumuler plusieurs erreurs.

Ces limites alimentent directement les semaines S5, S6 et S10.

Les transformations du premier mois rendent les limites des tuples visibles ;
leur remplacement reste volontairement réservé à la modélisation ADT de S5.
