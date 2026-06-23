# TP Jour 3 : Observer `Scope` sur un audit minimal

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Garde le même module `ZioEffectObservation.scala`.
- Génère l'audit à partir de ton petit batch de référence.
- N'écris pas de parseur CSV complet.
- N'ajoute pas de vrai fichier sur disque.

Le but est de voir que ZIO ferme une ressource en succès comme en erreur.

## Exercice 1 : Observer le chemin nominal (8 min)

1. Ajoute `openAuditReader`.
2. Ajoute `readAuditLines(false)`.
3. Lance ton programme.
4. Repère `audit acquire`, `audit read`, puis `audit release`.

**Validation :** `audit release` apparaît après la lecture.

---

## Exercice 2 : Observer la fermeture en erreur (10 min)

1. Passe `failAfterFirstLine` à `true`.
2. Relance le programme.
3. Observe l'erreur volontaire.
4. Vérifie que `audit release` apparaît quand même.
5. Remets `false`.

**Validation :** la ressource est fermée même quand l'effet échoue.

---

## Exercice 3 : Expliquer le rôle de `Scope` (10 min)

1. Ouvre `openAuditReader`.
2. Ouvre `readAuditLines`.
3. Entoure mentalement la zone couverte par `ZIO.scoped`.
4. Explique pourquoi le `BufferedReader` ne sort pas de cette zone.

**Validation :** le stagiaire sait dire : "`Scope` borne la durée de vie de la ressource."

**Livrable court :** deux sorties console et une phrase sur `Scope`.
