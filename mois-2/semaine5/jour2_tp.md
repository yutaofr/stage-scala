# TP Jour 2 : Enums & Hiérarchies d'Erreurs

**Durée :** ~4h | **Fil Rouge :** Sécuriser les états et les échecs du clearing

---

## Exercice 1 : Sécurisation des Statuts (1h)

1. Crée deux `enum` : `TransactionStatus` (Pending, Validated, Rejected) et `TransactionType` (Transfer, Withdrawal, Check).
2. Ajoute ces deux champs à ta `case class Transaction`.
3. Modifie ton générateur pour qu'il tire au sort un type de transaction.

---

## Exercice 2 : Hiérarchie d'Erreurs (1h)

1. Définis `sealed trait ClearingError`.
2. Ajoute les cas suivants :
   - `case class InvalidAmount(amount: BigDecimal)`
   - `case class UnknownBank(code: String)`
   - `case object DuplicateTransaction`
3. Crée une fonction `formatError(error: ClearingError): String` qui utilise le pattern matching pour renvoyer un message lisible.

---

## Exercice 3 : Validateur Typé (1h)

1. Réécris ton `Validator` pour qu'il retourne une `List[ClearingError]`.
2. Si une transaction est valide, la liste est vide.
3. Ajoute des règles :
   - Si montant <= 0 -> `InvalidAmount`.
   - Si banque source n'est pas dans le répertoire -> `UnknownBank`.

---

## Exercice 4 : Tests de Typage (1h)

1. Écris un test qui essaie de créer une transaction avec un statut invalide (tu verras que c'est impossible au niveau compilation !).
2. Teste le validateur et vérifie que tu reçois bien le bon type d'erreur métier.

**Livrable** : Code source utilisant des enums pour les états et un sealed trait pour les erreurs, avec tests unitaires.
