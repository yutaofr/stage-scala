# TP Jour 2 : Persister l'Histoire

**Durée :** ~4h | **Fil Rouge :** Développement du DAO de clearing

---

## Exercice 1 : Connexion Réactive (Starter Kit)

> [!TIP]
> **Starter Kit Fourni :** Faire le pont entre l'API Asynchrone de Datastax en Java et ZIO est complexe pour une journée. Sers-toi du Helper fourni : `exercises/s16/CassandraAsyncBridge.scala`.

1. Ajoute les dépendances du driver Datastax.
2. Utilise le mécanisme de `Scope` (M4-J3) pour garantir que la session (`CqlSession`) sera fermée proprement à la fin du programme.
3. Inspecte la fonction `fromCompletionStage` fournie dans le Starter Kit, tu l'utiliseras à l'exercice suivant !

---

## Exercice 2 : Implémentation du SAVE (1h30)

1. Implémente la méthode `save(tx: Transaction): ZIO[Any, Throwable, Unit]`.
2. Utilise un `PreparedStatement` pour la sécurité et la performance.
3. Vérifie via la console `cqlsh` que les transactions enregistrées sont bien présentes.

---

## Exercice 3 : Test de Performance (1h)

1. Génère une liste de 100 transactions fictives.
2. Enregistre-les en utilisant `ZIO.foreachPar`.
3. Mesure le temps total. Compare avec une écriture séquentielle (`ZIO.foreach`).

**Livrable** : Code source du TransactionRepository et logs de performance montrant les bénéfices de l'écriture asynchrone parallèle.
