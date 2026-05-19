# TP Jour 4 : Configuration par Currying

**Durée :** ~4h | **Fil Rouge :** Un moteur de clearing multi-banques

---

## Exercice 1 : Logger Configurable (1h)

1. Crée une fonction `logWithBank(bankName: String)(message: String): Unit`.
2. Crée deux fonctions spécialisées : `athLog` et `cihLog` via une application partielle.
3. Utilise-les pour logger les étapes de traitement d'un batch.

---

## Exercice 2 : Validateur de Seuil (1h30)

1. Implémente une fonction `checkLimit(limit: BigDecimal)(tx: Transaction): Boolean`.
2. Crée un pipeline qui utilise une limite différente selon que la transaction est de type `Transfer` ou `Withdrawal`.
3. Indice : Utilise `checkLimit` pour créer deux sous-validateurs spécialisés.

---

## Exercice 3 : Calculateur de Frais Partiel (1h30)

1. Implémente `applyFee(rate: Double)(amount: BigDecimal): BigDecimal`.
2. Imagine que le taux dépend de la banque émettrice.
3. Crée une Map qui associe le code banque à une fonction `BigDecimal => BigDecimal` pré-configurée avec le bon taux.
4. Applique la bonne fonction de frais dynamiquement lors du parcours de ta liste de transactions.

**Livrable** : Code source utilisant le currying pour injecter des seuils et des taux de frais différents dans la logique de processing.
