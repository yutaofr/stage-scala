# Clearing Engine de Marc — v3.2

Ce projet est le fil rouge construit par Marc pendant son stage. La version
`v3.2` conserve le traitement durable Kafka+Cassandra v3.1 et ajoute des logs
JSON corrélés, des métriques Prometheus, des traces OpenTelemetry propagées par
Kafka, un dashboard Grafana et des alertes routées par Alertmanager. Les
semaines Pekko, ZIO et Cats restent hors périmètre; aucune de ces bibliothèques
n'est introduite.

## Prérequis

- Java 17 ou 21.
- SBT 1.10.11.
- Docker avec le plugin Compose.

## Lancer les tests

```bash
sbt clean test
```

Le gate ciblé S12 exécute les lois des abstractions et la propriété de
conservation sur 10 000 batchs :

```bash
sbt "testOnly clearing.v23.*"
```

Le gate ciblé S15 vérifie les contrats Kafka, les fenêtres de crash et 1 000
événements déterministes :

```bash
sbt "testOnly clearing.v30.*"
```

Le gate ciblé S16 vérifie l'état durable, les projections idempotentes, les
requêtes paginées et le scénario de 500 records :

```bash
sbt "testOnly clearing.v31.*"
```

Le gate ciblé S17 vérifie les logs, métriques, traces, dashboards et règles :

```bash
sbt "testOnly clearing.v32.*"
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

## Lancer Kafka v3.0

```bash
docker compose -f docker/docker-compose-kafka.yml up -d --wait
sbt "runMain clearing.v30.runTransactionProducerV30"
sbt "runMain clearing.v30.runKafkaConsumerV30 \
  --group-id marc-v30-demo --max-records 50"
```

Le producer par défaut envoie 50 événements à 10/s. Le gate reproductible de
1 000 événements injecte 100 rejets fonctionnels :

```bash
sbt "runMain clearing.v30.runTransactionProducerV30 \
  --count 1000 --seed 1500 --rate 1000 --reject-every 10"
sbt "runMain clearing.v30.runKafkaConsumerV30 \
  --group-id marc-v30-gate --max-records 1000"
```

Les preuves exécutées se trouvent dans `preuves/`. Arrêter le laboratoire avec
`docker compose -f docker/docker-compose-kafka.yml down`.

## Lancer Kafka et Cassandra v3.1

```bash
docker compose -f docker/docker-compose-v31.yml up -d
docker compose -f docker/docker-compose-v31.yml wait kafka-init cassandra-init
sbt "run qualify --seed 1600"
sbt "run consumer --group-id marc-v31-demo --max-records 500"
sbt "run report --bank AWB --date 2026-07-14"
sbt "run benchmark --samples 100 --repetitions 3 --parallelism 8"
sbt "run dashboard --banks AWB,CIH --date 2026-07-14 \
  --interval-seconds 5 --refreshes 3"
```

Le scénario `qualify` produit 485 événements valides uniques, 5 JSON invalides
et 10 replays exacts. L'état suit l'ordre :

```text
Received → projections idempotentes → Projected
         → output/DLQ acquitté → Completed → commit offset + 1
```

`Completed` vient après l'ack Kafka. Un crash dans cette dernière fenêtre peut
dupliquer une sortie, mais ne perd pas la sortie et ne double pas Cassandra.
Les tables couvrent la reprise, l'historique bucketé, les mouvements et
positions par banque/jour, ainsi que l'activité des paires. Aucun IBAN brut
n'est persisté.

Les preuves S16 dans `preuves/` montrent une coupure Cassandra après un
traitement partiel, la reprise du même consumer group jusqu'au lag nul et un
replay complet absorbé sans nouvelle projection. Arrêter le laboratoire avec :

```bash
docker compose -f docker/docker-compose-v31.yml down -v
```

## Lancer l'observabilité v3.2

La stack v3.2 ajoute OpenTelemetry Collector, Jaeger, Prometheus, Grafana,
Alertmanager et un webhook local. Tous les dashboards, datasources et règles
sont provisionnés depuis `docker/observability/`.

```bash
docker compose -f docker/docker-compose-v32.yml up -d
sbt -Dlogback.configurationFile=src/main/resources/logback-json.xml \
  "run consumer --group-id marc-v32-demo"
