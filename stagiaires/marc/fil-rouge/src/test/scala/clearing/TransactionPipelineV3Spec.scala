package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TransactionPipelineV3Spec extends AnyFlatSpec with Matchers:
  private val transactions: List[TransactionV2.Transaction] = List(
    (1, "ATH", "CIH", BigDecimal("100"), "vir"),
    (2, "ATH", "ATH", BigDecimal("50"), "VIR"),
    (3, "ATH", "UNKNOWN", BigDecimal("20"), "PRE"),
    (4, "CIH", "BOA", BigDecimal("-1"), "CHQ")
  )

  "enrich" should "remplacer les codes courts par les noms complets" in:
    TransactionPipelineV3.enrich(transactions.take(1)) shouldBe List(
      (1, "Attijariwafa Bank", "CIH Bank", BigDecimal("100"), "vir")
    )

  "enrichWithFor" should "produire le même résultat que map" in:
    TransactionPipelineV3.enrichWithFor(transactions) shouldBe
      TransactionPipelineV3.enrich(transactions)

  "cleanBatches" should "aplatir, filtrer et normaliser le flux" in:
    TransactionPipelineV3.cleanBatches(List(transactions.take(2), transactions.drop(2))) shouldBe
      List((1, "ATH", "CIH", BigDecimal("100"), "VIR"))

  "positiveAmounts" should "aplatir les lots et supprimer les montants impropres" in:
    val lots = List(
      ("lot-1", List(BigDecimal("10"), BigDecimal("-2"))),
      ("lot-2", List(BigDecimal("0"), BigDecimal("5")))
    )
    TransactionPipelineV3.positiveAmounts(lots) shouldBe
      List(BigDecimal("10"), BigDecimal("5"))

  "auditRequired" should "sélectionner les transactions supérieures à 5000 DH" in:
    val highValue = (5, "ATH", "CIH", BigDecimal("5000.01"), "VIR")
    TransactionPipelineV3.auditRequired(transactions :+ highValue) shouldBe
      List(highValue)

  it should "accepter une liste de lots vide" in:
    TransactionPipelineV3.positiveAmounts(Nil) shouldBe Nil
