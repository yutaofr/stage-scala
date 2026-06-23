# TP Jour 5 : Observer un retry borné, sans circuit breaker

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Garde le même module `ZioClearingModule.scala`.
- Le retry utilise les erreurs du fil rouge : `ClearingError.InfrastructureFailure` et `UNKNOWN_BANK`.
- Ne crée pas de vrai client HTTP.
- Ne code pas de circuit breaker cette semaine.

Le but est de voir une politique de retry courte sur une panne d'infrastructure, puis une erreur métier définitive non retentée.

## Exercice 1 : Compter les retries d'infrastructure (10 min)

1. Lance `sbt "runMain distributed.zio.ZioClearingModule"`.
2. Compte les lignes `publication clearing #...`.
3. Repère `retry publication: Right(publication OK (2 positions))`.
4. Explique pourquoi le succès arrive au troisième appel.

**Validation :** la publication échoue deux fois avec `InfrastructureFailure`, puis réussit.

---

## Exercice 2 : Voir une erreur métier non retentée (8 min)

1. Repère `erreur métier: UNKNOWN_BANK`.
2. Repère `retry UNKNOWN_BANK: Left(...)`.
3. Vérifie qu'il n'y a pas plusieurs lignes `erreur métier: UNKNOWN_BANK`.
4. Relie ce comportement à `infrastructureOnly`.

**Validation :** `UNKNOWN_BANK` n'est pas retenté, car ce n'est pas une panne d'infrastructure.

---

## Exercice 3 : Changer la politique de retry (10 min)

1. Dans `infrastructureOnly`, remplace `Schedule.recurs(3)` par `Schedule.recurs(1)`.
2. Relance le programme.
3. Observe que la publication ne réussit plus.
4. Remets `Schedule.recurs(3)`.

**Validation :** le stagiaire voit que la politique de retry est une valeur modifiable, pas une boucle cachée dans le service.

**Livrable court :** une sortie avec succès, une sortie avec retry trop court, et une phrase sur `Schedule`.
