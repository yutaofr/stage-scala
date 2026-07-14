package clearing.v21

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV21Spec extends AnyFlatSpec with Matchers:
  private val athIban = "MA64ATH00000000000000000"
  private val cihIban = "MA64CIH00000000000000000"
  private val boaIban = "MA64BOA00000000000000000"

  private def fixturePath: String =
    Paths.get(getClass.getResource("/transactions-s10.csv").toURI).toString

  "V21Cli.parse" should "accepter le fichier par défaut et un chemin explicite" in:
    V21Cli.parse(Nil) shouldBe Right(
      V21Command("transactions-v21.csv", simulateHashFailure = false)
    )
    V21Cli.parse(List("mon-batch.csv")) shouldBe Right(
      V21Command("mon-batch.csv", simulateHashFailure = false)
    )

  it should "rendre la panne de hash explicite et refuser les arguments ambigus" in:
    V21Cli.parse(List("--simulate-hash-failure")) shouldBe Right(
      V21Command("transactions-v21.csv", simulateHashFailure = true)
    )
    V21Cli.parse(
      List("--simulate-hash-failure", "mon-batch.csv")
    ) shouldBe Right(
      V21Command("mon-batch.csv", simulateHashFailure = true)
    )
    V21Cli.parse(List("a.csv", "b.csv")).isLeft shouldBe true
    V21Cli.parse(List("--inconnu")).isLeft shouldBe true

  "ClearingAppV21.run" should "terminer le batch corrompu avec quatre catégories explicites" in:
    val rendered = ClearingAppV21.run(
      V21Command(fixturePath, simulateHashFailure = true)
    )

    rendered should include("V21|REFERENCE|MAD")
    rendered should include("STATISTIQUES|parsing=1|validation=1|business=2|technical=1|warnings=1")
    rendered should include("SUCCES|2")
    rendered should include("REJETS|5")
    rendered should include("GLOBAL|0.00")
    rendered should include("REJET : PARSE_ID - identifiant illisible")
    rendered should include("REJET : AM05 - transaction 1 — opération dupliquée")
    rendered should include("REJET : TECH_HASH_IBAN - SimulatedHashFailure : panne de hash simulée")
    rendered should not include athIban
    rendered should not include cihIban
    rendered should not include boaIban

  it should "transformer un fichier absent en erreur technique rendue" in:
    val rendered = ClearingAppV21.run(
      V21Command("target/fichier-absent-s10.csv", simulateHashFailure = false)
    )

    rendered should include("REJET : TECH_READ_FILE")
    rendered should include("fichier introuvable ou illisible")

  it should "retourner une valeur sans écrire sur la console" in:
    val output = new ByteArrayOutputStream()
    val rendered = Console.withOut(
      new PrintStream(output, true, StandardCharsets.UTF_8)
    ):
      ClearingAppV21.run(
        V21Command(fixturePath, simulateHashFailure = true)
      )

    rendered should include("SUCCES|2")
    output.toString(StandardCharsets.UTF_8) shouldBe empty

  "ClearingAppV21.runSafely" should "convertir seulement les exceptions NonFatal" in:
    val rendered = ClearingAppV21.runSafely:
      throw new IllegalStateException("boom")

    rendered shouldBe
      "REJET : TECH_RUN_APPLICATION - IllegalStateException : erreur inattendue"
    an[OutOfMemoryError] should be thrownBy ClearingAppV21.runSafely:
      throw new OutOfMemoryError("fatal")

  "V21Reporter.print" should "posséder le seul println du bord v21" in:
    val output = new ByteArrayOutputStream()

    Console.withOut(new PrintStream(output, true, StandardCharsets.UTF_8)):
      V21Reporter.print("rapport final")

    output.toString(StandardCharsets.UTF_8).trim shouldBe "rapport final"
