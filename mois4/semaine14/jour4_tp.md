# TP Jour 4 : Observer le parallèle borne dans le module fil rouge

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Garde le même module `ZioClearingModule.scala`.
- Ne crée pas de pool de threads.
- Ne cherche pas une mesure exacte : observe l'ordre de grandeur.

Le but est de voir `foreachPar`, `withParallelism` et `timeout` sur une simulation courte de validations.

## Exercice 1 : Comparer séquentiel et parallèle (8 min)

1. Lance `sbt "runMain distributed.zio.ZioClearingModule"`.
2. Note la durée `séquentiel`.
3. Note la durée `parallèle x2`.
4. Explique pourquoi quatre validations de 400 ms prennent environ deux vagues avec `withParallelism(2)`.

**Validation :** la version parallèle est plus courte que la version séquentielle.

---

## Exercice 2 : Changer la borne sans changer l'architecture (10 min)

1. Remplace `withParallelism(2)` par `withParallelism(4)`.
2. Relance le programme.
3. Compare avec la mesure précédente.
4. Remets `2`.

**Validation :** la durée baisse car les quatre validations peuvent partir ensemble.

---

## Exercice 3 : Observer un timeout (10 min)

1. Repère `timeoutPreview`.
2. Observe `timeout: None`.
3. Remplace `timeout(1.second)` par `timeout(4.seconds)`.
4. Relance et observe `Some(validation-lente ok)`.
5. Remets `1.second`.

**Validation :** le stagiaire sait dire : "`timeout` transforme un effet trop lent en absence de résultat."

**Livrable court :** trois mesures et une phrase sur le parallèle borne.
