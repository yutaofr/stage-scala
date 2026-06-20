---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 16"
footer: "Jour 2 — Drivers Asynchrones & ZIO Integration"
---

# ZIO & Cassandra
## Persistance réactive et non-bloquante

**Durée :** ~2h | **Fil Rouge :** Le DAO de clearing

---

# 📋 Objectifs du Jour

- Utiliser le driver Java `Datastax` pour Cassandra.
- Transformer les appels `Future` du driver en effets `ZIO`.
- Apprendre à gérer les sessions Cassandra comme des `ZLayer`.
- Éviter les goulots d'étranglement lors de l'insertion massive.

---

# 1. Le Driver Datastax v4

Contrairement aux bases SQL, le driver Cassandra est nativement asynchrone.

```scala
val session: CqlSession = CqlSession.builder().build()
val resultSetFuture = session.executeAsync("SELECT ...")
```

### Le pont vers ZIO
Nous allons utiliser `ZIO.fromCompletionStage` pour transformer cette "Future Java" en un "Effet ZIO".

---

# 2. Le Repository Pattern en ZIO

```scala
trait TransactionRepository:
  def save(tx: Transaction): ZIO[Any, Throwable, Unit]

val liveLayer = ZLayer.fromFunction(session => new TransactionRepositoryLive(session))
```

- Le service métier demande un `TransactionRepository`.
- Le repository demande une `CqlSession`.
- ZIO branche tout dynamiquement.

---

# 3. Écritures asynchrones bornées

Cassandra accepte plusieurs écritures en vol. Nous utiliserons un parallélisme borné pour augmenter le débit sans saturer le pool du driver ni le cluster.

> [!TIP]
> Commence avec une petite limite, mesure la latence et les erreurs, puis ajuste. Un parallélisme illimité peut réduire le débit.

---

# 🏗️ Application : Le TransactionDAO

Nous allons coder la couche d'accès aux données (DAO) qui traduit nos objets `Transaction` en requêtes CQL `INSERT`.

---

# 🧠 Quiz Rapide

1. Pourquoi utiliser `executeAsync` plutôt que `execute` ? (Pour ne pas bloquer les threads ZIO pendant l'attente du réseau).
2. Comment s'appelle l'opération qui transforme une Future Java en ZIO ? (`ZIO.fromCompletionStage`).
3. Doit-on fermer la session Cassandra à chaque requête ? (Non, on la maintient ouverte via un ZLayer Scope).

---

# 📝 Résumé du Jour

- Le driver Cassandra est le partenaire idéal de ZIO (tous les deux sont asynchrones).
- L'injection de dépendances via ZLayer rend le code propre et testable.
- Ton application est maintenant capable d'écrire sur disque à la vitesse de l'éclair.
- Tu maîtrises le pipeline de données moderne : Flux -> Calcul -> Stockage.

**Prochaine étape** : Utiliser le Kit 16.2 dans le TP du Jour 2.
