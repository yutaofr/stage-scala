# TP Jour 5 : Clearing Engine v0.3

**Durée :** ~4h | **Fil Rouge :** Assembler le premier moteur 100% fonctionnel

---

## Exercice 1 : Nettoyage et Intégration (2h)

1. Reprends tous les modules écrits depuis le début du stage.
2. Refactorise-les pour utiliser uniquement des collections immutables et des pipelines (`map`, `filter`, `foldLeft`).
3. Plus aucun `var` ne doit être présent dans ton code.

---

## Exercice 2 : Le Pipeline Complet v0.3 (1h30)

Crée un `object MainV03` qui exécute les étapes suivantes :
1. **Lecture** : Récupérer les lignes CSV (depuis un fichier ou une liste hardcodée).
2. **Parsing** : Transformer les lignes en tuples via `Transaction.apply`.
3. **Filtrage** : Supprimer les montants négatifs et les banques inconnues.
4. **Calcul** : Utiliser `NettingCalculator.calculate(transactions)` pour obtenir les positions nettes.
5. **Rapport** : Afficher un résumé trié par nom de banque (A-Z).

---

## Exercice 3 : Tests de Non-Régression (30 min)

1. Assure-toi que toutes les fonctionnalités de la S1 et S2 marchent toujours.
2. Ajoute un test d'intégration qui valide un scénario complet (10 transactions -> solde net final).

**Bilan Mois 1 - Semaine 3** : Ton moteur est maintenant une machine à traiter des données purement fonctionnelle. Il est robuste, testable et performant.

**Livrable** : Code source complet v0.3 et démonstration au tuteur.
