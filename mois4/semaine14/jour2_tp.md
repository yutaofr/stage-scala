# TP Jour 2 : Observer `ZLayer` sans reconstruire l'architecture

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Garde le même fichier `src/main/scala/distributed/zio/ZioClearingModule.scala`.
- Ne crée pas de nouveau service.
- Ne recopie pas les types du domaine.
- Lance `sbt "runMain distributed.zio.ZioClearingModule"` avant toute modification.

Le but est de voir le canal `R` et l'assemblage par `provide`.

## Exercice 1 : Lire les dépendances dans le type (8 min)

1. Ouvre `validateAndNetting`.
2. Repère `ZIO[ValidationService & NettingService, ClearingError, Map[BankCode, Money]]`.
3. Repère `ZIO.service[ValidationService]` et `ZIO.service[NettingService]`.
4. Note les deux services demandés par le programme.

**Validation :** le stagiaire relie le canal `R` aux services ZIO existants.

---

## Exercice 2 : Observer l'injection au bord du programme (10 min)

1. Repère `val services = ValidationService.live(knownBanks) ++ NettingService.live`.
2. Commente temporairement `++ NettingService.live`.
3. Compile.
4. Lis l'erreur : quelle dépendance manque ?
5. Rétablis la ligne.

**Validation :** l'erreur de compilation montre qu'une dépendance du canal `R` n'a pas été fournie.

---

## Exercice 3 : Observer une erreur métier contrôlée (10 min)

1. Dans `knownBanks`, retire `cih`.
2. Relance le programme.
3. Observe `Erreur contrôlée: UNKNOWN_BANK - ...`.
4. Remets `cih`.

**Validation :** l'erreur vient de `ClearingError`, donc du domaine existant, pas d'une exception cachée.

**Livrable court :** une sortie réussie, une erreur de compilation expliquée, et une erreur métier expliquée.
