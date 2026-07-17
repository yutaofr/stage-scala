package clearing.v11

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import clearing.model.*
import clearing.v10.NettingCalculatorV10
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV11Spec extends AnyFlatSpec with Matchers:
  private def resourcePath(name: String): String =
    val resource = Option(getClass.getResource(s"/$name"))
      .getOrElse(fail(s"Ressource $name absente"))
    Paths.get(resource.toURI).toString

  private def resourceLines(name: String): List[String] =
    val source = scala.io.Source.fromFile(resourcePath(name))
    try source.getLines().toList
    finally source.close()

  "ClearingAppV11.processLines" should "produire quarante-cinq succès, trois alertes et douze rejets" in:
    val result = ClearingAppV11.processLines(
      resourceLines("transactions-s6.csv")
    )

    result.receivedCount shouldBe 57
    result.successfulTransactions should have size 45
    result.warnings should have size 3
    result.rejectedLines should have size 12
    result.fileErrors shouldBe empty
    NettingCalculatorV10.globalNet(result.netPositions) shouldBe BigDecimal(0)

  it should "marquer chaque transaction acceptée comme validée" in:
    val result = ClearingAppV11.processLines(
      resourceLines("transactions-s6.csv")
    )

    result.successfulTransactions.map(_.status).distinct shouldBe List(
      TransactionStatus.Validated
    )

  it should "conserver toutes les erreurs de la même ligne" in:
    val result = ClearingAppV11.processLines(
      resourceLines("transactions-s6.csv")
    )
    val line56 = result.rejectedLines.find(_.lineNumber == 56)
      .getOrElse(fail("Ligne 56 absente du rapport"))

    line56.errors should have size 5
    line56.errors should contain allOf (
      InvalidAmount(BigDecimal("-10")),
      InvalidIban("XX64ATH00000000000000000"),
      InvalidIban("FR64CIH00000000000000000"),
      UnknownBank("XXX"),
      UnknownBank("YYY")
    )

  "ClearingAppV11.renderReport" should "rendre les compteurs, erreurs et positions triées" in:
    val report = ClearingAppV11.renderReport(
      ClearingAppV11.processLines(resourceLines("transactions-s6.csv"))
    )

    report should include("=== CLEARING ENGINE v1.1 ===")
    report should include("[SUCCESS] 45 transactions traitées avec succès.")
    report should include("[WARNING] 3 transactions suspectes (Fraud Signal).")
    report should include("[ERROR] 12 lignes ignorées :")
    report should include("Ligne 46")
    report should include("Ligne 56")
    report should include("[REPORT] Solde net global : 0 DH")
    report.indexOf("ATH :") should be < report.indexOf("BOA :")
    report.indexOf("BOA :") should be < report.indexOf("CIH :")

  it should "rendre un rapport cohérent pour un fichier vide" in:
    val result = ClearingAppV11.processLines(Nil)
    val report = ClearingAppV11.renderReport(result)

    result.successfulTransactions shouldBe empty
    result.rejectedLines shouldBe empty
    result.fileErrors shouldBe List(EmptyFile)
    result.netPositions shouldBe empty
    report should include(
      "[BLOCKING] Erreur bloquante — fichier vide"
    )
    report should include("[REPORT] Solde net global : 0 DH")

  it should "ne jamais calculer de position pour un fichier composé d'erreurs" in:
    val result = ClearingAppV11.processLines(
      resourceLines("transactions-s6-errors.csv")
    )

    result.receivedCount shouldBe 3
    result.successfulTransactions shouldBe empty
    result.rejectedLines should have size 3
    result.netPositions shouldBe empty

  "ClearingAppV11.run" should "lire le fichier réel et afficher le rapport" in:
    val output = ByteArrayOutputStream()
    val result = Console.withOut(output):
      ClearingAppV11.run(resourcePath("transactions-s6.csv"))

    result.successfulTransactions should have size 45
    output.toString("UTF-8") should include(
      "[WARNING] 3 transactions suspectes"
    )

  it should "transformer une erreur de lecture en SystemError" in:
    val output = ByteArrayOutputStream()
    val result = Console.withOut(output):
      ClearingAppV11.run("/fichier/s6/introuvable.csv")

    result.fileErrors.headOption shouldBe Some(
      FileReadFailure(
        "/fichier/s6/introuvable.csv",
        "fichier introuvable ou illisible"
      )
    )
    output.toString("UTF-8") should include("Erreur système")
