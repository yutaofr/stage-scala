# TP Jour 5 : Observer un retry borné

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Garde le même module `ZioEffectObservation.scala`.
- Utilise ton type d'erreur existant.
- Si ton projet a déjà une erreur technique, utilise-la.
- Sinon, ajoute une erreur locale **dans le module d'observation seulement**.
- Ne crée pas de vrai client HTTP et ne code pas de circuit breaker.

Le but est de voir une politique de retry courte, pas de concevoir une résilience industrielle.

## Exercice 1 : Définir l'erreur temporaire (8 min)

1. Ajoute `isTemporary(error: Err): Boolean`.
2. Ajoute `temporaryFailure(message): Err`.
3. Branche ces deux fonctions sur ton ADT d'erreur.
4. Si tu hésites, demande au tuteur le mapping exact.

**Validation :** le stagiaire sait quelle erreur peut être retentée.

---

## Exercice 2 : Compter les retries (10 min)

1. Ajoute `retryTemporaryOnly`.
2. Ajoute `publishObservation`.
3. Lance le programme.
4. Compte les lignes `publication observée #...`.

**Validation :** la publication échoue temporairement, puis réussit après retry.

---

## Exercice 3 : Vérifier qu'une erreur métier n'est pas retentée (10 min)

1. Ajoute `businessFailureObservation` avec une erreur métier de référence.
2. Applique `.retry(retryTemporaryOnly)` sur cette observation, comme dans le starter kit.
3. Relance le programme.
4. Vérifie que cette erreur n'est pas répétée plusieurs fois.
5. Explique pourquoi `isTemporary` retourne `false`.

**Validation :** le stagiaire distingue une panne technique temporaire d'une erreur métier définitive.

**Livrable court :** une sortie avec retry, une sortie sans retry métier, et une phrase sur `Schedule`.
