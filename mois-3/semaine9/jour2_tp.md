# TP Jour 2 : Pureté & Immuabilité Totale

**Durée :** ~4h | **Fil Rouge :** Le NettingCalculator Mathématique

---

## Exercice 1 : Conversion Séquentielle (1h30)

1. Prends ton code de calcul de netting de la semaine 7 (celui qui utilisait peut-être des `var` ou des `println` au milieu).
2. Réécris-le en utilisant uniquement des méthodes de collections pures (`foldLeft`, `mapValues`, etc.).
3. Vérifie qu'il ne reste aucun `var` dans tout le fichier de calcul.

---

## Exercice 2 : Test de Déterminisme (1h)

1. Crée un test unitaire qui appelle ta fonction de netting 1000 fois avec les mêmes arguments.
2. Vérifie (par programmation) que le résultat est **strictement identique** à chaque fois.
3. Insère un `println` ou un `var` au milieu et observe si tu peux encore garantir la même propreté (non, mais le test le prouvera).

---

## Exercice 3 : Pureté du Domaine (1h30)

1. Relis tes `case class` de domaine.
2. Assure-toi qu'aucune méthode à l'intérieur ne fait d'affichage ou de modification d'état.
3. Exemple : La méthode `Transaction.isValid` doit être une pure évaluation de prédicat.

**Livrable** : Code source du `NettingCalculator` 100% pur et immuable, accompagné de ses tests de déterminisme.