sbt "run qualify --seed 1701"
```

Interfaces locales : métriques sur `:8080/metrics`, Jaeger sur `:16686`,
Prometheus sur `:9090`, Grafana sur `:3000` et Alertmanager sur `:9093`.

Le gate reproductible recrée les volumes, traite 500 records, exige 485 sorties
et 5 DLQ, extrait les 970 IBAN réels de l'input, contrôle les signaux, provoque
`EngineDown`, corrèle pending/firing/resolved par incident, recherche les
données sensibles puis supprime les conteneurs et volumes :

```bash
./scripts/verify-v32-runtime.sh
```

Le comportement par défaut nettoie toujours la stack. Pour conserver
temporairement l'application rétablie et les interfaces locales afin de
collecter des preuves visuelles, le mode doit être explicitement activé :

```bash
KEEP_STACK=1 ./scripts/verify-v32-runtime.sh
```

Après la collecte, arrêter le processus annoncé par le gate et exécuter
`docker compose -f docker/docker-compose-v32.yml down -v`.

Les objectifs et limites sont décrits dans `slo-v32.md`. Les preuves S17 se
trouvent dans `preuves/s17-j*.md`.

## Lancer les démonstrations historiques

```bash
sbt "run --format JSON"
```

La commande traite `transactions-v22.csv` avec le profil MAD. Les autres
formats utilisent le même résultat :

```bash
sbt "run --format CSV"
sbt "run --format XML"
```

Un autre fichier
peut être fourni explicitement :

```bash
sbt "run --format JSON chemin/transactions-v22.csv"
```

Le scénario de panne contrôlée prouve la catégorie technique :

```bash
sbt "run --simulate-hash-failure --format JSON"
```

Le cœur v2.0 et son profil EUR restent disponibles séparément :

```bash
sbt "runMain clearing.v20.runClearingAppV20 --profile EUR transactions-v20.csv"
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

## Modules ajoutés en S10

- `EitherCsvParser` : parsing détaillé des huit colonnes en
  `Either[ParsingError, RailTransaction]`, sans conserver la ligne brute.
- `RailValidation` : accumulation des violations d'une ligne et rejet métier
  des IDs déjà acceptés.
- `AccountReservation` : exercice `findAccount`, `checkBalance` et
  `reserveFunds` composé avec un `for`.
- `RailRecovery` : récupération du seul libellé optionnel, avec warning
  conservé sur le rail droit.
- `RailwayEngine` : `for` complet par ligne, `List[Either]`, un
  `partitionMap`, statistiques et netting limité aux succès.
- `SecurityUtils.hashIbanTry`, `HttpExchangeRateService.fetchRateTry` et
  `V21IO` : frontières Java et fichier converties en valeurs typées.
- `RailRenderer` : consommation des rails avec `fold`, maps triées et rendu
  sans IBAN brut ni stack trace.
- `ClearingAppV21` et `V21Reporter` : capture de `NonFatal`, CLI, panne de hash
  simulée explicitement et unique frontière console.

## Modules ajoutés en S11

- `DomainTypes` : `BankCode`, `Iban`, `IbanHash` et `Money` opaques, factories
  sûres, `@targetName`, opérations monétaires et `Numeric[Money]`.
- `V22Domain` : transaction, configuration, résultat, positions et frais
  entièrement typés sur le chemin v2.2.
- `ClearingSerializable`, `ExportEngine` et les instances de format : sélection
  manuelle ou contextuelle de JSON, CSV et XML.
- `Syntax` : extensions sur `BigDecimal`, `String`, `LocalDateTime`, le domaine
  et les trois type classes de sérialisation.
- `TypedCsvParser` et `V22Validation` : conversion des primitives aux bords,
  erreurs nettoyées et règles sur les opaques.
- `TypedRailwayEngine` : `for` par ligne, netting `Money`, positions
  `BankCode`, warnings et unique `partitionMap`.
- `ClearingAppV22`, `V22IO` et `V22Reporter` : CLI multi-format, lecture typée,
  hash d'IBAN et unique frontière console.
- `MultiExportDemo` : trois transactions construites une fois et exportées par
  trois contextes différents.

## Modules ajoutés en S12

- `Functor` et `Box` : type class, instances `List`/`Option`/`Box`, fonction
  `transform` et propriétés d'identité/composition.
- `Monad` et `MonadicLogger` : `pure`, `flatMap`, `map` dérivé, chaînage
  générique et accumulation chronologique sans effet de bord.
- `LoggedRailwayLab` et `ForEquivalence` : relation entre journal et `Either`,
  puis équivalence observable du `for-yield` et du chaînage explicite.
- `V23Netting` : débit/crédit séquentiel, auto-virement neutre et invariant de
  somme globale.
- `FunctorLawSpec`, `MonadLawSpec` et `PropertySpec` : lois via les instances,
  IBAN simulés et deux propriétés sur 10 000 batchs de 200 transactions,
  construits sans filtre ni `Double`.
- `V23Pipeline` : observation, railway v2.2, netting certifié et export composés
  par un seul `for-yield` sur `MonadicLogger`.
