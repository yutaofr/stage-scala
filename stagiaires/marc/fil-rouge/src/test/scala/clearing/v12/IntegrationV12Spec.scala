package clearing.v12

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV12Spec extends AnyFlatSpec with Matchers:
  private def resourcePath(name: String): String =
    val resource = Option(getClass.getResource(s"/$name"))
      .getOrElse(fail(s"Ressource $name absente"))
    Paths.get(resource.toURI).toString

  private def resourceLines(name: String): List[String] =
    val source = scala.io.Source.fromFile(resourcePath(name))
    try source.getLines().toList
    finally source.close()

  "V12Cli.parse" should "accepter seulement les formes documentées" in:
    V12Cli.parse(Nil) shouldBe Some(
      V12Command.RunFile("transactions-v11.csv")
    )
    V12Cli.parse(List("flux.csv")) shouldBe Some(
      V12Command.RunFile("flux.csv")
    )
    V12Cli.parse(List("--generated", "100000")) shouldBe Some(
      V12Command.RunGenerated(100000)
    )

  it should "refuser texte, valeur négative, argument manquant et surplus" in:
    List(
      List("--generated", "abc"),
      List("--generated", "-1"),
      List("--generated"),
      List("--generated", "100", "surplus"),
      List("a.csv", "b.csv")
    ).foreach(arguments => V12Cli.parse(arguments) shouldBe None)

  it should "afficher l'usage sans lancer de traitement pour une forme invalide" in:
    val standardOutput = ByteArrayOutputStream()
    val errorOutput = ByteArrayOutputStream()

    Console.withOut(standardOutput):
      Console.withErr(errorOutput):
        runClearingAppV12("--generated", "abc")

    standardOutput.toString("UTF-8") shouldBe empty
    errorOutput.toString("UTF-8") should include(V12Cli.usage)

  "ClearingAppV12.processLines" should "réutiliser la validation v1.1 et produire le règlement v1.2" in:
    val result = ClearingAppV12.processLines(
      resourceLines("transactions-s6.csv"),
      batchSize = 10
    )

    result.receivedCount shouldBe 57
    result.transactions should have size 45
    result.rejectedLines should have size 12
    result.validationWarnings should have size 3
    result.batches.map(_.transactionCount) shouldBe List(10, 10, 10, 10, 5)
    all(result.batches.map(batch =>
      MultilateralNetting.isBalanced(batch.positions)
    )) shouldBe true
    result.bilateralSettlements should not be empty
    result.fraudAlerts should not be empty
    MultilateralNetting.isBalanced(result.positions) shouldBe true

  "ClearingAppV12.renderReport" should "afficher les rapports bilatéral, agrégé, batch et fraude" in:
    val report = ClearingAppV12.renderReport(
      ClearingAppV12.processLines(
        resourceLines("transactions-s6.csv"),
        batchSize = 10
      )
    )

    report should include("=== CLEARING ENGINE v1.2 ===")
    report should include("[VALIDATION] 45 validées | 12 rejetées")
    report should include("[BUSINESS] Volume total échangé")
    report should include("[BILATERAL] Règlements nets")
    report should include("BANQUES DÉBITRICES :")
    report should include("BANQUES CRÉDITRICES :")
    report should include("[BATCH] 5 batches — tous équilibrés : true")
    report should include("[FRAUD WINDOW]")
    report should include("[REPORT] Solde net global : 0 DH")

  it should "conserver l'erreur de fichier vide" in:
    val result = ClearingAppV12.processLines(Nil)

    result.transactions shouldBe empty
    result.fileErrors shouldBe List(EmptyFile)
    result.positions shouldBe empty
    ClearingAppV12.renderReport(result) should include(
      "Erreur bloquante — fichier vide"
    )

  "ClearingAppV12.runGenerated" should "démontrer cent mille transactions équilibrées" in:
    val output = ByteArrayOutputStream()
    val result = Console.withOut(output):
      ClearingAppV12.runGenerated(100000)

    result.transactions should have size 100000
    result.rejectedLines shouldBe empty
    result.batches should have size 100
    result.totalVolume shouldBe BigDecimal("5009401.25")
    result.mostActiveBank shouldBe Some(
      BankActivity(
        BankDirectory.fromCode("SGMB"),
        participationCount = 40178
      )
    )
    MultilateralNetting.isBalanced(result.positions) shouldBe true
    output.toString("UTF-8") should include(
      "[INPUT] 100000 transactions générées."
    )

  "ClearingAppV12.runFile" should "transformer une lecture impossible en erreur système" in:
    val output = ByteArrayOutputStream()
    val result = Console.withOut(output):
      ClearingAppV12.runFile("/fichier/s7/introuvable.csv")

    result.fileErrors.headOption shouldBe Some(
      FileReadFailure(
        "/fichier/s7/introuvable.csv",
        "fichier introuvable ou illisible"
      )
    )
    output.toString("UTF-8") should include("Erreur système")
