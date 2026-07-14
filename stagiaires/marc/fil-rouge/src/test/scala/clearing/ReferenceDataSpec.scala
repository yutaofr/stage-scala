package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ReferenceDataSpec extends AnyFlatSpec with Matchers:
  private val transactions: List[TransactionV2.Transaction] = List(
    (1, "ATH", "CIH", BigDecimal("100"), "VIR"),
    (2, "ATH", "BOA", BigDecimal("50"), "PRE"),
    (3, "CIH", "ATH", BigDecimal("40"), "VIR")
  )

  "repertoire" should "contenir au moins cinq banques marocaines" in:
    ReferenceData.repertoire.size should be >= 5
    ReferenceData.getBankName("001") shouldBe "Attijariwafa Bank"

  "getBankName" should "retourner une valeur explicite pour un code inconnu" in:
    ReferenceData.getBankName("999") shouldBe "Banque Inconnue"

  "uniqueIbans" should "éliminer les doublons avec un Set" in:
    ReferenceData.uniqueIbans(
      List("MA001", "MA002", "MA001")
    ) shouldBe Set("MA001", "MA002")

  "indexBySender" should "grouper les transactions par banque source" in:
    val index = ReferenceData.indexBySender(transactions)
    index("ATH").map(_._1) shouldBe List(1, 2)
    index("CIH").map(_._1) shouldBe List(3)

  "sentCounts" should "compter les transactions envoyées par banque" in:
    ReferenceData.sentCounts(transactions) shouldBe Map("ATH" -> 2, "CIH" -> 1)
