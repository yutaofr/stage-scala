# TP Jour 3 : Le Vrai Netting avec Fold

**Durée :** ~4h | **Fil Rouge :** Calculer les positions nettes interbancaires de manière pure

---

## Exercice 1 : Somme et Moyenne (1h)

1. Reçois une liste de montants : `List(1500.50, 2000.0, 500.75, 100.0)`.
2. Utilise `reduce` pour calculer la somme totale.
3. Utilise `foldLeft` pour calculer à la fois la somme et le nombre d'éléments en UN SEUL passage (retourne un tuple `(Somme, Count)`).
4. Déduis-en la moyenne.

---

## Exercice 2 : Le Calculateur de Netting (2h) - TP MAJEUR

1. Tu as une liste de transactions : `List(("ATH", "CIH", 100), ("CIH", "ATH", 40), ("ATH", "BOA", 50))`.
2. Utilise `foldLeft` pour créer une `Map[String, BigDecimal]` qui contient les positions nettes de chaque banque.
   - Si une banque envoie, on soustrait.
   - Si une banque reçoit, on ajoute.
3. **Contrainte** : Interdiction d'utiliser `var` ou des boucles `while`.
4. Vérifie que la somme de toutes les positions nettes dans la Map finale est égale à **ZÉRO** (Propriété fondamentale de la compensation).

---

## Exercice 3 : Statistiques par Banque (1h)

1. À partir du `Map` généré par `groupBy` au Jour 1, utilise `mapValues` et `foldLeft` pour produire un rapport :
   - Montant max envoyé par banque.
   - Montant total envoyé par banque.
   - Nombre de transactions par banque.

---

## Exercice 4 : Robustesse (30 min)

1. Teste tes fonctions sur une liste de transactions vide.
2. Assure-toi que ton code ne crashe pas et retourne des valeurs cohérentes (ex: 0).

**Livrable** : Code du `NettingCalculator` utilisant `foldLeft` et `mapValues`, avec tests unitaires.
