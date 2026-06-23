# TP Jour 1 : Brancher ZIO sur ton fil rouge

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Pars de **ton** projet fil rouge construit de S1 à S13.
- Ne pars pas d'un projet fourni par le support.
- Ne recopie pas de domaine prêt à l'emploi.
- Crée un seul fichier d'observation, par exemple `ZioEffectObservation.scala`.

Le but du jour est de vérifier que ZIO se branche sur ton moteur existant sans le remplacer.

## Exercice 1 : Remplir la fiche de correspondance (8 min)

1. Ouvre ton type transaction.
2. Repère ton type d'erreur métier.
3. Repère ta fonction de validation qui retourne `Either`.
4. Repère ta fonction de netting pur.
5. Note ces quatre noms dans la fiche du starter kit.

**Validation :** le stagiaire peut dire : "voici mes noms à moi pour transaction, erreur, validation, netting."

---

## Exercice 2 : Créer le module d'observation (10 min)

1. Crée `ZioEffectObservation.scala` dans ton projet.
2. Ajoute seulement la zone d'adaptation du starter kit.
3. Branche `sampleBatch`, `validateBase`, et `nettingBase` sur ton code existant.
4. Ne modifie pas les fichiers de domaine ou de netting.

**Validation :** le projet compile avec le nouveau fichier vide de logique métier.

---

## Exercice 3 : Comparer Scala de base et ZIO (10 min)

1. Ajoute `basePipeline`.
2. Ajoute `zioPipeline = ZIO.fromEither(basePipeline)`.
3. Lance ton programme avec la commande `runMain` adaptée à ton package.
4. Vérifie que le résultat métier reste le même.

**Validation :** le stagiaire sait dire : "ZIO suspend mon pipeline `Either` dans une description; le métier ne change pas."

**Livrable court :** fiche de correspondance remplie, fichier d'observation créé, une sortie console.
