# TP Jour 1 : Reprendre le fil rouge avec un petit module ZIO

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Pars du projet `fil-rouge`.
- Vérifie que `clearing.model`, `clearing.core` et `distributed.zio.ClearingServices` existent déjà.
- Copie le module du starter kit dans `src/main/scala/distributed/zio/ZioClearingModule.scala`.
- Lance `sbt "runMain distributed.zio.ZioClearingModule"`.

Le but du jour n'est pas d'écrire une application ZIO complète. Le but est de voir que ZIO se branche autour du moteur existant.

## Exercice 1 : Vérifier que le cœur n'a pas changé (8 min)

1. Ouvre `ZioClearingModule.scala`.
2. Repère les imports `clearing.core.*` et `clearing.model.*`.
3. Repère `baseScalaNetting`.
4. Lance le programme et compare les blocs `Scala base` et `ZIO module`.

**Validation :** les positions nettes affichées sont identiques dans les deux blocs.

---

## Exercice 2 : Lire le type `ZIO[R, E, A]` (10 min)

1. Repère `validateAndNetting`.
2. Lis son type complet.
3. Note ce que représentent `R`, `E` et `A`.
4. Explique pourquoi `ValidationService & NettingService` n'est pas dans la version Scala de base.

**Validation :** le stagiaire sait dire : "ZIO ajoute les dépendances explicites; il ne change pas le calcul pur."

---

## Exercice 3 : Modifier seulement les données d'exemple (10 min)

1. Dans `sampleBatch`, remplace `40.00` par `70.00`.
2. Relance le programme.
3. Vérifie que `Scala base` et `ZIO module` changent de la même façon.
4. Remets `40.00`.

**Validation :** le changement reste local au batch d'exemple; aucun fichier `clearing.*` n'est modifié.

**Livrable court :** une sortie console et trois phrases : cœur intact, type ZIO lu, résultat Scala/ZIO cohérent.
