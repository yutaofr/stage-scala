package clearing

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV04Spec extends AnyFlatSpec with Matchers:
  private val expectedPositions = Map(
    "ATH" -> BigDecimal("-70"),
    "BMCE" -> BigDecimal("-20"),
    "BOA" -> BigDecimal("76"),
    "CIH" -> BigDecimal("20"),
    "SGMB" -> BigDecimal("-6")
  )

  private def resourcePath: String =
    val resource = Option(getClass.getResource("/transactions-s4.csv"))
      .getOrElse(fail("Ressource transactions-s4.csv absente"))
    Paths.get(resource.toURI).toString

  "processLines" should "conserver dix transactions et compter six rejets" in:
    val lines = scala.io.Source.fromFile(resourcePath)
    val values = try lines.getLines().toList
    finally lines.close()

    MainV04.processLines(values, ValidationRules.defaultRules) shouldBe
      (expectedPositions, 10, 6)

  it should "utiliser les règles fournies par l'appelant" in:
    val lines = scala.io.Source.fromFile(resourcePath)
    val values = try lines.getLines().toList
    finally lines.close()
    val (positions, accepted, ignored) = MainV04.processLines(
      values,
      List(ValidationRules.positif)
    )

    accepted shouldBe 11
    ignored shouldBe 5
    positions("ATH") shouldBe BigDecimal("-100070")
    positions("CIH") shouldBe BigDecimal("100020")
    NettingCalculator.globalNet(positions) shouldBe BigDecimal(0)

  "run" should "lire le vrai CSV et afficher un rapport mathématiquement correct" in:
    val output = ByteArrayOutputStream()
    val result = Console.withOut(output):
      MainV04.run(resourcePath)

    result shouldBe (expectedPositions, 10, 6)
    val report = output.toString("UTF-8")
    report should include("Lignes reçues : 16")
    report should include("Acceptées : 10 | Ignorées : 6")
    report should include("Solde net global : 0 DH")

  "renderReport" should "trier les banques par ordre alphabétique" in:
    val report = MainV04.renderReport((expectedPositions, 10, 6))
    report.indexOf("ATH") should be < report.indexOf("BMCE")
    report.indexOf("BMCE") should be < report.indexOf("BOA")
    report.indexOf("BOA") should be < report.indexOf("CIH")

  it should "rendre un rapport cohérent lorsque toutes les lignes sont rejetées" in:
    MainV04.renderReport((Map.empty, 0, 2)) should include(
      "Solde net global : 0 DH"
    )
