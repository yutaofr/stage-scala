# Marc Semaine 4 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Livrer le Clearing Engine v0.4 avec HOF, currying, Option, récursion terminale et validation configurable.

**Architecture:** Des fonctions `BigDecimal => Boolean` forment le moteur de règles. Un workflow pur compose `Transaction.apply`, validation et enrichissement avec `Option`; `MainV04` conserve les lectures et affichages au bord.

**Tech Stack:** Scala 3.3.8, SBT 1.10.11, ScalaTest 3.2.19, Java 17/21.

---

### Task 1: Fonctions d'ordre supérieur et currying

**Files:**
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/ValidationRules.scala`
- Create: `stagiaires/marc/fil-rouge/src/test/scala/clearing/ValidationRulesSpec.scala`

**Step 1: Write the failing tests**

Tester `filtrerTransactions` avec pair, supérieur à 1 000 et intervalle
100–500. Tester `appliquerFrais("ATH")(100)` à `101.00`, une autre banque à
`102.00`, puis les fonctions spécialisées. Tester `validateAll` et
`validateAny`, y compris une liste de règles vide.

**Step 2: Run test to verify it fails**

Run: `sbt "testOnly clearing.ValidationRulesSpec"`

Expected: compilation failure because `ValidationRules` does not exist.

**Step 3: Write the minimal implementation**

Créer `type Rule = BigDecimal => Boolean`, `nonNul`, `positif`, `seuilAudit`,
les deux combinateurs et le configurateur curryfié. Arrondir le montant avec
frais à deux décimales.

**Step 4: Run test to verify it passes**

Run: `sbt "testOnly clearing.ValidationRulesSpec"`

Expected: all tests in the suite pass.

### Task 2: Exercices Option

**Files:**
- Modify: `stagiaires/marc/fil-rouge/src/main/scala/clearing/ReferenceData.scala`
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/OptionTools.scala`
- Create: `stagiaires/marc/fil-rouge/src/test/scala/clearing/OptionToolsSpec.scala`

**Step 1: Write the failing tests**

Tester banque connue/inconnue, chaîne IBAN-utilisateur complète ou rompue,
entrée vide/nettoyée/majuscule, `List[Option]` aplatie et devise connue/inconnue.

**Step 2: Run test to verify it fails**

Run: `sbt "testOnly clearing.OptionToolsSpec"`

Expected: compilation failure on the missing S4 APIs.

**Step 3: Write the minimal implementation**

Ajouter `findBankName`; implémenter les exercices avec `get`, `map`, `flatMap`,
`Option.when` and `flatten`.

**Step 4: Run test to verify it passes**

Run: `sbt "testOnly clearing.OptionToolsSpec"`

Expected: all tests pass.

### Task 3: Composition du parser

**Files:**
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/TransactionWorkflowV4.scala`
- Create: `stagiaires/marc/fil-rouge/src/test/scala/clearing/TransactionWorkflowV4Spec.scala`

**Step 1: Write the failing tests**

Tester séparément validation, enrichissement et composition. Couvrir ligne
valide, montant négatif, seuil d'audit, banque inconnue, virement interne,
colonnes vides, montant texte et comptage des lignes ignorées.

**Step 2: Run test to verify it fails**

Run: `sbt "testOnly clearing.TransactionWorkflowV4Spec"`

Expected: compilation failure because the workflow is absent.

**Step 3: Write the minimal implementation**

Composer `Transaction(line)`, `validate` et `enrich` avec une
for-comprehension. Collecter par `lines.view.map(...).flatten.toList` et calculer
`ignored = lines.size - accepted.size`.

**Step 4: Run test to verify it passes**

Run: `sbt "testOnly clearing.TransactionWorkflowV4Spec"`

Expected: all tests pass and existing `CsvParserSpec` stays green.

### Task 4: Récursion terminale

**Files:**
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/BankingRecursion.scala`
- Create: `stagiaires/marc/fil-rouge/src/test/scala/clearing/BankingRecursionSpec.scala`

**Step 1: Write the failing tests**

Tester l'historique `100, 50, 250, 150`, la liste vide, la frontière `-500`,
le premier passage sous la limite, signature présente/absente et Fibonacci.
Ajouter un test sur 100 000 éléments pour la sûreté de pile.

**Step 2: Run test to verify it fails**

Run: `sbt "testOnly clearing.BankingRecursionSpec"`

Expected: compilation failure because `BankingRecursion` is absent.

**Step 3: Write the minimal implementation**

Employer des fonctions internes avec accumulateurs et `@tailrec`. Inverser une
seule fois l'accumulateur de l'historique.

**Step 4: Run test to verify it passes**

Run: `sbt "testOnly clearing.BankingRecursionSpec"`

Expected: all tests pass without `StackOverflowError`.

### Task 5: Intégration v0.4

**Files:**
- Create: `stagiaires/marc/fil-rouge/src/main/scala/clearing/MainV04.scala`
- Create: `stagiaires/marc/fil-rouge/src/test/scala/clearing/IntegrationV04Spec.scala`
- Create: `stagiaires/marc/fil-rouge/src/test/resources/transactions-s4.csv`
- Modify: `stagiaires/marc/fil-rouge/build.sbt`
- Modify: `stagiaires/marc/fil-rouge/README.md`

**Step 1: Write the failing integration tests**

Tester le fichier de seize lignes, les dix acceptations, les six rejets, les
positions S3 et le total nul. Fournir une liste sans `seuilAudit` et vérifier
que la transaction de 100 000 devient acceptable.

**Step 2: Run test to verify it fails**

Run: `sbt "testOnly clearing.IntegrationV04Spec"`

Expected: compilation failure because `MainV04` is absent.

**Step 3: Write the minimal implementation**

Lire le fichier, appeler le workflow, déléguer le netting, trier le rapport et
exposer `runMainV04` avec fichier par défaut. Passer la version à
`0.4.0-SNAPSHOT` et documenter le flux.

**Step 4: Run test to verify it passes**

Run: `sbt "testOnly clearing.IntegrationV04Spec"`

Expected: all integration tests pass.

### Task 6: Validation mentor

**Files:**
- Create: `stagiaires/marc/suivi/semaine-04.md`
- Modify: `stagiaires/marc/README.md`

**Step 1: Run all local tests**

Run: `sbt clean test`

Expected: every S1–S4 suite passes on Java 21.

**Step 2: Run the two demonstrations**

Run: `sbt run`

Run: `sbt "run src/test/resources/transactions-s4.csv"`

Expected: graceful rejection counts and global net zero.

**Step 3: Run Docker verification**

Run: `docker run --rm -v "$PWD:/app" -w /app sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 sbt clean test`

Expected: every suite passes on Java 17.

**Step 4: Audit source**

Search production sources for `var`, `null`, `???`, `while`, mutable
collections and unexpected `Double` use. Verify the S4 combinators, `view`,
for-comprehension, `foldLeft` and `@tailrec` are present.

**Step 5: Record evidence**

Write the exact test counts, demonstrations, incidents and mentor decision in
`suivi/semaine-04.md`; mark S4 accepted only after every check succeeds.
