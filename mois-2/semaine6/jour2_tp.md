# TP Jour 2 : Gardes & Extracteurs Personnalisés

**Durée :** ~4h | **Fil Rouge :** Détecter la fraude et les anomalies de clearing

---

## Exercice 1 : Gardes de Validation (1h30)

1. Modifie ton `TransactionValidator`.
2. Utilise le pattern matching avec des gardes pour détecter :
   - Les transactions de montant nul.
   - Les transactions dont l'IBAN source et destination sont identiques.
   - Les transactions suspectes (ex: montant exact de 9999.99).

---

## Exercice 2 : L'Extracteur de Fraude (1h30)

1. Crée un objet `FraudDetector`.
2. Implémente `def unapply(tx: Transaction): Option[String]` (retourne la raison de la suspicion).
3. Critères de fraude :
   - Montant > 1 000 000 DH.
   - OU IBAN commençant par "XX" (simulateur de liste noire).
4. Utilise cet extracteur dans un `match` pour logger les alertes.

---

## Exercice 3 : Extracteur de Devise (1h)

1. Crée un extracteur `InternationalTx` qui détecte si l'IBAN ne commence pas par "MA".
2. Dans ton pipeline, utilise-le pour appliquer automatiquement un frais de change de 2%.

**Livrable** : Code source utilisant des extracteurs personnalisés et des gardes pour une logique métier avancée.
