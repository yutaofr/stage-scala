# TP Jour 3 : Pipelines & Filtrage Typé

**Durée :** ~4h | **Fil Rouge :** Transformer les lignes brutes en résultats agrégés

---

## Exercice 1 : Filtrage par Pattern (1h)

1. Tu as une liste hétérogène `List[ClearingError]`.
2. Utilise une `for-comprehension` pour extraire uniquement les messages des `ValidationError`.
3. Utilise une `for-comprehension` pour extraire les codes des `BusinessError`.

---

## Exercice 2 : Pipeline Monadique (1h30)

1. Implémente trois fonctions :
   - `fetchTransaction(id: Int): Option[Transaction]`
   - `fetchExchangeRate(currency: String): Option[BigDecimal]`
   - `calculateFees(amount: BigDecimal): Option[BigDecimal]`
2. Utilise un `for-yield` pour calculer le montant final avec frais et conversion.
3. Si une donnée manque (None), le résultat final doit être `None`.

---

## Exercice 3 : Collecte de Batch (1h30)

1. Prends une `List[String]` représentant un fichier CSV.
2. Écris un pipeline `for-yield` qui :
   - Parse chaque ligne (Option).
   - Ne garde que les transactions dont le montant est > 0.
   - Ne garde que les transactions de type `Transfer`.
3. Retourne une `List[Transaction]` propre.

**Livrable** : Code source utilisant intensivement les for-comprehensions pour le filtrage et le chaînage d'Options.