- `ClearingAppV23` et `V23Reporter` : lecture, SHA-256, capture de `NonFatal` et
  unique frontière console.

## Modules ajoutés en S15

- `EventModel` et `EventCodec` : contrat JSON à huit champs, enveloppe horodatée
  et événements output/DLQ sans IBAN brut.
- `V30RecordProcessor` : adaptation vers `TypedRailwayEngine` v2.3 sans
  recopier le domaine ni les règles métier.
- `TransactionGenerator` et `TransactionProducer` : seed fixe, clé sender,
  header transaction, `acks=all`, idempotence et callbacks attendus.
- `BatchCoordinator` : traitement séquentiel par partition, arrêt au premier
  échec et calcul exact de l'offset suivant.
- `InMemoryDeduplicationRegistry` : couple ID/fingerprint marqué après l'accusé,
  conflit de payload vers la DLQ et état explicitement perdu au redémarrage.
- `KafkaDecisionPublisher`, `KafkaOffsetCommitter` et `KafkaConsumerLoop` :
  output/DLQ avant `commitSync`, `seek` du premier échec, auto-commit désactivé
  et arrêt propre.
- `docker-compose-kafka.yml` : Apache Kafka 4.3.0 KRaft, listeners hôte et
  conteneur, healthcheck broker et trois topics de trois partitions.

## Chemin Kafka v3.0

```text
clearing-input -> RecordEnvelope -> EventCodec -> TypedRailwayEngine v2.3
  -> ValidatedEvent ou RejectedEvent
  -> clearing-output ou clearing-dlq
  -> ack -> cache mémoire -> commit(partition, offset + 1)
```

La clé de sortie vient du sender validé; la clé DLQ vient de l'ID ou du
fingerprint, jamais de la clé d'entrée non fiable. La DLQ contient un
fingerprint SHA-256 plutôt que le payload original. Un crash après publication et avant commit peut
rejouer le record; le v3.0 garantit at-least-once, pas exactly-once externe.

## Chemin historique v2.3

```text
fichier -> V22IO -> chaîne CSV -> lignes numérotées
        -> factories BankCode / Iban / Money
        -> validate -> recover -> forex -> fee -> hash
        -> List[Either[V22Error, PreparedTransaction]]
        -> partitionMap -> V23Netting -> Map[BankCode, Money]
        -> given JSON / CSV / XML + journal pur -> V23Reporter
```

Le cœur v2.3 reçoit une chaîne, une configuration immutable, un format et une
fonction de hash typée `Iban => Either[HashFailure, IbanHash]`. Il retourne un
`V23Execution` et son journal sans lire ni afficher. Chaque `Left` reste attaché
à sa ligne; seuls les `Right` anonymisés alimentent les positions et les frais.

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
27. `Either` nomme l'erreur sur le rail gauche et la valeur sur le rail droit;
    `flatMap` arrête seulement la ligne au premier échec.
28. Une for-comprehension sur `Either` exprime le même enchaînement que des
    appels successifs à `flatMap` et un dernier `map`.
29. `fold` force le traitement explicite des deux rails sans dépendance
    externe; l'`Either` standard ne fournit pas `bimap`.
30. Une récupération sûre reste étroite et observable. Le défaut de libellé
    devient un warning; montant, IBAN, taux et frais restent bloquants.
31. `Try` protège une frontière Java; le cœur convertit ensuite son `Failure`
    en `TechnicalError` stable.
32. `NonFatal` couvre les exceptions récupérables. Les erreurs fatales de la
    JVM doivent continuer à remonter.
33. Une type class ajoute un comportement à un type sans modifier sa classe ni
    imposer un héritage métier.
34. `given` fournit une instance et `using` la demande; un import local rend le
    choix du format explicite.
35. Une extension method améliore la syntaxe, mais reste une fonction externe
    au type enrichi.
36. Un type opaque distingue deux concepts à la compilation tout en gardant le
    même type sous-jacent sur la JVM.
37. `@targetName` sépare les signatures JVM de deux extensions effacées vers le
    même type.
38. `Numeric[Money]` permet aux collections de sommer et comparer les montants
    sans abandonner le type métier.
39. Un Functor transforme la valeur dans un contexte sans changer la forme de
    ce contexte; identité et composition empêchent les surprises.
40. Une Monad ajoute `pure` et `flatMap`; `map` se dérive en replaçant le
    résultat de la fonction dans le contexte.
41. Un `for-yield` utilise les `flatMap` des générateurs successifs et un `map`
    final. Deux conteneurs différents ne se mélangent pas automatiquement.
42. Un journal pur accumule des traces; il ne court-circuite pas un `Either`
    placé dans sa valeur.
