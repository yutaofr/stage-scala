# TP Jour 2 : Le Calculateur Multilatéral

**Durée :** ~4h | **Fil Rouge :** Implémenter le cœur algorithmique du clearing

---

## Exercice 1 : Calcul des Positions (2h)

1. Crée un objet `MultilateralNetting`.
2. Implémente la méthode `computePositions(txs: List[Transaction]): Map[String, BigDecimal]`.
3. Utilise un `foldLeft` pour parcourir la liste une seule fois et mettre à jour les soldes de l'expéditeur et du destinataire.
4. **Contrainte** : Ne pas utiliser de `var`.

---

## Exercice 2 : Vérificateur d'Équilibre (1h)

1. Crée une fonction `isBalanced(positions: Map[String, BigDecimal]): Boolean`.
2. Elle doit sommer toutes les valeurs de la Map et vérifier que le résultat est 0 (attention à la précision des `BigDecimal`).
3. Ajoute un log d'erreur si l'équilibre n'est pas respecté.

---

## Exercice 3 : Rapport de Règlement (1h)

1. Crée une fonction qui trie les banques :
   - D'abord les débitrices (celles qui doivent payer).
   - Ensuite les créditrices (celles qui vont recevoir).
2. Affiche un rapport clair :
   `BANQUES DÉBITRICES :`
   `- CIH : -250 000 DH`
   `BANQUES CRÉDITRICES :`
   `- ATH : +250 000 DH`

**Livrable** : Code source de l'algorithme multilatéral avec tests unitaires prouvant l'équilibre du système sur des jeux de données complexes.
