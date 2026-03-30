# TP S2-J2 : Pattern Matching au Service de la Validation

**Durée :** ~4h | **Fil Rouge :** Réécrire le validateur avec du pattern matching

---

## Exercice 1 : Refactoring du Validateur (1h30)

Réécrire `Validator.validateTransaction` avec du pattern matching :

```scala
def validateTransaction(iban: String, bankCode: String, amount: BigDecimal): String =
  (isValidIban(iban), isKnownBank(bankCode), isPositiveAmount(amount)) match
    case (false, _, _)     => "IBAN invalide"
    case (_, false, _)     => "Banque inconnue"
    case (_, _, false)     => "Montant invalide"
    case (true, true, true) => "OK"
```

---

## Exercice 2 : Routeur de Transactions (1h)

```scala
object TransactionRouter:
  def route(tx: (Int, String, String, BigDecimal, String)): String =
    tx match
      // Virement interne (même banque)
      case (_, from, to, _, _) if from == to => ???
      // Virement inter-bancaire important (> 50000)
      case (_, _, _, amount, "VIR") if amount > 50000 => ???
      // Prélèvement
      case (_, _, _, _, "PRE") => ???
      // Cas par défaut
      case _ => ???
```

---

## Exercice 3 : Extracteur de Statistiques (1h)

Utilise le matching pour extraire les données des tuples de manière lisible et produire un rapport complet.

---

## Exercice 4 : Tests (30 min)

Tester chaque branche du routeur avec des transactions ciblées.
