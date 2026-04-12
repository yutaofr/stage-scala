# TP Jour 5 : Clearing Engine v0.4 — Prototype Complet

**Durée :** ~4h | **Fil Rouge :** Démonstration finale du premier mois

---

## Exercice 1 : Intégration Finale (2h)

1. Rassemble toutes les compétences acquises :
   - Lecture de fichier CSV.
   - Parsing résilient via `Option`.
   - Validation dynamique via un moteur de règles (HOF).
   - Calcul de netting global via `foldLeft`.
   - Utilisation de `view` pour optimiser le traitement.
2. Ton moteur ne doit contenir aucun `null`, aucun `var`, aucun `while`.

---

## Exercice 2 : Documentation & Architecture (1h)

1. Rédige un court README expliquant comment lancer ton moteur.
2. Dessine (ou décris par texte) le chemin d'une donnée depuis le fichier CSV jusqu'au rapport final.
3. Identifie les points du code qui te semblent encore fragiles (ex: les tuples anonymes).

---

## Exercice 3 : Démo au Tuteur (1h)

1. Prépare un jeu de données contenant :
   - Des transactions normales.
   - Des transactions malformées (montants textes, colonnes manquantes).
   - Des transactions interdites (montants négatifs).
2. Lance le moteur et montre que :
   - Les erreurs sont gérées gracieusement (Option).
   - Le résultat final est mathématiquement correct.
   - Le code est propre et respecte les standards Scala 3.

**Bilan Mois 1** : Objectifs atteints. Le stagiaire maîtrise les bases de la FP et a construit un prototype solide.

**Livrable** : Code source complet v0.4 taggé, documenté et testé.
