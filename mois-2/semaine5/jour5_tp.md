# TP Jour 5 : Clearing Engine v1.0 — La Migration Finale

**Durée :** ~4h | **Fil Rouge :** Livraison du prototype architecturé

---

## Exercice 1 : Nettoyage & Refactoring (2h)

1. Parcours l'intégralité de ton projet.
2. Assure-toi qu'il ne reste **aucun Tuple** anonyme dans la logique métier.
3. Vérifie que tous les statuts utilisent des `enum`.
4. Vérifie que toutes les erreurs de validation utilisent la hiérarchie `ClearingError`.

---

## Exercice 2 : Assemblage v1.0 (1h30)

1. Crée un `object ClearingAppV10`.
2. Il doit utiliser le pipeline complet :
   - `Source -> List[String]`
   - `List[String] -> List[Transaction]` (Parsing csv)
   - `List[Transaction] -> (Valid, Invalid)` (Validation)
   - `Valid -> ClearingResult` (Netting)
   - `(ClearingResult, Invalid) -> Console Report` (Final)

---

## Exercice 3 : Revue de Code (30 min)

1. Prépare 3 points forts de ton architecture à présenter.
2. Identifie une zone de code qui pourrait encore être améliorée (préparation pour la S6).

**Bilan Mois 2 - Semaine 5** : Le moteur est passé à l'âge adulte. La modélisation ADT est le socle de tout ce qui va suivre (Concurrence, Distribution, Persistance).

**Livrable** : Code source complet v1.0, tags Git posés, et tests verts.
