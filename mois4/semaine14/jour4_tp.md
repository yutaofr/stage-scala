# TP Jour 4 : Observer les fibers et le parallèle borné

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Copie le **Kit 14.4** dans `src/main/scala/distributed/zio/ParallelObservation.scala`.
- Garde les mêmes batchs pour comparer les mesures.
- Ne cherche pas une mesure exacte : observe l'ordre de grandeur.

Le but est de voir `foreachPar`, `withParallelism`, et `timeout`.

## Exercice 1 : Comparer séquentiel et parallèle (8 min)

1. Lance `sbt "runMain distributed.zio.ParallelObservation"`.
2. Note la durée `séquentiel`.
3. Note la durée `parallèle x2`.
4. Explique pourquoi quatre batchs de 500 ms prennent environ deux vagues en parallèle x2.

**Validation :** la version parallèle est plus courte que la version séquentielle.

---

## Exercice 2 : Changer la borne (10 min)

1. Remplace `withParallelism(2)` par `withParallelism(4)`.
2. Relance.
3. Compare avec la mesure précédente.
4. Remets `2` si le tuteur veut garder la version de base.

**Validation :** la durée baisse car les quatre batchs peuvent partir ensemble.

---

## Exercice 3 : Observer un timeout (10 min)

1. Garde `Batch("lent", 3.seconds)`.
2. Observe `timeout : None`.
3. Remplace `timeout(1.second)` par `timeout(4.seconds)`.
4. Relance et observe `Some(...)`.

**Validation :** le stagiaire sait dire : "`timeout` transforme un effet trop lent en absence de résultat."

**Livrable court :** trois mesures et une phrase sur le parallèle borné.
