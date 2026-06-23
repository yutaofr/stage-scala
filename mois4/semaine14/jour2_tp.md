# TP Jour 2 : Observer `ZLayer`

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Copie le **Kit 14.2** dans `src/main/scala/distributed/zio/ZLayerObservation.scala`.
- Ne crée pas de nouvelle interface.
- Ne recopie pas les types du domaine.

Le but est de voir le canal `R` et l'assemblage par `provide`, pas de concevoir une architecture complète.

## Exercice 1 : Lire les dépendances dans le type (8 min)

1. Ouvre `program`.
2. Repère son type : `ZIO[ValidationService & NettingService, ClearingError, Map[BankCode, Money]]`.
3. Lance `sbt "runMain distributed.zio.ZLayerObservation"`.
4. Note les deux services demandés par le programme.

**Validation :** le rapport affiche `AWB` et `CIH`.

---

## Exercice 2 : Observer une erreur métier (10 min)

1. Dans `knownBanks`, retire `cih`.
2. Relance le programme.
3. Observe la ligne courte `Erreur métier : UNKNOWN_BANK - ...`.
4. Remets `cih`.

**Validation :** le stagiaire relie l'erreur au canal `E`, pas à une exception cachée.

---

## Exercice 3 : Observer l'injection (10 min)

1. Commente temporairement `NettingService.live` dans `provide`.
2. Compile.
3. Lis le message : quelle dépendance manque ?
4. Rétablis la ligne.

**Validation :** l'erreur de compilation mentionne le service non fourni.

**Livrable court :** une sortie réussie, une erreur métier, et une erreur de compilation expliquée en une phrase.
