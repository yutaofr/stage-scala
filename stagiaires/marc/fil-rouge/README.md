# Clearing Engine de Marc — v1.0

Ce projet est le fil rouge construit par Marc pendant son stage. La version
`v1.0` ouvre le deuxième mois du stage. Cette version conserve les prototypes
S1 à S4 pour la non-régression et ajoute une nouvelle chaîne fondée sur des case
classes, des enums et une hiérarchie d'erreurs métier.

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
sbt "run chemin/transactions-v10.csv"
```

Le format final contient cinq colonnes :

```text
id,sender,receiver,amount,type
```

Le parser accepte aussi le format pédagogique à trois colonnes.
`ClearingAppV10` sépare les lignes malformées, les transactions invalides et
les transactions valides. Les règles rejettent les montants non positifs, les
banques inconnues, les virements internes et les identifiants dupliqués. Pour
chaque transfert valide, le moteur débite l'émetteur et crédite le bénéficiaire;
la somme des positions reste nulle.

Comme le format à trois colonnes ne porte pas d'identifiant, `parseLines` lui
attribue des identifiants distincts après le plus grand ID explicite du fichier.
Plusieurs lignes historiques ne deviennent donc pas de faux doublons.

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

## Modules ajoutés en S5

- `clearing.model.Domain` : case classes `Bank`, `Account`, `Transaction`,
  `ClearingBatch`, `ClearingResult` et résultats de validation nommés.
- `TransactionStatus` et `TransactionType` : états et types fermés, contrôlés
  par le compilateur.
- `ClearingError` : erreurs `InvalidAmount`, `UnknownBank`,
  `DuplicateTransaction` et `ValidationError`.
- `CsvParserV10` et `TransactionGeneratorV10` : création de transactions
  métier sans tuple positionnel.
- `TransactionValidator` et `ErrorReporter` : accumulation et rendu exhaustif
  des erreurs typées.
- `NettingCalculatorV10`, `BatchProcessor` et `ClearingAppV10` : traitement
  complet du lot et rapport v1.0.

## Chemin d'une donnée

```text
CSV -> List[String] -> CsvParserV10 -> List[Transaction]
    -> ValidationSummary(valid, invalid)
    -> ClearingBatch -> ClearingResult
    -> Map[banque, position] + erreurs typées -> rapport trié
```

Une erreur de règle produit un sous-type de `ClearingError`. Une ligne que le
parser ne peut pas construire produit encore `None`; le rapport la compte sans
en conserver la cause. S6 remplacera ce dernier manque par `Either` et
`Validated`.

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
6. Une case class donne un nom et un type à chaque champ; `copy` crée une
   nouvelle valeur sans mutation.
7. Un enum ferme la liste des états possibles; un match exhaustif signale les
   cas oubliés lors de la compilation.
8. Un ADT d'erreur permet d'accumuler et de traiter des échecs métier sans
   comparer des messages libres.

## Limites volontaires de v1.0

- Les codes bancaires restent des `String`; le validateur les relie au
  référentiel, mais le compilateur ne peut pas détecter une faute de frappe.
- Le parser utilise encore `Option`; il compte les lignes malformées sans
  conserver leur cause précise.
- `ClearingError` accumule les erreurs métier dans une `List`; S6 comparera ce
  choix à `Either` et `Validated`.

Les modules v0.x contiennent encore leurs tuples pédagogiques. Le paquet v1.0
ne les appelle pas; ils restent disponibles pour comparer avant et après la
migration.
