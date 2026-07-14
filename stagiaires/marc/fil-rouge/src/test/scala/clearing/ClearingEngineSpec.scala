package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ClearingEngineSpec extends AnyFlatSpec with Matchers:
  private val batch: List[ClearingEngine.Transfer] = List(
    ("ATH", "CIH", BigDecimal("100")),
    ("CIH", "BOA", BigDecimal("40")),
    ("BOA", "ATH", BigDecimal("20")),
    ("ATH", "BOA", BigDecimal("60")),
    ("ATH", "UNKNOWN", BigDecimal("25")),
    ("CIH", "ATH", BigDecimal("-5"))
  )

  "partitionTransactions" should "séparer les transactions valides et rejetées" in:
    val (valid, rejected) = ClearingEngine.partitionTransactions(batch)
    valid should have size 4
    rejected should have size 2

  "netPositions" should "débiter l'émetteur et créditer le bénéficiaire" in:
    val (valid, _) = ClearingEngine.partitionTransactions(batch)
    ClearingEngine.netPositions(valid) shouldBe Map(
      "ATH" -> BigDecimal("-140"),
      "CIH" -> BigDecimal("60"),
      "BOA" -> BigDecimal("80")
    )

  it should "conserver une somme nette globale égale à zéro" in:
    val (valid, _) = ClearingEngine.partitionTransactions(batch)
    ClearingEngine.netPositions(valid).values.sum shouldBe BigDecimal(0)

  "bankStats" should "calculer maximum, moyenne et catégorie dominante" in:
    val (valid, _) = ClearingEngine.partitionTransactions(batch)
    ClearingEngine.bankStats("ATH", valid) shouldBe
      (BigDecimal("100"), BigDecimal("60.00"), "Micro")

  it should "retourner des statistiques neutres pour une banque absente" in:
    ClearingEngine.bankStats("SGMB", Nil) shouldBe
      (BigDecimal(0), BigDecimal("0.00"), "Aucune")

  "renderReport" should "présenter le batch, les positions et l'invariant" in:
    val report = ClearingEngine.renderReport(batch)
    report should include("Transactions reçues : 6")
    report should include("Valides : 4 | Rejetées : 2")
    report should include("ATH : -140 DH — DÉBITRICE")
    report should include("BOA : 80 DH — CRÉDITRICE")
    report should include("Solde net global : 0 DH")
