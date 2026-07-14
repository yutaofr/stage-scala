package clearing

import java.io.ByteArrayOutputStream
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ClearingProcessorSpec extends AnyFlatSpec with Matchers:
  private val valid: List[TransactionV2.Transaction] = List(
    (1, "ATH", "CIH", BigDecimal("100"), "VIR"),
    (2, "CIH", "BOA", BigDecimal("40"), "PRE"),
    (3, "BOA", "ATH", BigDecimal("20"), "CHQ")
  )
  private val invalid: TransactionV2.Transaction =
    (4, "ATH", "UNKNOWN", BigDecimal("50"), "VIR")

  "SimpleClearingProcessor" should "respecter le contrat ClearingProcessor" in:
    val processor: ClearingProcessor = SimpleClearingProcessor
    processor shouldBe SimpleClearingProcessor

  "validate" should "accepter uniquement montant et banques valides" in:
    SimpleClearingProcessor.validate(valid.head) shouldBe true
    SimpleClearingProcessor.validate(invalid) shouldBe false
    SimpleClearingProcessor.validate(
      (5, "ATH", "CIH", BigDecimal("0"), "VIR")
    ) shouldBe false

  "calculate" should "calculer les positions des transactions valides" in:
    SimpleClearingProcessor.calculate(valid) shouldBe Map(
      "ATH" -> BigDecimal("-80"),
      "CIH" -> BigDecimal("60"),
      "BOA" -> BigDecimal("20")
    )

  "process" should "filtrer avant de calculer" in:
    SimpleClearingProcessor.process(valid :+ invalid) shouldBe
      SimpleClearingProcessor.calculate(valid)

  "report" should "afficher les positions triées et le total global" in:
    val output = ByteArrayOutputStream()
    Console.withOut(output):
      SimpleClearingProcessor.report(
        Map("CIH" -> BigDecimal("60"), "ATH" -> BigDecimal("-60"))
      )
    val text = output.toString("UTF-8")
    text.indexOf("ATH") should be < text.indexOf("CIH")
    text should include("Solde net global : 0 DH")

  "Logger" should "préfixer un message par INFO" in:
    val output = ByteArrayOutputStream()
    Console.withOut(output):
      SimpleClearingProcessor.log("test")
    output.toString("UTF-8") should include("[INFO] test")
