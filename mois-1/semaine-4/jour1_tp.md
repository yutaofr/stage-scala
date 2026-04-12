# TP Jour 1 : Règles de Validation Configurables

**Durée :** ~4h | **Fil Rouge :** Rendre le validateur du moteur flexible

---

## Exercice 1 : Fonctions en Paramètres (1h)

1. Crée une fonction `filtrerTransactions(txs: List[BigDecimal], critere: BigDecimal => Boolean)`.
2. Appelle cette fonction avec différents critères :
   - Montant pair.
   - Montant supérieur à 1000.
   - Montant compris entre 100 et 500.

---

## Exercice 2 : Configurateur de Frais (Currying) (1h30)

1. Implémente une fonction curryfiée `appliquerFrais(nomBanque: String)(montant: BigDecimal): BigDecimal`.
2. Si la banque est "ATH", les frais sont de 1%, sinon 2%.
3. Crée deux fonctions spécialisées : `fraisATH` et `fraisGenerique`.
4. Applique-les sur une liste de montants.

---

## Exercice 3 : Moteur de Règles (1h30)

1. Définis un type `Rule` comme alias de `BigDecimal => Boolean`.
2. Crée une `List[Rule]` contenant :
   - `nonNul` : montant != 0.
   - `positif` : montant > 0.
   - `seuilAudit` : montant < 100000.
3. Crée une fonction `validateAll(amount: BigDecimal, rules: List[Rule]): Boolean` qui utilise `forall`.
4. Crée une fonction `validateAny(amount: BigDecimal, rules: List[Rule]): Boolean` qui utilise `exists`.

**Livrable** : Code du validateur utilisant des HOF et du currying, avec tests associés.
