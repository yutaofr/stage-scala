# TP Jour 2 : Types & Précision Financière

**Durée :** ~4h | **Fil Rouge :** Représenter correctement les montants de transactions

---

## Exercice 1 : Le Piège du Double (45 min)

Crée `src/main/scala/clearing/PrecisionDemo.scala` :

```scala
package clearing

object PrecisionDemo:
  @main def testPrecision(): Unit =
    val a: Double = 0.1
    val b: Double = 0.2
    val sumDouble = a + b
    println(s"Double : 0.1 + 0.2 = $sumDouble")
    println(s"Égal à 0.3 ? ${sumDouble == 0.3}")

    val x = BigDecimal("0.1")
    val y = BigDecimal("0.2")
    val sumBD = x + y
    println(s"BigDecimal : 0.1 + 0.2 = $sumBD")
    println(s"Égal à 0.3 ? ${sumBD == BigDecimal("0.3")}")
```

**Questions :**
1. Exécute le programme. Que remarques-tu ?
2. Pourquoi ne jamais utiliser `Double` pour de l'argent ?

---

## Exercice 2 : Le Convertisseur de Devises (1h)

Crée `src/main/scala/clearing/CurrencyConverter.scala` :

```scala
package clearing

object CurrencyConverter:
  val MAD_TO_EUR: BigDecimal = BigDecimal("0.092")
  val MAD_TO_USD: BigDecimal = BigDecimal("0.099")

  def convert(amount: BigDecimal, rate: BigDecimal): BigDecimal =
    ???  // Implémente la conversion

  def formatAmount(amount: BigDecimal, currency: String): String =
    ???  // Retourne par ex : "1 500.00 EUR"

  @main def demo(): Unit =
    val montantMAD = BigDecimal("15000.00")
    println(formatAmount(convert(montantMAD, MAD_TO_EUR), "EUR"))
    println(formatAmount(convert(montantMAD, MAD_TO_USD), "USD"))
```

**Livrable :** Code fonctionnel avec sortie formatée.

---

## Exercice 3 : Calculateur d'Intérêts Composés (1h15)

Crée `src/main/scala/clearing/InterestCalculator.scala` :

```scala
package clearing

object InterestCalculator:
  /** Calcule les intérêts composés sur n périodes.
    * Formula : capital * (1 + tauxAnnuel) ^ nombreAnnees
    */
  def interetsComposes(
    capital: BigDecimal,
    tauxAnnuel: BigDecimal,
    nombreAnnees: Int
  ): BigDecimal =
    ???

  @main def simuler(): Unit =
    val capital = BigDecimal("100000.00")
    val taux = BigDecimal("0.035")  // 3.5%
    for annee <- 1 to 10 do
      val resultat = interetsComposes(capital, taux, annee)
      println(s"Année $annee : ${resultat.setScale(2, BigDecimal.RoundingMode.HALF_UP)} DH")
```

**Indice :** Utilise une boucle `(1 to n).foldLeft(capital)((acc, _) => ...)` ou une version plus simple.

---

## Exercice 4 : Tests de Précision (1h)

Crée `src/test/scala/clearing/CurrencyConverterSpec.scala` :

```scala
package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CurrencyConverterSpec extends AnyFlatSpec with Matchers:
  "convert" should "convertir 10000 MAD en EUR correctement" in {
    CurrencyConverter.convert(BigDecimal("10000"), CurrencyConverter.MAD_TO_EUR) shouldBe BigDecimal("920.00")
  }

  it should "retourner 0 pour un montant de 0" in {
    ???
  }

  "formatAmount" should "formater avec le symbole de devise" in {
    ???
  }
```

**Livrable :** Tous les tests passent.
