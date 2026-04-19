# TP Jour 1 : Hiérarchie d'Erreurs Imbriquée

**Durée :** ~4h | **Fil Rouge :** Un système de reporting d'erreurs professionnel

---

## Exercice 1 : La Super-Hiérarchie (1h30)

1. Dans `ClearingError.scala`, définis la hiérarchie suivante :
   - `sealed trait ClearingError`
   - `sealed trait HighLevelError extends ClearingError` (Ex: Fichier corrompu)
   - `sealed trait LineError extends ClearingError` (Ex: Une transaction spécifique)
2. Ajoute des `case class` concrètes pour chaque catégorie.

---

## Exercice 2 : Filtrage par Catégorie (1h)

1. Crée une `List[ClearingError]` contenant un mélange d'erreurs.
2. Utilise `collect` ou `filter` pour extraire uniquement les `LineError`.
3. Compte combien d'erreurs bloquantes (`HighLevelError`) sont présentes.

---

## Exercice 3 : Reporter de Précision (1h)

1. Implémente une fonction `detailedReport(e: ClearingError): String`.
2. Elle doit utiliser un pattern matching imbriqué pour donner des détails différents selon la catégorie.
3. Exemple : Les `ValidationError` doivent afficher le nom du champ, les `BusinessError` doivent afficher un code erreur interne.

---

## Exercice 4 : Tests d'Exhaustivité (30 min)

1. Ajoute un nouveau trait `SystemError extends ClearingError`.
2. Vérifie que tes fonctions de matching gérant `ClearingError` affichent un warning.
3. Corrige-les.

**Livrable** : Code source avec la nouvelle hiérarchie d'erreurs et reporter associé.
