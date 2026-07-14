# Clearing Engine de Marc — v2.0

Ce projet est le fil rouge construit par Marc pendant son stage. La version
`v2.0` ouvre le troisième mois du stage. Elle conserve les jalons S1 à S8 pour
la non-régression et ajoute un cœur de clearing déterministe composé uniquement
de fonctions pures. Les fichiers et la console restent dans deux adaptateurs de
bord. Le même cœur fonctionne avec un profil MAD ou EUR.

## Prérequis

- Java 17 ou 21.
- SBT 1.10.11, ou Docker.

## Lancer les tests

```bash
sbt clean test
```

La couverture du cœur pur v2.0 est mesurée et bloquante à 100 % des statements
et des branches :

```bash
sbt clean coverage "testOnly clearing.v20.*" coverageReport
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

La commande traite `transactions-v20.csv` avec le profil MAD. Un autre fichier
peut être fourni explicitement :

```bash
sbt "run chemin/transactions-v20.csv"
```

Le profil EUR réutilise exactement le même pipeline :

```bash
sbt "run --profile EUR transactions-v20.csv"
```

La démonstration connectée v1.3 reste disponible séparément :

```bash
sbt "runMain clearing.v13.runClearingAppV13"
```

La démonstration déterministe S7 de 100 000 transactions reste disponible :

```bash
sbt "runMain clearing.v12.runClearingAppV12 --generated 100000"
```

Le laboratoire S7 mesure un million de transactions :

```bash
sbt "runMain clearing.v12.runScalabilityLab 1000000"
```

Le format v1.1 contient huit colonnes :

```text
id,sender,receiver,sourceIban,destinationIban,amount,type,currency
```

`Transaction.fromCsv` contrôle la structure, les types, le type de transaction
et la devise. `ClearingAppV11` accumule ensuite toutes les erreurs métier d'une
même ligne. Une transaction acceptée prend le statut `Validated` avant d'entrer
dans v1.2. `ClearingAppV12` calcule alors les règlements bilatéraux, les
positions N-à-N, les lots et les fenêtres suspectes. Chaque lot et le calcul
global restent équilibrés.

Le fichier `transactions-v13.csv` illustre les trois devises, une alerte et
deux lignes invalides. Les anciens jeux restent disponibles pour la
non-régression, mais ne font pas partie du contrat v1.3.

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

## Modules ajoutés en S6

- `ClearingError` : catégories `HighLevelError`, `LineError` et `SystemError`,
  puis erreurs de validation et métier imbriquées.
- `Currency`, `Iban` et `Transaction.fromCsv` : companions qui contrôlent la
  construction des entrées v1.1.
- `AdvancedTransactionValidator` : guards, utilisation de l'extracteur d'IBAN,
  accumulation des erreurs et signaux de fraude sans interrompre la validation
  au premier échec.
- `FraudDetector` et `InternationalTx` : extracteurs personnalisés; le
  laboratoire de change applique 2 % de frais aux transactions internationales.
- `ErrorQueries`, `FeePipeline`, `CleanTransferPipeline`,
  `InternationalFeePipeline` et `BatchValidationLab` : exercices de
  for-comprehension, filtrage par pattern et frais internationaux depuis le CSV.
- `ClearingAppV11` : orchestration robuste, numéros de ligne, rapport complet,
  fichier vide, lecture impossible et lot composé uniquement d'erreurs.

## Modules ajoutés en S7

- `BankPair` et `BilateralNetting` : indexation des flux par paire, agrégation
  avec `groupBy` et `view.mapValues`, consultation d'un duo et règlement net.
- `MultilateralNetting` : positions N-à-N immuables avec `foldLeft` et
  `updatedWith`, adaptateurs `String`/`Bank` et ordre débiteur-créditeur.
- `FlowSegmentation` : séparation des statuts avec `partition`, traitement par
  lots avec `grouped`, fusion des positions et fenêtres de fraude avec
  `sliding`.
- `BusinessReporter` : volume validé, banque la plus active, rapports
  bilatéral et multilatéral, séparation des logs financiers et techniques.
- `ScalabilityLab` : comparaison `List`/`Vector`, pipeline strict/`view` et
  calcul CPU séquentiel/parallèle, avec égalité fonctionnelle vérifiée.
- `ClearingAppV12` : réutilisation de la validation v1.1, démonstration
  déterministe de 100 000 transactions et rapport v1.2 complet.

## Modules ajoutés en S8

- `SecurityUtils`, `BankTime` et `SecureBatchFactory` : SHA-256 via
  `MessageDigest`, UUID Java, `ZonedDateTime`, zone marocaine et horloge
  injectable. Le rapport ne rend jamais un IBAN brut.
- `LegacyJavaMock.java` et `JavaCollectionAdapters` : vraies `ArrayList` et
  `HashMap`, vue `asScala`, copie immutable, `asJava` et conversion stable de
  `Double` vers `BigDecimal`; les `null` Java sont filtrés avec `Option` à la
  frontière.
- `LocalExchangeRateServer` et `HttpExchangeRateService` : échange HTTP local
  avec le client et le serveur du JDK, statut contrôlé et absence de dépendance
  Internet.
- `CurrencyConversion` : un appel par devise distincte, cache local,
  taux strictement positif, conversion MAD à deux décimales et rejet limité à
  la devise sans taux valide.
- `BankRepository.java`, `SpringTransactionValidator` et `ClearingService` :
  stéréotypes Spring, injection constructeur, conversion unique du repository
  Java et assemblage manuel du graphe.
- `ClearingAppV13` : validation v1.1, repository, change, journal sécurisé,
  règlements v1.2 et scénario contrôlé de panne USD.

## Modules ajoutés en S9

- `DataCleaner` : nettoyage d'IBAN, arrondi financier, `Either` pédagogique,
  anonymisation et exemples explicites de `andThen` et `compose`.
- `CurriedRules` et `PureEngineProfiles` : limites strictes, frais configurés
  par application partielle et profils MAD/EUR injectés sans état global.
- `PureDomain` : états intermédiaires nommés qui conservent numéros de ligne,
  ordre des transactions et rejets sans exposer les IBAN bruts.
- `PureNettingCalculator` : calcul immutable avec `foldLeft` et `updatedWith`;
  les frais restent séparés du principal de règlement.
- `PureClearingEngine` : cinq fonctions d'étape assemblées avec `andThen`, de
  la chaîne CSV au rapport déterministe.
- `PureClearingRenderer` : maps triées, hashes seulement et octets stables.
- `IOBridge` et `ClearingReporter` : lecture et affichage isolés aux bords;
  aucun effet de bord ne se trouve dans le cœur.
- `sbt-scoverage` : gate limité aux six fichiers du cœur pur, avec seuils
  statement et branch fixés à 100 %.

## Chemin d'une donnée

```text
fichier -> IOBridge -> chaîne CSV
        -> splitLines -> parse -> validate -> prepare -> calculate
        -> PureClearingReport -> rendu déterministe -> ClearingReporter
