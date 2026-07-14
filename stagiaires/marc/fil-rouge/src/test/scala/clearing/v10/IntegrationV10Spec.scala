package clearing.v10

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV10Spec extends AnyFlatSpec with Matchers:
  private val expectedPositions = Map(
    "ATH" -> BigDecimal("-70"),
    "BMCE" -> BigDecimal("-20"),
    "BOA" -> BigDecimal("76"),
    "CIH" -> BigDecimal("20"),
    "SGMB" -> BigDecimal("-6")
  )

  private def resourcePath: String =
    val resource = Option(getClass.getResource("/transactions-s5.csv"))
      .getOrElse(fail("Ressource transactions-s5.csv absente"))
    Paths.get(resource.toURI).toString

  private def resourceLines: List[String] =
    val source = scala.io.Source.fromFile(resourcePath)
    try source.getLines().toList
    finally source.close()

  "ClearingAppV10.processLines" should "séparer dix valides, quatre invalides et deux lignes malformées" in:
    val appResult = ClearingAppV10.processLines(resourceLines, batchId = 5)

    appResult.receivedCount shouldBe 16
    appResult.result.batch.transactions should have size 14
    appResult.invalidTransactions should have size 4
    appResult.malformedCount shouldBe 2
    appResult.result.netPositions shouldBe expectedPositions
    NettingCalculatorV10.globalNet(appResult.result.netPositions) shouldBe
      BigDecimal(0)

  it should "conserver les quatre causes métier typées" in:
    val appResult = ClearingAppV10.processLines(resourceLines, batchId = 5)

    appResult.result.errors.map(_.getClass.getSimpleName) shouldBe List(
      "InvalidAmount",
      "UnknownBank",
      "ValidationError",
      "DuplicateTransaction$"
    )

  "ClearingAppV10.renderReport" should "rendre un rapport trié et explicite" in:
    val report = ClearingAppV10.renderReport(
      ClearingAppV10.processLines(resourceLines, batchId = 5)
    )

    report should include("=== CLEARING ENGINE v1.0 ===")
    report should include("Lignes reçues : 16")
    report should include("Parsées : 14 | Malformées : 2")
    report should include("Valides : 10 | Invalides : 4")
    report should include("Transaction 13 : Montant invalide : -5 DH")
    report should include("Transaction 10 : Transaction dupliquée")
    report should include("Résultat : Échec partiel (4 erreurs)")
    report should include("Solde net global : 0 DH")
    report.indexOf("ATH") should be < report.indexOf("BMCE")

  "ClearingAppV10.run" should "lire le vrai fichier et afficher le même rapport" in:
    val output = ByteArrayOutputStream()
    val result = Console.withOut(output):
      ClearingAppV10.run(resourcePath)

    result.result.netPositions shouldBe expectedPositions
    output.toString("UTF-8") should include("Valides : 10 | Invalides : 4")
