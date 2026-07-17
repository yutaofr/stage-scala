package clearing

import java.io.ByteArrayOutputStream
import scala.util.Random
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TransactionPipelineSpec extends AnyFlatSpec with Matchers:
  private val batch = List(
    ("ATH", "VIR", BigDecimal("120.00")),
    ("CIH", "PRE", BigDecimal("-10.00")),
    ("ATH", "CB", BigDecimal("80.00")),
    ("BOA", "CHQ", BigDecimal("0.00"))
  )

  "categorize" should "rejeter les montants nuls ou négatifs" in:
    TransactionCategorizer.categorize(BigDecimal("0")) shouldBe "Rejetée"
    TransactionCategorizer.categorize(BigDecimal("-0.01")) shouldBe "Rejetée"

  it should "respecter toutes les frontières positives" in:
    TransactionCategorizer.categorize(BigDecimal("99.99")) shouldBe "Micro"
    TransactionCategorizer.categorize(BigDecimal("100")) shouldBe "Standard"
    TransactionCategorizer.categorize(BigDecimal("9999.99")) shouldBe "Standard"
    TransactionCategorizer.categorize(BigDecimal("10000")) shouldBe "Importante"
    TransactionCategorizer.categorize(BigDecimal("99999.99")) shouldBe "Importante"
    TransactionCategorizer.categorize(BigDecimal("100000")) shouldBe "Exceptionnelle"

  "describeType" should "décrire les quatre codes connus et le fallback" in:
    TransactionCategorizer.describeType("VIR") shouldBe "Virement"
    TransactionCategorizer.describeType("PRE") shouldBe "Prélèvement"
    TransactionCategorizer.describeType("CHQ") shouldBe "Chèque"
    TransactionCategorizer.describeType("CB") shouldBe "Carte Bancaire"
    TransactionCategorizer.describeType("AUTRE") shouldBe "Inconnu"

  "generateAmount" should "produire un montant borné avec deux décimales" in:
    val amount = TransactionGenerator.generateAmount(new Random(0L))
    amount should be >= BigDecimal("-10000.00")
    amount should be < BigDecimal("40000.00")
    amount.scale shouldBe 2

  "generateBatch" should "produire le nombre demandé avec des valeurs autorisées" in:
    val generated = TransactionGenerator.generateBatch(20, new Random(0L))
    generated should have size 20
    generated.map(_._1).forall(TransactionGenerator.banks.contains) shouldBe true
    generated.map(_._2).forall(TransactionGenerator.types.contains) shouldBe true

  "filterValid" should "conserver uniquement les montants strictement positifs" in:
    TransactionFilter.filterValid(batch).map(_._3) shouldBe
      List(BigDecimal("120.00"), BigDecimal("80.00"))

  "filterByBank" should "conserver uniquement la banque demandée" in:
    TransactionFilter.filterByBank(batch, "ATH") should have size 2

  "totalsByBank" should "additionner les montants par banque" in:
    TransactionFilter.totalsByBank(batch) shouldBe Map(
      "ATH" -> BigDecimal("200.00"),
      "CIH" -> BigDecimal("-10.00"),
      "BOA" -> BigDecimal("0.00")
    )

  "sumByBank" should "afficher chaque total calculé" in:
    val output = ByteArrayOutputStream()
    Console.withOut(output):
      TransactionFilter.sumByBank(batch)
    val text = output.toString("UTF-8")
    text should include("ATH : 200.00 DH")
    text should include("CIH : -10.00 DH")
