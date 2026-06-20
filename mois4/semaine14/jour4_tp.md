# TP Jour 4 : Concurrence Massive avec ZIO

**Durée :** ~4h | **Fil Rouge :** Accélération du Clearing Engine

---

## Point de départ

- Copie le **Kit 14.4** de `starter_kit_s14.md`.
- Utilise les mêmes transactions et la même fonction pour les mesures séquentielles et parallèles.
- Ne conclue qu'à partir de mesures répétées.

## Exercice 1 : Foreach Parallèle (1h30)

1. Crée dix transactions déterministes.
2. Simule une validation I/O de `500.millis`.
3. Mesure `ZIO.foreach`, puis `ZIO.foreachPar(...).withParallelism(4)`.
4. Explique pourquoi la durée parallèle attendue se rapproche de trois vagues, et non de `0.5 s`.

**Validation :** les dix résultats sont présents, dans un ordre qui ne doit pas servir de garantie métier.

---

## Exercice 2 : Timeout & Interruption (1h30)

1. Tire un délai avec `Random.nextIntBetween(0, 5)`.
2. Transforme ce nombre en durée, puis simule l'appel.
3. Utilise `.timeout(2.seconds)` et traite explicitement `Some` et `None`.
4. Ajoute un finaliseur pour prouver l'interruption de l'appel trop lent.

**Validation :** un appel rapide réussit ; un appel lent retourne `None` et exécute son finaliseur.

---

## Exercice 3 : Le Race (1h)

1. Simule deux services qui renvoient `(source, taux)`.
2. Utilise `race` pour sélectionner le premier succès.
3. Simule ensuite un échec rapide et un succès lent ; vérifie la sémantique observée.
4. Explique quand `raceFirst` serait préférable.

**Validation :** la sortie identifie la source et documente le comportement en cas d'échec.

**Livrable** : Code source démontrant l'usage de `foreachPar`, `timeout` et `race` pour optimiser les performances du moteur.
