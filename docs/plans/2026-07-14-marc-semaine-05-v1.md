# Marc Semaine 5 v1.0 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Livrer un Clearing Engine v1.0 fondé sur des case classes, des enums et des ADT métier, sans tuple anonyme dans la nouvelle chaîne.

**Architecture:** `clearing.model` définit les données et les erreurs; `clearing.v10` orchestre parsing, validation, netting et rapport. Les versions v0.x restent disponibles pour les tests de non-régression, sans être appelées par v1.0.

**Tech Stack:** Scala 3.3.8, ScalaTest 3.2.19, sbt, Java 17 et 21.

---

### Task 1: Modèle métier nommé

**Files:**
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/model/Domain.scala`
- Test: `stagiaires/marc/fil-rouge/src/test/scala/clearing/model/DomainSpec.scala`

**Step 1: Write the failing test**

Tester l'égalité structurelle et `copy` de `Bank` et `Transaction`, la limite
de `isHighValue`, les valeurs des deux enums, `ClearingBatch.totalAmount` et
les objets de résultat nommés.

**Step 2: Run test to verify it fails**

Run: `sbt 'testOnly clearing.model.DomainSpec'`
Expected: FAIL because package `clearing.model` is absent.

**Step 3: Write minimal implementation**

Créer les case classes `Bank`, `Account`, `Transaction`, `InvalidTransaction`,
`ValidationSummary`, `ClearingBatch`, `ClearingResult` et `AppResult`. Créer
les enums `TransactionStatus` et `TransactionType`, puis le trait scellé
`ClearingError` et ses quatre cas.

**Step 4: Run test to verify it passes**

Run: `sbt 'testOnly clearing.model.DomainSpec'`
Expected: PASS.

### Task 2: Parsing, génération et routage typés

**Files:**
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/v10/CsvParserV10.scala`
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/v10/TransactionGeneratorV10.scala`
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/v10/TransactionRouterV10.scala`
- Test: `stagiaires/marc/fil-rouge/src/test/scala/clearing/v10/TypedInputsSpec.scala`

**Step 1: Write the failing test**

Tester les formats CSV à trois et cinq colonnes, les trois codes de type, le
rejet des codes inconnus, le générateur et les quatre branches de statut.

**Step 2: Run test to verify it fails**

Run: `sbt 'testOnly clearing.v10.TypedInputsSpec'`
Expected: FAIL because the v1.0 input modules are absent.

**Step 3: Write minimal implementation**

Parser avec des Regex et `Option`, générer des transactions à banques
distinctes avec `Random`, puis router par un match exhaustif sur
`TransactionStatus`.

**Step 4: Run test to verify it passes**

Run: `sbt 'testOnly clearing.v10.TypedInputsSpec'`
Expected: PASS.

### Task 3: Erreurs et validation par ADT

**Files:**
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/v10/TransactionValidator.scala`
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/v10/ErrorReporter.scala`
- Test: `stagiaires/marc/fil-rouge/src/test/scala/clearing/v10/TransactionValidatorSpec.scala`

**Step 1: Write the failing test**

Tester l'accumulation de `InvalidAmount`, `UnknownBank` et
`ValidationError`, le traitement déterministe des doublons et le rendu de
chaque sous-type de `ClearingError`.

**Step 2: Run test to verify it fails**

Run: `sbt 'testOnly clearing.v10.TransactionValidatorSpec'`
Expected: FAIL because validator and reporter are absent.

**Step 3: Write minimal implementation**

Construire les erreurs par concaténation de listes immuables. Parcourir le lot
avec `foldLeft` et un état nommé pour conserver les identifiants déjà vus.
Formatter les erreurs par pattern matching exhaustif et déstructuration.

**Step 4: Run test to verify it passes**

Run: `sbt 'testOnly clearing.v10.TransactionValidatorSpec'`
Expected: PASS.

### Task 4: Lot, netting et application v1.0

**Files:**
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/v10/NettingCalculatorV10.scala`
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/v10/BatchProcessor.scala`
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/v10/ClearingAppV10.scala`
- Test: `stagiaires/marc/fil-rouge/src/test/scala/clearing/v10/BatchProcessorSpec.scala`
- Test: `stagiaires/marc/fil-rouge/src/test/scala/clearing/v10/IntegrationV10Spec.scala`
- Create: `stagiaires/marc/fil-rouge/src/test/resources/transactions-s5.csv`
- Create: `stagiaires/marc/fil-rouge/transactions-v10.csv`

**Step 1: Write the failing test**

Tester le netting nommé, les trois descriptions de résultat, le fichier de
seize lignes et le rapport complet. Le scénario cible contient dix valides,
quatre invalides typées et deux lignes malformées; ses positions se compensent.

**Step 2: Run test to verify it fails**

Run: `sbt 'testOnly clearing.v10.BatchProcessorSpec clearing.v10.IntegrationV10Spec'`
Expected: FAIL because batch processing and v1.0 app are absent.

**Step 3: Write minimal implementation**

Calculer les positions avec `foldLeft`. Valider le lot, calculer seulement les
transactions valides et rendre un `AppResult`. Lire le fichier avec fermeture
garantie et afficher un rapport stable trié par code bancaire.

**Step 4: Run test to verify it passes**

Run: `sbt 'testOnly clearing.v10.BatchProcessorSpec clearing.v10.IntegrationV10Spec'`
Expected: PASS with global net equal to zero.

### Task 5: Livrable, non-régression et acceptation mentor

**Files:**
- Modify: `stagiaires/marc/fil-rouge/build.sbt`
- Modify: `stagiaires/marc/fil-rouge/README.md`
- Modify: `stagiaires/marc/README.md`
- Create: `stagiaires/marc/suivi/semaine-05.md`

**Step 1: Update the deliverable**

Passer la version à `1.0.0-SNAPSHOT`, choisir `clearing.v10.runClearingAppV10`
comme main, documenter les commandes, les choix et les limites, puis cocher
chaque critère seulement après preuve.

**Step 2: Run focused and full verification**

Run: `sbt clean test 'run transactions-v10.csv'`
Expected: all S1-S5 tests pass and the v1.0 report shows ten valid
transactions, four typed invalid transactions, two malformed lines, and a
global net of zero.

Run in the pedagogical Java 17 image:
`docker run --rm -v "$PWD:/workspace" -w /workspace sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 sbt clean test 'run transactions-v10.csv'`
Expected: the same tests and report pass.

**Step 3: Audit the v1.0 source**

Run: `rg -n 'Tuple|\._[1-9]|case \(' src/main/scala/clearing/model src/main/scala/clearing/v10`
Expected: no anonymous tuple business access.

Run: `rg -n '\b(var|null|while)\b|\?\?\?|scala\.collection\.mutable|\bDouble\b' src/main/scala/clearing/model src/main/scala/clearing/v10`
Expected: no forbidden marker.

**Step 4: Commit and tag**

```bash
git add docs/plans stagiaires/marc
git commit -m "feat: add Marc week five domain model"
git tag -a marc-v1.0 -m "Marc Clearing Engine v1.0"
```
