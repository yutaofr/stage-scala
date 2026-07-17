package clearing

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV03Spec extends AnyFlatSpec with Matchers:
  "MainV03" should "enchaîner parsing, filtrage et netting" in:
    val lines = List(
      "1,ATH,CIH,100,VIR",
      "2,ATH,ATH,30,VIR",
      "3,ATH,UNKNOWN,20,VIR",
      "4,CIH,BOA,-5,PRE",
      "ligne invalide"
    )
    MainV03.processLines(lines) shouldBe Map(
      "ATH" -> BigDecimal("-100"),
      "CIH" -> BigDecimal("100")
    )

  it should "traiter le scénario CSV de dix transactions" in:
    val resource = Option(getClass.getResource("/transactions-s3.csv"))
      .getOrElse(fail("Ressource transactions-s3.csv absente"))
    val file = Paths.get(resource.toURI).toString
    val output = ByteArrayOutputStream()
    val positions = Console.withOut(output):
      MainV03.run(file)

    positions shouldBe Map(
      "ATH" -> BigDecimal("-70"),
      "BMCE" -> BigDecimal("-20"),
      "BOA" -> BigDecimal("76"),
      "CIH" -> BigDecimal("20"),
      "SGMB" -> BigDecimal("-6")
    )
    NettingCalculator.globalNet(positions) shouldBe BigDecimal(0)
    output.toString("UTF-8") should include("Transactions valides : 10")

  "renderReport" should "trier les banques par ordre alphabétique" in:
    val report = MainV03.renderReport(
      Map("CIH" -> BigDecimal("20"), "ATH" -> BigDecimal("-20")),
      2
    )
    report.indexOf("ATH") should be < report.indexOf("CIH")
    report should include("Solde net global : 0 DH")
