# TP Jour 4 : Structurer le Moteur avec des Traits

**Durée :** ~4h | **Fil Rouge :** Première architecture modulaire

---

## Exercice 1 : Le Contrat de Clearing (1h30)

1. Crée un fichier `src/main/scala/clearing/ClearingProcessor.scala`.
2. Définis un `trait ClearingProcessor`.
3. Il doit contenir 3 méthodes abstraites :
   - `validate(tx: Transaction): Boolean`
   - `calculate(txs: List[Transaction]): Map[String, BigDecimal]`
   - `report(results: Map[String, BigDecimal]): Unit`

---

## Exercice 2 : Première Implémentation (1h)

1. Crée un `object SimpleClearingProcessor` qui `extends ClearingProcessor`.
2. Implémente les méthodes avec la logique que tu as écrite les jours précédents.
3. Crée une méthode `run(file: String)` qui orchestre tout l'appel (lecture -> validation -> calcul -> rapport).

---

## Exercice 3 : Mix-in de Logging (1h)

1. Crée un `trait Logger` avec une méthode `log(msg: String)`.
2. Modifie `SimpleClearingProcessor` pour qu'il mixe le trait `Logger` :
   `object SimpleClearingProcessor extends ClearingProcessor with Logger`
3. Ajoute des logs au début et à la fin de chaque étape importante.

---

## Exercice 4 : Tests & Contrats (30 min)

1. Crée `ClearingProcessorSpec.scala`.
2. Vérifie que ton processor renvoie les bons résultats pour une petite liste de transactions.
3. Teste que le Logger fonctionne (tu peux simplement vérifier que l'exécution se termine sans erreur).

**Livrable** : Code architecturé proprement utilisant les traits et passant les tests.
