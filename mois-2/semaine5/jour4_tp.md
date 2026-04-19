# TP Jour 4 : Modélisation par Composition

**Durée :** ~4h | **Fil Rouge :** Passer de la transaction isolée au Lot (Batch) de clearing

---

## Exercice 1 : Le Lot de Clearing (1h30)

1. Crée la structure `ClearingBatch` :
   - Un identifiant (Int).
   - Une liste de `Transaction`.
   - Un `TransactionStatus`.
2. Crée une méthode `totalAmount` dans `ClearingBatch` qui calcule la somme de toutes les transactions présentes dans le lot.

---

## Exercice 2 : Résultat de Compensation (1h30)

1. Crée la structure `ClearingResult` :
   - Le `ClearingBatch` d'origine.
   - Une `Map[String, BigDecimal]` pour les positions nettes.
   - Une `List[ClearingError]` pour les anomalies rencontrées.
2. Implémente une fonction `processBatch(batch: ClearingBatch): ClearingResult`.
3. Elle doit valider les transactions du lot, calculer le netting, et retourner le résultat complet.

---

## Exercice 3 : Matching Imbriqué (1h)

1. Crée une fonction `describeResult(res: ClearingResult): String`.
2. Utilise le pattern matching pour renvoyer :
   - "Succès total" si la liste d'erreurs est vide.
   - "Échec partiel ($n erreurs)" si des erreurs existent.
   - "Batch Invalide" si le batch d'origine était déjà `Rejected`.

**Livrable** : Code source montrant la manipulation de structures imbriquées complexes et un pipeline de traitement typé.
