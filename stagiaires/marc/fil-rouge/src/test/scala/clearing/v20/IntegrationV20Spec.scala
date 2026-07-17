package clearing.v20

import clearing.model.Currency
import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV20Spec extends AnyFlatSpec with Matchers:
  private val resourcePath =
    Paths.get(getClass.getResource("/transactions-s9.csv").toURI).toString

  "V20Cli.parse" should "sélectionner le fichier et le profil MAD par défaut" in:
    V20Cli.parse(Nil) shouldBe Some(
      V20Command("transactions-v20.csv", V20Profile.MAD)
    )

  it should "accepter un profil EUR explicite" in:
    V20Cli.parse(List("--profile", "EUR")) shouldBe Some(
      V20Command("transactions-v20.csv", V20Profile.EUR)
    )

  it should "accepter un chemin explicite avec ou sans profil" in:
    V20Cli.parse(List("batch.csv")) shouldBe Some(
      V20Command("batch.csv", V20Profile.MAD)
    )
    V20Cli.parse(List("--profile", "MAD", "batch.csv")) shouldBe Some(
      V20Command("batch.csv", V20Profile.MAD)
    )

  it should "refuser un profil inconnu et les arguments en trop" in:
    V20Cli.parse(List("--profile", "USD")) shouldBe None
    V20Cli.parse(List("a.csv", "b.csv")) shouldBe None
    V20Cli.parse(List("--profile")) shouldBe None

  "IOBridge.read" should "retourner un contenu nommé sans afficher" in:
    val output = new ByteArrayOutputStream()
    val result = Console.withOut(new PrintStream(output)):
      IOBridge.read(resourcePath)

    result shouldBe FileReadResult.Success(
      resourcePath,
      Files.readString(Paths.get(resourcePath), StandardCharsets.UTF_8)
    )
    output.toString(StandardCharsets.UTF_8) shouldBe empty

  it should "retourner un échec nommé sans lancer d'exception" in:
    val missing = "/fichier/s9/inexistant.csv"

    IOBridge.read(missing) shouldBe FileReadResult.Failure(
      missing,
      "fichier introuvable ou illisible"
    )

  "ClearingAppV20.process" should "traiter exactement le lot MAD de démonstration" in:
    val report = completed(V20Command(resourcePath, V20Profile.MAD))
    val rendered = PureClearingRenderer.render(report)

    report.referenceCurrency shouldBe Currency.MAD
    report.transactions should have size 3
    report.rejections should have size 4
    report.positions shouldBe Map(
      "ATH" -> BigDecimal("57.50"),
      "BOA" -> BigDecimal("-49.50"),
      "CIH" -> BigDecimal("-8.00")
    )
    report.feesByBank shouldBe Map(
      "ATH" -> BigDecimal("0.10"),
      "BOA" -> BigDecimal("0.07"),
      "CIH" -> BigDecimal("0.22")
    )
    rendered should include("INPUT|7")
    rendered should include("ACCEPTED|3")
    rendered should include("REJECTED|4")
    rendered should include("GLOBAL|0.00")
    rendered should not include "MA64"

  it should "traiter le même lot en EUR avec un solde global nul" in:
    val report = completed(V20Command(resourcePath, V20Profile.EUR))
    val rendered = PureClearingRenderer.render(report)

    report.referenceCurrency shouldBe Currency.EUR
    report.transactions should have size 3
    report.rejections should have size 4
    report.positions shouldBe Map(
      "ATH" -> BigDecimal("5.32"),
      "BOA" -> BigDecimal("-4.58"),
      "CIH" -> BigDecimal("-0.74")
    )
    report.feesByBank shouldBe Map(
      "ATH" -> BigDecimal("0.01"),
      "BOA" -> BigDecimal("0.01"),
      "CIH" -> BigDecimal("0.02")
    )
    rendered should include("REFERENCE|EUR")
    rendered should include("GLOBAL|0.00")
    rendered should not include "MA64"

  it should "conserver l'échec de lecture dans le résultat" in:
    ClearingAppV20.process(
      V20Command("/fichier/s9/inexistant.csv", V20Profile.MAD)
    ) shouldBe V20Execution.ReadFailed(
      "/fichier/s9/inexistant.csv",
      "fichier introuvable ou illisible"
    )

  "ClearingReporter" should "être le seul bord qui imprime le rapport" in:
    val report = completed(V20Command(resourcePath, V20Profile.MAD))
    val output = new ByteArrayOutputStream()

    Console.withOut(new PrintStream(output, true, StandardCharsets.UTF_8)):
      ClearingReporter.print(report)

    output.toString(StandardCharsets.UTF_8) shouldBe
      PureClearingRenderer.render(report) + System.lineSeparator()

  private def completed(command: V20Command): PureClearingReport =
    ClearingAppV20.process(command) match
      case V20Execution.Completed(report) => report
      case failure                        => fail(s"Exécution inattendue : $failure")
