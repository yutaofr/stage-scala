package clearing.v23

import clearing.v22.{HashBoundary, HashFailure, OutputFormat, V22Command, V22Profiles}
import clearing.v22.DomainTypes.*
import clearing.v22.DomainTypes.given
import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV23Spec extends AnyFlatSpec with Matchers:
  private val athIban = "MA64ATH00000000000000000"
  private val cihIban = "MA64CIH00000000000000000"
  private val boaIban = "MA64BOA00000000000000000"
  private val safeHash = IbanHash.from("a" * 64).toOption.get

  private def fixturePath: String =
    Paths.get(getClass.getResource("/transactions-s11.csv").toURI).toString

  private def fixture: String = Files.readString(Paths.get(fixturePath))

  private val workingHash: HashBoundary = _ => Right(safeHash)

  private val simulatedHashFailure: HashBoundary = iban =>
    if iban.bankSegment.startsWith("BOA") then Left(HashFailure.Simulated)
    else Right(safeHash)

  "V23Pipeline.runPure" should "assembler le cas nominal avec un journal chronologique" in:
    val execution = V23Pipeline.runPure(
      V22Profiles.clearingMAD,
      workingHash,
      OutputFormat.Json
    )(fixture)

    execution.value.result.transactions should have size 3
    execution.value.result.rejections should have size 4
    execution.value.result.positions.values.sum shouldBe Money.zero
    execution.value.report should include("\"successes\":3")
    execution.value.report should include("\"global\":\"0.00\"")
    execution.logs shouldBe List(
      "Entrée observée : 7 lignes",
      "Clearing terminé : 3 succès, 4 rejets",
      "Netting audité : somme globale nulle",
      "Export JSON terminé"
    )

  it should "exporter les mêmes invariants en JSON, CSV et XML" in:
    val reports = List(
      OutputFormat.Json,
      OutputFormat.Csv,
      OutputFormat.Xml
    ).map: format =>
      format -> V23Pipeline
        .runPure(V22Profiles.clearingMAD, workingHash, format)(fixture)
        .value
        .report
    .toMap

    reports(OutputFormat.Json) should include("\"successes\":3")
    reports(OutputFormat.Json) should include("\"rejections\":4")
    reports(OutputFormat.Json) should include("\"global\":\"0.00\"")
    reports(OutputFormat.Csv) should startWith("RESULT,MAD,3,4")
    reports(OutputFormat.Csv) should include("GLOBAL,0.00")
    reports(OutputFormat.Xml) should startWith(
      "<clearingResult referenceCurrency=\"MAD\" successes=\"3\" rejections=\"4\">"
    )
    reports(OutputFormat.Xml) should include("<global>0.00</global>")

  it should "isoler une panne de hash sans interrompre le batch" in:
    val execution = V23Pipeline.runPure(
      V22Profiles.clearingMAD,
      simulatedHashFailure,
      OutputFormat.Json
    )(fixture)

    execution.value.result.transactions should have size 2
    execution.value.result.rejections should have size 5
    execution.value.result.statistics.technical shouldBe 1
    execution.value.result.positions.values.sum shouldBe Money.zero
    execution.value.report should include("TECH_HASH_IBAN")

  it should "ne révéler ni IBAN, ni hash, ni ligne CSV brute dans le journal" in:
    val execution = V23Pipeline.runPure(
      V22Profiles.clearingMAD,
      workingHash,
      OutputFormat.Json
    )(fixture)
    val logs = execution.logs.mkString("\n")

    logs should not include athIban
    logs should not include cihIban
    logs should not include boaIban
    logs should not include safeHash.value
    logs should not include fixture.linesIterator.next()
    execution.value.report should not include athIban
    execution.value.report should not include cihIban
    execution.value.report should not include boaIban

  it should "retourner une valeur sans écrire sur la console" in:
    val output = new ByteArrayOutputStream()
    val execution = Console.withOut(
      new PrintStream(output, true, StandardCharsets.UTF_8)
    ):
      V23Pipeline.runPure(
        V22Profiles.clearingMAD,
        workingHash,
        OutputFormat.Json
      )(fixture)

    execution.value.report should include("\"successes\":3")
    output.toString(StandardCharsets.UTF_8) shouldBe empty

  "ClearingAppV23.run" should "ajouter le journal pur après le rapport" in:
    val rendered = ClearingAppV23.run(
      V22Command(fixturePath, OutputFormat.Json, false)
    )

    rendered should include("\"successes\":3")
    rendered should include("\nJOURNAL_PUR\n")
    rendered should endWith("Export JSON terminé")

  it should "transformer un fichier absent en erreur technique stable" in:
    ClearingAppV23.run(
      V22Command(
        "target/fichier-absent-s12.csv",
        OutputFormat.Json,
        false
      )
    ) shouldBe
      "REJET : TECH_READ_FILE - FileNotFoundException : fichier introuvable ou illisible"

  "ClearingAppV23.runSafely" should "capturer seulement NonFatal" in:
    val rendered = ClearingAppV23.runSafely:
      throw new IllegalStateException("boom")

    rendered shouldBe
      "REJET : TECH_RUN_APPLICATION - IllegalStateException : erreur inattendue"

    an[OutOfMemoryError] should be thrownBy ClearingAppV23.runSafely:
      throw new OutOfMemoryError("fatal")

  "V23Reporter.print" should "constituer l'unique frontière console" in:
    val output = new ByteArrayOutputStream()

    Console.withOut(new PrintStream(output, true, StandardCharsets.UTF_8)):
      V23Reporter.print("rapport v23")

    output.toString(StandardCharsets.UTF_8).trim shouldBe "rapport v23"
