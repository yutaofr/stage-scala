package clearing.v22

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV22Spec extends AnyFlatSpec with Matchers:
  private val athIban = "MA64ATH00000000000000000"
  private val cihIban = "MA64CIH00000000000000000"
  private val boaIban = "MA64BOA00000000000000000"

  private def fixturePath: String =
    Paths.get(getClass.getResource("/transactions-s11.csv").toURI).toString

  "V22Cli.parse" should "choisir JSON et le fichier par défaut" in:
    V22Cli.parse(Nil) shouldBe Right(
      V22Command("transactions-v22.csv", OutputFormat.Json, false)
    )

  it should "accepter format, panne et chemin dans un ordre explicite" in:
    V22Cli.parse(List("--format", "CSV", "batch.csv")) shouldBe Right(
      V22Command("batch.csv", OutputFormat.Csv, false)
    )
    V22Cli.parse(
      List("batch.csv", "--simulate-hash-failure", "--format", "XML")
    ) shouldBe Right(
      V22Command("batch.csv", OutputFormat.Xml, true)
    )

  it should "refuser format, option, doublon et chemins ambigus" in:
    V22Cli.parse(List("--format", "YAML")).isLeft shouldBe true
    V22Cli.parse(List("--unknown")).isLeft shouldBe true
    V22Cli.parse(List("a.csv", "b.csv")).isLeft shouldBe true
    V22Cli.parse(List("--format", "JSON", "--format", "CSV")).isLeft shouldBe
      true

  "ClearingAppV22.run" should "exporter les mêmes compteurs dans trois formats" in:
    val json = ClearingAppV22.run(
      V22Command(fixturePath, OutputFormat.Json, false)
    )
    val csv = ClearingAppV22.run(
      V22Command(fixturePath, OutputFormat.Csv, false)
    )
    val xml = ClearingAppV22.run(
      V22Command(fixturePath, OutputFormat.Xml, false)
    )

    json should include("\"successes\":3")
    json should include("\"rejections\":4")
    json should include("\"global\":\"0.00\"")
    csv should startWith("RESULT,MAD,3,4")
    csv should include("GLOBAL,0.00")
    xml should startWith(
      "<clearingResult referenceCurrency=\"MAD\" successes=\"3\" rejections=\"4\">"
    )
    xml should include("<global>0.00</global>")
    List(json, csv, xml).mkString should not include athIban
    List(json, csv, xml).mkString should not include cihIban
    List(json, csv, xml).mkString should not include boaIban

  it should "isoler une panne de hash sans interrompre le batch" in:
    val rendered = ClearingAppV22.run(
      V22Command(fixturePath, OutputFormat.Json, true)
    )

    rendered should include("\"successes\":2")
    rendered should include("\"rejections\":5")
    rendered should include("TECH_HASH_IBAN")
    rendered should include("\"technical\":1")
    rendered should include("\"global\":\"0.00\"")

  it should "transformer un fichier absent en erreur technique stable" in:
    ClearingAppV22.run(
      V22Command(
        "target/fichier-absent-s11.csv",
        OutputFormat.Json,
        false
      )
    ) shouldBe
      "REJET : TECH_READ_FILE - FileNotFoundException : fichier introuvable ou illisible"

  it should "retourner une valeur sans écrire sur la console" in:
    val output = new ByteArrayOutputStream()
    val rendered = Console.withOut(
      new PrintStream(output, true, StandardCharsets.UTF_8)
    ):
      ClearingAppV22.run(
        V22Command(fixturePath, OutputFormat.Json, false)
      )

    rendered should include("\"successes\":3")
    output.toString(StandardCharsets.UTF_8) shouldBe empty

  "ClearingAppV22.runSafely" should "capturer seulement NonFatal" in:
    val rendered = ClearingAppV22.runSafely:
      throw new IllegalStateException("boom")

    rendered shouldBe
      "REJET : TECH_RUN_APPLICATION - IllegalStateException : erreur inattendue"

    an[OutOfMemoryError] should be thrownBy ClearingAppV22.runSafely:
      throw new OutOfMemoryError("fatal")

  "V22IO.read" should "lire un fichier réel dans un Either" in:
    val path = Files.createTempFile("v22-io", ".txt")
    Files.writeString(path, "contenu", StandardCharsets.UTF_8)

    try V22IO.read(path.toString) shouldBe Right("contenu")
    finally Files.deleteIfExists(path)

  "V22Reporter.print" should "constituer l'unique frontière console" in:
    val output = new ByteArrayOutputStream()

    Console.withOut(new PrintStream(output, true, StandardCharsets.UTF_8)):
      V22Reporter.print("rapport v22")

    output.toString(StandardCharsets.UTF_8).trim shouldBe "rapport v22"
