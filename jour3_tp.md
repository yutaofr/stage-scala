# TP Jour 3 : Conditions & Validation de Transactions

**Durée :** ~4h | **Fil Rouge :** Catégoriser et filtrer les transactions selon des règles métier

---

## Exercice 1 : Catégoriseur de Transactions (1h)

Crée `src/main/scala/clearing/TransactionCategorizer.scala` :

```scala
package clearing

object TransactionCategorizer:
  def categorize(amount: BigDecimal): String =
    // Utilise un `match` avec gardes pour retourner :
    // - "Rejetée" si montant <= 0
    // - "Micro" si montant < 100
    // - "Standard" si montant < 10_000
    // - "Importante" si montant < 100_000
    // - "Exceptionnelle" sinon
    ???

  def describeType(code: String): String =
    // Utilise un `match` pour mapper :
    // "VIR" -> "Virement", "PRE" -> "Prélèvement",
    // "CHQ" -> "Chèque",  "CB"  -> "Carte Bancaire"
    // tout autre code -> "Inconnu"
    ???
```

**Livrable :** Code et tests.

---

## Exercice 2 : Générateur de Transactions Aléatoires (1h)

```scala
package clearing

import scala.util.Random

object TransactionGenerator:
  val banks = List("ATH", "CIH", "BOA", "BMCE", "SGMB")
  val types = List("VIR", "PRE", "CHQ", "CB")

  def generateAmount(): BigDecimal =
    BigDecimal(Random.nextDouble() * 50000 - 10000)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def generateBatch(n: Int): List[(String, String, BigDecimal)] =
    // Retourne une liste de tuples (banque, type, montant)
    ???

  @main def generate(): Unit =
    val batch = generateBatch(20)
    for (bank, txType, amount) <- batch do
      val cat = TransactionCategorizer.categorize(amount)
      println(f"$bank%-6s | ${TransactionCategorizer.describeType(txType)}%-15s | $amount%12.2f DH | $cat")
```

**Livrable :** Affichage tabulaire de 20 transactions avec catégorie.

---

## Exercice 3 : Filtre Business (1h)

Utilise les `for-yield` pour filtrer le batch :

```scala
object TransactionFilter:
  def filterValid(batch: List[(String, String, BigDecimal)]): List[(String, String, BigDecimal)] =
    // Ne garder que les transactions avec montant > 0
    ???

  def filterByBank(batch: List[(String, String, BigDecimal)], bank: String): List[(String, String, BigDecimal)] =
    // Ne garder que les transactions d'une banque donnée
    ???

  def sumByBank(batch: List[(String, String, BigDecimal)]): Unit =
    // Pour chaque banque, afficher le total des montants
    ???
```

**Livrable :** Code fonctionnel, testé avec le batch de 20 transactions.

---

## Exercice 4 : Tests (1h)

Écris des tests pour `TransactionCategorizer` :
- Un test par catégorie de montant.
- Un test pour chaque code de type de transaction.
- Un test pour un code inconnu.

**Livrable :** Suite de tests complète, tous verts.
