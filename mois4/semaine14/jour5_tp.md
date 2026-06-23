# TP Jour 5 : Observer retry et circuit ouvert

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Copie le **Kit 14.5** dans `src/main/scala/distributed/zio/RetryObservation.scala`.
- Le mock est déterministe.
- Le circuit breaker reste une observation simple, pas une implémentation industrielle.

Le but est de voir une politique de retry, une erreur non retentée, et un appel court-circuité.

## Exercice 1 : Compter les retries (10 min)

1. Lance `sbt "runMain distributed.zio.RetryObservation"`.
2. Compte les lignes `appel distant`.
3. Repère le résultat de `retry EUR`.
4. Explique pourquoi le succès arrive au quatrième appel.

**Validation :** `EUR` échoue trois fois, puis réussit.

---

## Exercice 2 : Voir une erreur définitive (8 min)

1. Repère la ligne `retry EURO`.
2. Vérifie qu'elle ne produit pas de nouveaux appels distants.
3. Relie ce comportement à `InvalidCurrency`.

**Validation :** l'erreur définitive n'est pas retentée.

---

## Exercice 3 : Observer un circuit ouvert (10 min)

1. Repère `open <- Ref.make(true)`.
2. Relance et observe `circuit ouvert : aucun appel distant`.
3. Remplace `true` par `false`.
4. Relance et observe qu'un appel distant repart.

**Validation :** le stagiaire sait dire : "un circuit ouvert protège le service distant."

**Livrable court :** une sortie avec retry, une sortie sans retry, et une phrase sur le circuit ouvert.
