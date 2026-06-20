# Continuité du fil rouge S14-S20 — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** fournir une base exécutable issue des acquis S1-S13, puis aligner les starter kits S14-S20 sur un contrat métier et des routes stables.

**Architecture:** un projet cumulatif `fil-rouge/` porte le domaine canonique, les contrats de frontière et les tests. Chaque semaine ajoute une capacité sans redéfinir `Transaction`. Les fichiers `starter_kit_sXX.md` décrivent les zones laissées au stagiaire et renvoient vers cette base.

**Tech Stack:** Scala 3.3 LTS, ZIO 2, ZIO JSON, Apache Kafka 4, Cassandra 4, Tapir, ZIO HTTP, Docker Compose et sbt-assembly.

---

### Task 1: Stabiliser la reprise S13 vers S14

**Files:**
- Create: `fil-rouge/build.sbt`
- Create: `fil-rouge/src/main/scala/clearing/model/Domain.scala`
- Create: `fil-rouge/src/main/scala/clearing/core/Clearing.scala`
- Create: `fil-rouge/src/main/scala/clearing/contract/Events.scala`
- Create: `fil-rouge/src/test/scala/clearing/CoreSuite.scala`
- Modify: `mois4/semaine14/starter_kit_s14.md`

**Checks:**

1. Compiler le projet.
2. Tester la validation, l’invariant du netting et le codec JSON aller-retour.
3. Exposer les erreurs métier dans le canal ZIO avec `ZIO.fromEither`.

### Task 2: Formaliser le flux Kafka S15

**Files:**
- Create: `fil-rouge/src/main/scala/distributed/kafka/KafkaPipeline.scala`
- Modify: `mois4/semaine15/starter_kit_s15.md`
- Modify: `mois4/semaine15/jour2_tp.md`
- Modify: `mois4/semaine15/jour3_tp.md`
- Modify: `mois4/semaine15/jour5_tp.md`

**Checks:**

1. Utiliser `BankCode.value` comme clé Kafka.
2. Publier les succès dans `clearing-output` et les rejets dans `clearing-dlq`.
3. Valider les offsets par partition après la dernière sortie confirmée.

### Task 3: Rendre la reprise S16 réparable

**Files:**
- Create: `fil-rouge/src/main/scala/distributed/persistence/DurableProcessing.scala`
- Create: `fil-rouge/docker/init-cassandra.cql`
- Modify: `mois4/semaine16/starter_kit_s16.md`
- Modify: `mois4/semaine16/jour3_tp.md`
- Modify: `mois4/semaine16/jour4_tp.md`

**Checks:**

1. Persister les états `Received`, `Projected` et `Completed`.
2. Rendre les projections idempotentes.
3. Reprendre un record incomplet avant de valider son offset.

### Task 4: Relier S17-S18 au processeur réel

**Files:**
- Modify: `mois5/semaine17/starter_kit_s17.md`
- Modify: `mois5/semaine18/starter_kit_s18.md`
- Modify: `mois5/semaine18/jour1_tp.md`

**Checks:**

1. Instrumenter l’enveloppe Kafka et le processeur durable.
2. Mesurer aussi les échecs.
3. Stabiliser l’ingestion HTTP sur `POST /api/v1/transactions`.

### Task 5: Fournir le packaging et l’API S19-S20

**Files:**
- Create: `fil-rouge/src/main/scala/distributed/http/Api.scala`
- Create: `fil-rouge/Dockerfile`
- Create: `fil-rouge/docker/compose.yml`
- Create: `fil-rouge/.github/workflows/ci.yml`
- Modify: `mois5/semaine19/starter_kit_s19.md`
- Modify: `mois5/semaine20/starter_kit_s20.md`
- Modify: `mois5/semaine20/jour2_tp.md`

**Checks:**

1. Compiler le serveur et générer le JAR d’assemblage.
2. Exposer `/health`, `/docs`, l’ingestion et la consultation.
3. Faire utiliser les variables Kafka et Cassandra par l’application.

### Task 6: Corriger la chronologie

**Files:**
- Modify: `mois-3/semaine12/jour5_cours.md`
- Modify: `mois-3/semaine12/jour5_tp.md`
- Modify: `mois4/semaine13/semaine13_detaille.md`
- Modify: `mois5/semaine20/semaine20_detaille.md`

**Checks:**

1. S12 livre v2.3.
2. S13 livre v2.3.1, première orchestration concurrente.
3. S14 livre v2.4, puis S15 démarre v3.0.
