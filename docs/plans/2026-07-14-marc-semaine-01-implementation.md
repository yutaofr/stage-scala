# Marc Semaine 1 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Construire et valider le premier Clearing Engine `v0.1` de Marc.

**Architecture:** Un projet SBT cumulatif contient des modules Scala 3 simples
dans le package `clearing`. Les calculs sont purs ; les méthodes `@main` servent
uniquement aux démonstrations.

**Tech Stack:** Scala 3.3.8, SBT 1.10.11, ScalaTest 3.2.19.

---

### Task 1: Socle SBT et suivi

**Files:**
- Create: `stagiaires/marc/fil-rouge/build.sbt`
- Create: `stagiaires/marc/fil-rouge/project/build.properties`
- Create: `stagiaires/marc/README.md`
- Create: `stagiaires/marc/suivi/semaine-01.md`

**Steps:**
1. Déclarer Scala 3 et ScalaTest.
2. Exécuter `sbt test` pour valider le socle vide.
3. Noter les versions réellement utilisées.

### Task 2: Jours 1 et 2 — nombres et précision

**Files:**
- Test: `src/test/scala/clearing/BasicsSpec.scala`
- Test: `src/test/scala/clearing/CurrencyConverterSpec.scala`
- Test: `src/test/scala/clearing/InterestCalculatorSpec.scala`
- Create: `src/main/scala/clearing/Basics.scala`
- Create: `src/main/scala/clearing/PrecisionDemo.scala`
- Create: `src/main/scala/clearing/CurrencyConverter.scala`
- Create: `src/main/scala/clearing/InterestCalculator.scala`

**Steps:**
1. Écrire les tests de somme, conversion, formatage et intérêts composés.
2. Lancer les suites et vérifier l'échec dû aux API absentes.
3. Implémenter les API minimales avec `BigDecimal`.
4. Relancer les suites et obtenir un résultat vert.

### Task 3: Jour 3 — catégorisation et filtres

**Files:**
- Test: `src/test/scala/clearing/TransactionPipelineSpec.scala`
- Create: `src/main/scala/clearing/TransactionCategorizer.scala`
- Create: `src/main/scala/clearing/TransactionGenerator.scala`
- Create: `src/main/scala/clearing/TransactionFilter.scala`

**Steps:**
1. Tester chaque frontière de catégorie et chaque code de transaction.
2. Tester la taille, les valeurs autorisées et les filtres du batch.
3. Vérifier RED, implémenter, puis vérifier GREEN.

### Task 4: Jour 4 — validation, netting et récursion

**Files:**
- Test: `src/test/scala/clearing/ValidatorSpec.scala`
- Test: `src/test/scala/clearing/NettingCalculatorSpec.scala`
- Test: `src/test/scala/clearing/TransactionSearchSpec.scala`
- Create: `src/main/scala/clearing/Validator.scala`
- Create: `src/main/scala/clearing/NettingCalculator.scala`
- Create: `src/main/scala/clearing/TransactionSearch.scala`

**Steps:**
1. Tester chaque règle et les cas vide, unique et mixte.
2. Vérifier RED.
3. Implémenter les fonctions sans état mutable.
4. Vérifier GREEN.

### Task 5: Jour 5 — livrable v0.1

**Files:**
- Test: `src/test/scala/clearing/ClearingEngineSpec.scala`
- Create: `src/main/scala/clearing/ClearingEngine.scala`
- Modify: `stagiaires/marc/suivi/semaine-01.md`

**Steps:**
1. Tester un batch déterministe et l'invariant de netting global.
2. Vérifier RED.
3. Assembler génération, validation, catégorisation, netting et rapport.
4. Vérifier GREEN avec `sbt clean test`.
5. Lancer la démonstration avec `sbt run`.
6. Reporter les commandes, résultats et limites dans le journal.
