# TP Jour 2 : Persister l'Histoire

**Durée :** ~4h | **Fil Rouge :** Développement du DAO de clearing

---

## Exercice 1 : Connexion Réactive (Starter Kit)

> [!TIP]
> **Starter Kit fourni :** utilise le **Kit 16.2**, fichier `distributed/persistence/ClearingRepository.scala`.

1. Vérifie les dépendances du driver dans le build.
2. Lance le programme avec la couche `CqlSession` scoped.
3. Inspecte le helper `executeAsync`.
4. Coupe Cassandra et vérifie que l'erreur reste dans le canal `Task`.

**Validation :** la session s'ouvre une fois, se partage et se ferme à l'arrêt.

---

## Exercice 2 : Implémentation du SAVE (1h30)

1. Prépare les statements au démarrage de la couche.
2. Implémente `stage` et `markStage` avec `processing_state`.
3. Persiste ensuite les quatre projections par banque, date et paire.
4. Réutilise les mêmes clés lors d'une reprise.
5. Vérifie le résultat avec `cqlsh`.

**Validation :** deux appels avec le même ID conservent une ligne d'état et les mêmes projections.

---

## Exercice 3 : Test de Performance (1h)

1. Génère cent transactions déterministes.
2. Compare `ZIO.foreach` avec `ZIO.foreachPar(...).withParallelism(8)`.
3. Répète chaque mesure au moins trois fois après un warm-up.
4. Compare débit, latence et erreurs ; ne conserve pas seulement la meilleure valeur.

**Validation :** le rapport contient les paramètres et les trois mesures de chaque variante.

**Livrable** : Code source du TransactionRepository et logs de performance montrant les bénéfices de l'écriture asynchrone parallèle.
