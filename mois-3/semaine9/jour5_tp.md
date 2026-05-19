# TP Jour 5 : Clearing Engine v2.0 — Le Cœur Pur

**Durée :** ~4h | **Fil Rouge :** Refactoring complet vers la pureté et la composition

---

## Exercice 1 : Modularisation Totale (2h)

1. Isole ta logique de calcul dans une classe `PureClearingEngine`.
2. Elle ne doit contenir aucune dépendance à Spring, aucun accès fichier, aucun affichage.
3. Définis chaque étape du clearing comme une `val` de type fonction (ex: `val validator: Transaction => Transaction`).
4. Assemble le tout via `andThen`.

---

## Exercice 2 : Injection de Configuration (1h30)

1. Utilise le currying de la veille pour injecter :
   - Le seuil de validation.
   - Le taux de frais.
2. Ton application principale doit "préparer" le moteur en fournissant ces configurations avant de lancer le traitement.

---

## Exercice 3 : Démonstration du Déterminisme (30 min)

1. Montre que pour un même batch d'entrée, ton moteur v2.0 produit **exactement** le même objet de résultat, à l'octet près, sans aucun effet de bord dans la console.

**Bilan Mois 3 - Semaine 9** : La transition vers la FP est réussie. Le code est 3x plus court, plus lisible et 100% testable.

**Livrable** : Code source v2.0 complet, basé sur la composition de fonctions pures.
