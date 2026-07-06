---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 16"
footer: "Jour 2 — Drivers Asynchrones & Multithreading"
---

# Programmation Asynchrone & Cassandra
## Persistance réactive et non-bloquante

**Durée :** ~2h | **Fil Rouge :** Le DAO de clearing

---

# 📋 Objectifs du Jour

- Utiliser le driver Java `Datastax` pour Cassandra.
- Gérer l'asynchronisme de manière native avec `CompletableFuture`.
- Apprendre à structurer son DAO avec des patrons de conception classiques (Repository).
- Éviter les goulots d'étranglement lors de l'insertion massive.

---

# 1. Le Driver Datastax v4 Asynchrone

Contrairement aux bases de données SQL traditionnelles bloquantes, le driver Cassandra est nativement asynchrone et repose sur des futures.

```scala
val session: CqlSession = CqlSession.builder().build()
val completionStage = session.executeAsync("SELECT ...")
```

### Utiliser CompletableFuture en Scala
Nous pouvons convertir le `CompletionStage` retourné par le driver en un `CompletableFuture` standard pour chaîner ou attendre les résultats de manière non bloquante :
```scala
val future = completionStage.toCompletableFuture
// Attente bloquante si nécessaire (dans les tests ou le thread principal)
val result = future.get()
```

---

# 2. Le Repository Pattern

Pour isoler la persistance du domaine, nous utilisons une interface pure :

```scala
trait TransactionRepository:
  def save(tx: Transaction): Unit
```

L'implémentation concrète reçoit la session `CqlSession` par constructeur. C'est l'injection de dépendances classique par constructeur.

---

# 3. Écritures asynchrones bornées

Cassandra accepte de nombreuses écritures simultanées. Pour maximiser le débit sans saturer la base de données, nous pouvons contrôler le nombre de futures actives simultanément à l'aide d'un pool de threads ou en chaînant les appels.

> [!TIP]
> Ne lancez pas toutes les écritures en parallèle sans limite. Utilisez un parallélisme borné (par exemple via un `ExecutorService` dédié) pour garder le contrôle sur la latence.

---

# 🏗️ Application : Le TransactionDAO

Nous allons coder la couche d'accès aux données (DAO) qui traduit nos objets `Transaction` en requêtes CQL `INSERT` et interagit avec notre cluster Cassandra local de manière asynchrone.

---

# 🧠 Quiz Rapide

1. Pourquoi utiliser `executeAsync` plutôt que `execute` ? (Pour ne pas bloquer les threads de l'application pendant l'attente du réseau).
2. Comment attend-on la fin d'une opération asynchrone en Java/Scala ? (En appelant `.get()` sur la future ou en chaînant les étapes via des callbacks).
3. Doit-on fermer la session Cassandra à chaque requête ? (Non, on la maintient ouverte durant toute la vie de l'application car sa création est très coûteuse).

---

# 📝 Résumé du Jour

- Le driver Cassandra fournit une API nativement asynchrone.
- Le découplage par Repository permet de garder le cœur métier indépendant de Cassandra.
- Tu maîtrises le pipeline de données moderne : Flux -> Calcul -> Stockage.

**Prochaine étape** : Utiliser le Kit 16.2 dans le TP du Jour 2.
