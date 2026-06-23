# TP Jour 3 : Observer `Scope` sur un audit de batch

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Garde le même module `ZioClearingModule.scala`.
- Le CSV d'audit est en memoire pour éviter les problemes de chemin.
- Ne transforme pas l'exercice en parseur CSV complet.

Le but est de voir que ZIO ferme une ressource en succès comme en erreur.

## Exercice 1 : Observer le chemin nominal (8 min)

1. Lance `sbt "runMain distributed.zio.ZioClearingModule"`.
2. Repère `audit acquire`.
3. Repère les lignes `audit read`.
4. Repère `audit release`.

**Validation :** `audit release` apparait apres la lecture.

---

## Exercice 2 : Observer la fermeture en erreur (10 min)

1. Dans `ZioClearingModule`, remplace `readAuditLines(failAfterFirst = false)` par `readAuditLines(failAfterFirst = true)`.
2. Relance le programme.
3. Observe le message `Erreur contrôlée`.
4. Vérifie que `audit release` apparait quand même.
5. Remets `false`.

**Validation :** la ressource est fermée même quand la lecture échoue.

---

## Exercice 3 : Lire le role de `ZIO.scoped` (10 min)

1. Ouvre `openAuditReader`.
2. Ouvre `readAuditLines`.
3. Entoure mentalement la zone couverte par `ZIO.scoped`.
4. Explique pourquoi le `BufferedReader` ne doit pas sortir de cette zone.

**Validation :** le stagiaire sait dire : "Scope borne la durée de vie de la ressource."

**Livrable court :** deux sorties console et une phrase sur `Scope`.
