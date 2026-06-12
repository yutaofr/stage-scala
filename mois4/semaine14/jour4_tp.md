# TP Jour 4 : Concurrence Massive avec ZIO

**Durée :** ~4h | **Fil Rouge :** Accélération du Clearing Engine

---

## Exercice 1 : Foreach Parallèle (1h30)

1. Crée une liste de 10 "Transactions".
2. Implémente une fonction `validate(tx: Transaction)` qui fait un `ZIO.sleep(500.millis)`.
3. Lance le traitement séquentiel (`ZIO.foreach`) et mesure le temps (5 secondes).
4. Lance le traitement parallèle (`ZIO.foreachPar`) et mesure le temps (0.5 secondes !).

---

## Exercice 2 : Timeout & Interruption (1h30)

1. Simule un appel à l'API de change qui prend parfois trop de temps (utiliser `ZIO.sleep(Random.nextInt(5).seconds)`).
2. Utilise `.timeout(2.seconds)` pour garantir que le moteur ne reste pas bloqué.
3. Vérifie que si le timeout est atteint, tu reçois un `None`.

---

## Exercice 3 : Le Race (1h)

1. Simule deux services de change redondants (Source A et Source B).
2. Utilise `sourceA.race(sourceB)` pour récupérer le taux de la source la plus rapide.
3. Affiche le nom de la source qui a gagné la course.

**Livrable** : Code source démontrant l'usage de `foreachPar`, `timeout` et `race` pour optimiser les performances du moteur.