```

Le cœur v2.0 reçoit une chaîne et une configuration immutable. Il retourne un
rapport sans lire, afficher, interroger le réseau, demander l'heure ou créer un
UUID. Une ligne échouée devient un `PureRejection`; les autres sont nettoyées,
validées, converties, anonymisées puis compensées. Les frais sont rapportés à
part et ne modifient pas le principal crédité au bénéficiaire.

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
9. Une factory privée peut garantir qu'un `Iban` construit est valide, tandis
   que le candidat brut reste observable par le validateur.
10. Un extracteur nomme une règle de classification et rend le pattern matching
    lisible sans cacher le résultat métier.
11. `groupBy` construit les groupes, tandis que `view.mapValues` transforme
    leurs valeurs sans reconstruire immédiatement une `Map` intermédiaire.
12. `foldLeft` et `updatedWith` permettent de calculer les positions N-à-N sans
    état mutable; la somme nulle constitue l'invariant central.
13. `grouped` découpe un flux en lots indépendants; fusionner leurs positions
    doit redonner le calcul global.
14. `sliding` observe des fenêtres qui se chevauchent. Une fenêtre est signalée
    seulement lorsque son total dépasse strictement le seuil.
15. Un benchmark compare les résultats avant les durées. Un temps isolé dépend
    de la JVM et de la machine; il ne constitue pas un test fonctionnel.
16. `asScala` peut créer une vue liée à la collection Java; `.toList` crée un
    instantané immutable qui isole le cœur métier.
17. Une frontière HTTP retourne `Option` lorsque l'absence de taux est un échec
    attendu. Le batch continue avec les devises disponibles.
18. Une valeur `Clock` et un fournisseur d'UUID injectés rendent les tests
    déterministes sans remplacer les APIs Java en production.
19. Une annotation Spring décrit le rôle d'un composant. L'injection par
    constructeur rend ses dépendances explicites, même lors d'un assemblage
    manuel.
20. Le netting ne doit jamais additionner des monnaies différentes. La
    conversion ou le rejet ciblé précède donc tous les calculs v1.2.
21. Les collections Java peuvent contenir `null`. La frontière les transforme
    en `Option` et les filtre avant d'appeler les fonctions Scala.
22. Une fonction pure retourne la même valeur pour les mêmes arguments et ne
    modifie aucun état observable; elle se teste sans infrastructure.
23. `andThen` suit l'ordre de lecture, tandis que `compose` commence par la
    fonction placée à droite.
24. Le currying fixe une partie de la configuration une seule fois et produit
    une nouvelle fonction spécialisée, par exemple pour une limite ou un taux.
25. Un log pur est une chaîne retournée. Seul l'adaptateur de bord décide de
    l'afficher.
26. Un rapport déterministe exclut l'heure et l'UUID, trie les maps avant le
    rendu et conserve l'ordre d'entrée des listes.

## Limites volontaires de v1.3 et v2.0

- V2.0 accumule explicitement succès et rejets entre les étapes. La propagation
  de l'`Either` avec `flatMap` sera le sujet de S10.
- Les profils MAD/EUR sont des valeurs locales déterministes. V2.0 ne contacte
  volontairement ni fournisseur de taux, ni Kafka, ni Cassandra.
- Les frais sont calculés et rapportés, mais ne participent pas au principal de
  règlement. La comptabilisation complète des commissions reste hors S9.

- Les codes bancaires restent des `String`; le validateur les relie au
  référentiel, mais le compilateur ne peut pas détecter une faute de frappe.
- Le parser utilise encore `Option`; `MalformedCsv` conserve la ligne brute,
  mais ne distingue pas encore chaque cause syntaxique.
- Les IBAN restent des chaînes dans le candidat `Transaction` afin que le
  validateur puisse expliquer les entrées invalides; seules les valeurs passées
  par `Iban.apply` portent la garantie de validité.
- La conversion prend trois taux instantanés et ne gère ni date de valeur, ni
  spread, ni arrondi propre à chaque paire de devises.
- `InternationalFeePipeline` démontre les frais de 2 % demandés par le TP. La
  chaîne de clearing principale reste marocaine : elle explique puis rejette un
  IBAN international avant le netting.
- Le petit JSON HTTP est extrait sans bibliothèque dédiée. Une structure JSON
  riche et un décodage typé arriveront avec Circe au mois 3.
- Les règlements produits sont des instructions calculées. V1.2 ne gère ni
  frais, ni collatéral, ni liquidité, ni finalité de paiement.
- L'identifiant déterministe sert d'ordre temporel dans le laboratoire de
  fenêtres. Un horodatage métier explicite arrivera dans une version ultérieure.
- Les mesures `List`/`Vector`, strict/`view` et séquentiel/parallèle décrivent
  l'environnement d'exécution; aucune variante n'est déclarée toujours plus
  rapide.
- Les annotations Spring sont réelles, mais la démo assemble les objets à la
  main. Elle ne démarre pas encore un contexte Spring ou Spring Boot.
- Le `BankRepository` reste en mémoire et le serveur de taux reste local. Les
  mois suivants introduiront Cassandra, Kafka et les services conteneurisés.

Les modules v0.x contiennent encore leurs tuples pédagogiques. Le paquet v1.3
réutilise `BankPair` en production; l'adaptateur `(String, String)` reste exposé
par l'exercice bilatéral parce que le TP demande cette forme exacte.
