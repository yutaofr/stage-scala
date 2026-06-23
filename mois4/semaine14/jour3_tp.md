# TP Jour 3 : Observer `Scope` et la fermeture

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Copie le **Kit 14.3** dans `src/main/scala/distributed/zio/ResourceObservation.scala`.
- Le CSV est intégré au snippet pour éviter les problèmes de chemin.
- Ne remplace pas le kit par un gros parseur de fichier.

Le but est de voir que ZIO ferme la ressource en succès comme en échec.

## Exercice 1 : Observer le chemin nominal (8 min)

1. Lance `sbt "runMain distributed.zio.ResourceObservation"`.
2. Repère `acquire reader`.
3. Repère `release reader`.
4. Note l'ordre des messages.

**Validation :** `release reader` apparaît une seule fois après la lecture.

---

## Exercice 2 : Observer la fermeture en erreur (10 min)

1. Dans `run`, remplace `failAfterFirst = false` par `failAfterFirst = true`.
2. Relance.
3. Observe le message d'erreur.
4. Vérifie que `release reader` apparaît quand même.

**Validation :** l'erreur est contrôlée et la ressource est fermée.

---

## Exercice 3 : Lire le rôle de `ZIO.scoped` (10 min)

1. Ouvre `readAll`.
2. Entoure mentalement la zone couverte par `ZIO.scoped`.
3. Explique pourquoi le `reader` ne sort pas de cette zone.
4. Remets `failAfterFirst = false`.

**Validation :** le stagiaire sait dire : "le scope borne la durée de vie de la ressource."

**Livrable court :** deux sorties console et une phrase sur `Scope`.
