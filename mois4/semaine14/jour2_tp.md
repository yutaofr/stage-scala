# TP Jour 2 : Observer le canal `R` sans nouvelle architecture

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Garde le même fichier `ZioEffectObservation.scala`.
- N'ajoute pas de service métier.
- N'ajoute pas de repository.
- Utilise seulement une petite configuration locale d'observation.

Le but est de voir le canal `R` de `ZIO[R, E, A]`.

## Exercice 1 : Ajouter `ObservationConfig` (8 min)

1. Ajoute `ObservationConfig(parallelism, failAuditAfterFirstLine)`.
2. Ajoute `defaultConfig`.
3. Lis le type `ZIO[ObservationConfig, Err, Positions]`.
4. Note ce que représente `R`.

**Validation :** le stagiaire relie `R` à une dépendance explicite, pas à un conteneur magique.

---

## Exercice 2 : Observer `provide` (10 min)

1. Ajoute `.provide(ZLayer.succeed(defaultConfig))` dans `run`.
2. Lance le programme.
3. Retire temporairement `.provide(...)`.
4. Compile et lis l'erreur.
5. Remets `.provide(...)`.

**Validation :** l'erreur de compilation indique qu'une dépendance reste à fournir.

---

## Exercice 3 : Changer une configuration, pas le métier (10 min)

1. Change `parallelism = 2` en `parallelism = 1`.
2. Relance le programme.
3. Vérifie que le résultat métier reste identique.
4. Remets `2`.

**Validation :** seule l'observation change; validation et netting restent intacts.

**Livrable court :** une sortie avec configuration fournie et une phrase sur le canal `R`.
