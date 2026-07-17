# Marc Semaine 2 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Livrer le Clearing Engine `v0.2` alimenté par CSV et structuré par traits.

**Architecture:** Le tuple riche S2 traverse génération, parsing, routing et
processing. Les calculs restent purs ; les effets fichier, logs et console sont
concentrés dans `SimpleClearingProcessor` et le point d'entrée.

**Tech Stack:** Scala 3.3.8, SBT 1.10.11, ScalaTest 3.2.19.

---

### Task 1: Tuple riche, rapports et routing

**Files:**
- Test: `src/test/scala/clearing/TransactionV2Spec.scala`
- Create: `src/main/scala/clearing/TransactionV2.scala`
- Create: `src/main/scala/clearing/ReportGenerator.scala`
- Create: `src/main/scala/clearing/TransactionRouter.scala`

**Steps:**
1. Tester taille, identifiants et banques distinctes du batch.
2. Tester tri par montant absolu et matrice antisymétrique.
3. Tester les quatre branches du routeur.
4. Vérifier RED, implémenter le minimum, puis vérifier GREEN.

### Task 2: Parser résilient et factory

**Files:**
- Test: `src/test/scala/clearing/CsvParserSpec.scala`
- Create: `src/main/scala/clearing/CsvParser.scala`
- Create: `src/main/scala/clearing/Transaction.scala`

**Steps:**
1. Tester lignes 3 et 5 colonnes, espaces, ligne vide, montant et colonnes invalides.
2. Tester que `Transaction(line)` délègue au parser.
3. Vérifier RED.
4. Implémenter avec pattern matching, `trim`, `toIntOption` et `Option`.
5. Vérifier GREEN.

### Task 3: Contrats, logs et moteur v0.2

**Files:**
- Test: `src/test/scala/clearing/ClearingProcessorSpec.scala`
- Test: `src/test/scala/clearing/IntegrationV02Spec.scala`
- Test resource: `src/test/resources/transactions-s2.csv`
- Create: `src/main/scala/clearing/ClearingProcessor.scala`
- Create: `src/main/scala/clearing/Logger.scala`
- Create: `src/main/scala/clearing/SimpleClearingProcessor.scala`
- Create: `transactions.csv`
- Modify: `src/main/scala/clearing/ClearingEngine.scala`
- Modify: `build.sbt`

**Steps:**
1. Tester le contrat sur une petite liste déterministe.
2. Tester les logs et le rapport par capture console.
3. Tester le pipeline sur un vrai fichier CSV.
4. Vérifier RED, implémenter, puis vérifier GREEN.
5. Exécuter `sbt clean test`, `sbt run` et le test Docker.
6. Mettre à jour README et journal mentor avec les preuves fraîches.
