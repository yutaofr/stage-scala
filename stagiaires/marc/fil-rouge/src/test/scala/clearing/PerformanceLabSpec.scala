package clearing

import scala.util.Random
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class PerformanceLabSpec extends AnyFlatSpec with Matchers:
  private val transactions = Vector(
    (1, "ATH", "CIH", BigDecimal("100.125"), "vir"),
    (2, "CIH", "BOA", BigDecimal("0"), "PRE"),
    (3, "BOA", "ATH", BigDecimal("12.345"), "chq")
  )

  "pipelineWithView" should "produire le même résultat que le pipeline strict" in:
    PerformanceLab.pipelineWithView(transactions) shouldBe
      PerformanceLab.pipelineEager(transactions)

  "nettingOnListAndVector" should "conserver le même résultat pour les deux collections" in:
    val generated = TransactionV2.generateBatch(1_000, new Random(0L))
    val (fromList, fromVector) = PerformanceLab.nettingOnListAndVector(generated)
    fromList shouldBe fromVector
    NettingCalculator.globalNet(fromList) shouldBe BigDecimal(0)

  "totalAmount" should "parcourir List et Vector avec le même résultat" in:
    PerformanceLab.totalAmount(transactions.toList) shouldBe
      PerformanceLab.totalAmount(transactions)

  "safeSum" should "additionner un million d'entiers sans StackOverflow" in:
    PerformanceLab.safeSum(1_000_000) shouldBe 500000500000L

  "unsafeRecursiveSum" should "illustrer la récursion non terminale sur un petit jeu" in:
    PerformanceLab.unsafeRecursiveSum(List(1, 2, 3, 4)) shouldBe 10L

  "measureMillis" should "retourner le résultat et une durée non négative" in:
    val (result, duration) = PerformanceLab.measureMillis(21 * 2)
    result shouldBe 42
    duration should be >= 0L

  "repeatedMiddleAccess" should "lire la même valeur sur List et Vector" in:
    val asList = transactions.toList
    PerformanceLab.repeatedMiddleAccess(asList, 3) shouldBe
      PerformanceLab.repeatedMiddleAccess(transactions, 3)

  "stressNetting" should "traiter cent mille transactions et préserver l'invariant" in:
    val positions = PerformanceLab.stressNetting(100_000, 0L)
    NettingCalculator.globalNet(positions) shouldBe BigDecimal(0)
