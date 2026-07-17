package clearing

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV02Spec extends AnyFlatSpec with Matchers:
  "Le moteur v0.2" should "lire un vrai CSV et produire les positions attendues" in:
    val resource = Option(getClass.getResource("/transactions-s2.csv"))
      .getOrElse(fail("Ressource transactions-s2.csv absente"))
    val file = Paths.get(resource.toURI).toString
    val output = ByteArrayOutputStream()
    val errors = ByteArrayOutputStream()

    val positions = Console.withOut(output):
      Console.withErr(errors):
        SimpleClearingProcessor.run(file)

    positions shouldBe Map(
      "ATH" -> BigDecimal("-140"),
      "CIH" -> BigDecimal("60"),
      "BOA" -> BigDecimal("80")
    )
    output.toString("UTF-8") should include("[INFO] Lecture terminée")
    output.toString("UTF-8") should include("Solde net global : 0 DH")
    errors.toString("UTF-8") should include("Ligne malformée")

  "processLines" should "utiliser le contrat fourni par le moteur" in:
    val lines = List(
      "1,ATH,CIH,100,VIR",
      "2,CIH,ATH,40,PRE"
    )
    val output = ByteArrayOutputStream()
    val positions = Console.withOut(output):
      ClearingEngine.processLines(lines, SimpleClearingProcessor)
    positions shouldBe Map(
      "ATH" -> BigDecimal("-60"),
      "CIH" -> BigDecimal("60")
    )

  "inputFile" should "choisir le CSV par défaut sans argument CLI" in:
    ClearingEngine.inputFile(Seq.empty) shouldBe "transactions.csv"

  it should "utiliser le chemin explicitement fourni" in:
    ClearingEngine.inputFile(Seq("autre.csv")) shouldBe "autre.csv"
