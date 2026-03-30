# TP Jour 4 : Fonctions Réutilisables pour le Moteur de Clearing

**Durée :** ~4h | **Fil Rouge :** Construire les signatures et les fonctions utilitaires du moteur

---

## Exercice 1 : Le Module de Validation (1h)

Crée `src/main/scala/clearing/Validator.scala` :

```scala
package clearing

object Validator:
  /** Vérifie qu'un montant est strictement positif */
  def isPositiveAmount(amount: BigDecimal): Boolean = ???

  /** Vérifie qu'un IBAN a le bon format (commence par 2 lettres, suivi de chiffres, longueur 24) */
  def isValidIban(iban: String): Boolean = ???

  /** Vérifie qu'un code banque fait partie de la liste connue */
  def isKnownBank(bankCode: String, knownBanks: List[String] = List("ATH", "CIH", "BOA", "BMCE", "SGMB")): Boolean = ???

  /** Valide une transaction complète. Retourne un message d'erreur ou "OK" */
  def validateTransaction(iban: String, bankCode: String, amount: BigDecimal): String =
    if !isPositiveAmount(amount) then "Montant invalide"
    else if !isValidIban(iban) then "IBAN invalide"
    else if !isKnownBank(bankCode) then "Banque inconnue"
    else "OK"
```

**Livrable :** Toutes les fonctions implémentées.

---

## Exercice 2 : Le Calculateur de Position Nette (1h30)

Crée `src/main/scala/clearing/NettingCalculator.scala` :

```scala
package clearing

object NettingCalculator:
  /** Calcule le solde net à partir d'une liste de montants
    * (positif = crédit, négatif = débit) */
  def netBalance(transactions: List[BigDecimal]): BigDecimal = ???

  /** Calcule le nombre de crédits et de débits */
  def countBySign(transactions: List[BigDecimal]): (Int, Int) =
    val credits = ???  // nombre de positifs
    val debits  = ???  // nombre de négatifs
    (credits, debits)

  /** Génère un résumé textuel */
  def summary(bankName: String, transactions: List[BigDecimal]): String =
    val net = netBalance(transactions)
    val (c, d) = countBySign(transactions)
    val position = if net >= 0 then "CRÉDITRICE" else "DÉBITRICE"
    s"""
    |=== Résumé $bankName ===
    |Nombre de crédits : $c
    |Nombre de débits  : $d
    |Solde net          : $net DH
    |Position           : $position
    """.stripMargin
```

**Livrable :** Code fonctionnel.

---

## Exercice 3 : Fonction Récursive — Recherche de Transaction (1h)

```scala
package clearing

object TransactionSearch:
  /** Recherche récursive du premier montant supérieur à un seuil */
  def findFirstAbove(
    transactions: List[BigDecimal],
    threshold: BigDecimal
  ): Option[BigDecimal] =   // On verra Option en détail plus tard !
    transactions match
      case Nil => None
      case head :: tail =>
        if head > threshold then Some(head)
        else findFirstAbove(tail, threshold)
```

**Tâches :**
1. Implémente une variante `countAbove` (nombre de transactions au-dessus du seuil) en récursion.
2. Implémente une variante `sumAbove` (somme des transactions au-dessus du seuil).

---

## Exercice 4 : Test Suite Complète (30 min)

Crée `src/test/scala/clearing/ValidatorSpec.scala` et `NettingCalculatorSpec.scala` :
- **Validator** : Tester chaque règle de validation.
- **NettingCalculator** : Tester avec des listes vides, une seule valeur, et des cas mixtes.

**Livrable :** Tous les tests verts.
