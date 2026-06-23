# TP Jour 4 : Observer le parallèle borné

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Garde le même module `ZioEffectObservation.scala`.
- Réutilise ton `sampleBatch`.
- Ne crée pas de pool de threads.
- Ne cherche pas une mesure exacte : observe l'ordre de grandeur.

Le but est de voir `foreach`, `foreachPar`, et `withParallelism`.

## Exercice 1 : Comparer séquentiel et parallèle (8 min)

1. Ajoute `validateSlow`.
2. Ajoute `parallelPreview`.
3. Lance le programme.
4. Compare les durées `séquentiel` et `parallèle`.

**Validation :** la version parallèle est plus courte si le batch contient plusieurs éléments.

---

## Exercice 2 : Changer la borne (10 min)

1. Passe `parallelism` de `2` à `1`.
2. Relance le programme.
3. Remets `2`, puis relance.
4. Compare les deux sorties.

**Validation :** `withParallelism` change l'orchestration, pas le résultat métier.

---

## Exercice 3 : Ajouter un timeout court (10 min)

1. Ajoute un effet lent avec `ZIO.sleep(3.seconds)`.
2. Applique `.timeout(1.second)`.
3. Observe `None`.
4. Passe le timeout à `4.seconds` et observe `Some(...)`.

**Validation :** le stagiaire sait dire : "`timeout` transforme un effet trop lent en absence de résultat."

**Livrable court :** deux mesures et une phrase sur le parallèle borné.