43. Un générateur doit construire des valeurs valides directement. Filtrer des
    candidats rares peut faire abandonner ScalaCheck avant le nombre d'essais.
44. Dix mille essais cherchent efficacement des contre-exemples, mais ne
    constituent pas une preuve formelle exhaustive.
45. KRaft porte les métadonnées Kafka sans ZooKeeper; le laboratoire conserve
    cependant un seul broker et un facteur de réplication 1.
46. La clé sender conserve l'ordre des événements d'une banque dans une même
    partition; elle ne crée aucun ordre global entre partitions.
47. Un offset committé désigne le prochain record à lire. Après l'offset 10,
    le consumer committe donc 11.
48. At-least-once préfère un doublon possible à une perte : output ou DLQ doit
    être confirmé avant le commit.
49. L'idempotence du producer Kafka ne rend pas un effet externe exactly-once.
    La déduplication durable appartient au stockage métier.
50. Une DLQ est aussi une frontière de sécurité; un fingerprint permet la
    corrélation sans recopier les IBAN du payload invalide.

## Limites volontaires de v3.0

- Le cluster possède un seul broker, sans réplication réelle, TLS ni SASL.
- Le cache de déduplication est local au processus et perdu au redémarrage.
- Un crash entre l'ack de sortie et le commit peut republier le résultat.
- Un même ID avec un payload différent devient un conflit DLQ; il n'est jamais
  ignoré comme replay.
- Les événements JSON ne disposent pas encore d'un registry de schémas.
- Le monitoring Prometheus/Grafana arrive après la persistance Cassandra.

## Limites volontaires de v3.1

- Le laboratoire possède un broker Kafka et un nœud Cassandra avec réplication
  1; il ne prouve aucune haute disponibilité.
- L'état durable et les projections sont idempotents, mais Kafka et Cassandra
  ne partagent pas de transaction exactly-once.
- Un crash après l'ack Kafka et avant `Completed` peut republier output ou DLQ.
- `max.poll.records=1` borne simplement les futures; aucun débit cible n'est
  encore qualifié.
- L'historique utilise 16 buckets fixes et le top des paires est agrégé en
  Scala; ces choix devront être mesurés avant un volume de production.
- Le script Compose initialise `clearing/datacenter1`; un autre keyspace ou
  datacenter doit être créé par une migration compatible.
- Prometheus et Grafana appartiennent à la prochaine semaine autorisée.

## Limites volontaires historiques de v2.3

- Les serializers sont volontaires et sans bibliothèque externe. Ils prouvent
  les type classes, l'échappement et le déterminisme; ils ne constituent pas
  encore un contrat de schéma versionné.
- `unsafe` initialise seulement les constantes contrôlées et la démonstration.
  Toute donnée externe passe par une factory qui retourne `Either`.
- Les taux et pourcentages restent des `BigDecimal`; S11 demande un opaque
  `Money`, pas une modélisation complète de chaque grandeur numérique.
- Le journal v2.3 utilise une `List[String]` pédagogique. Il ne remplace pas une
  solution de logs structurés, corrélés et persistés.
- Les propriétés exercent un domaine généré fini. Elles n'établissent pas une
  preuve formelle sur tous les programmes et toutes les entrées possibles.
- V2.3 lit encore un fichier local. Kafka, Cassandra et les services
  conteneurisés arrivent dans les semaines suivantes.

## Limites historiques conservées pour la non-régression

- V2.1 traite chaque ligne avec `Either`, mais n'accumule pas plusieurs erreurs
  entre les étapes : le premier échec bloquant court-circuite la ligne.
- Le profil MAD est une valeur locale déterministe. V2.1 ne contacte
  volontairement ni fournisseur de taux, ni Kafka, ni Cassandra.
- Les frais sont calculés et rapportés, mais ne participent pas au principal de
  règlement. La comptabilisation complète des commissions reste hors S10.

- Dans les jalons S1 à S10, les codes bancaires restent des `String`; le
  validateur les relie au référentiel, mais le compilateur ne peut pas détecter
  une faute de frappe. Le chemin actif v2.2 utilise `BankCode`.
- La factory historique `Transaction.fromCsv`, conservée pour les jalons S5 à
  S9, utilise encore `Option` et `MalformedCsv`. Le chemin actif v2.1 passe par
  `EitherCsvParser` et ne conserve jamais la ligne CSV brute dans une erreur.
- Dans le domaine historique, les IBAN restent des chaînes dans le candidat
  `Transaction` afin que le validateur puisse expliquer les entrées invalides;
  seules les valeurs passées par `Iban.apply` portent la garantie de validité.
  Le parser actif v2.2 construit l'opaque `Iban`.
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
