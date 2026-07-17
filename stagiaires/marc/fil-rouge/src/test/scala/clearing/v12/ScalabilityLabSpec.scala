package clearing.v12

import java.io.ByteArrayOutputStream
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ScalabilityLabSpec extends AnyFlatSpec with Matchers:
  "ScalabilityCli.parseSize" should "accepter le défaut et une taille positive ou nulle" in:
    ScalabilityCli.parseSize(Nil) shouldBe Some(1000000)
    ScalabilityCli.parseSize(List("0")) shouldBe Some(0)
    ScalabilityCli.parseSize(List("250000")) shouldBe Some(250000)

  it should "refuser texte, valeur négative et arguments en surplus" in:
    List(
      List("abc"),
      List("-1"),
      List("100", "surplus")
    ).foreach(arguments => ScalabilityCli.parseSize(arguments) shouldBe None)

  it should "afficher l'usage sans lancer le benchmark pour un argument invalide" in:
    val standardOutput = ByteArrayOutputStream()
    val errorOutput = ByteArrayOutputStream()

    Console.withOut(standardOutput):
      Console.withErr(errorOutput):
        runScalabilityLab("abc")

    standardOutput.toString("UTF-8") shouldBe empty
    errorOutput.toString("UTF-8") should include(ScalabilityCli.usage)

  "ScalabilityLab.measureMillis" should "retourner le résultat et une durée non négative" in:
    val timed = ScalabilityLab.measureMillis(21 * 2)

    timed.value shouldBe 42
    timed.durationMillis should be >= 0L

  "ScalabilityLab" should "produire le même netting sur List et Vector" in:
    val transactions = S7TransactionGenerator.generateVector(10000)
    val comparison = ScalabilityLab.comparePreparedNetting(
      transactions.toList,
      transactions
    )

    comparison.listPositions shouldBe comparison.vectorPositions
    comparison.listDurationMillis should be >= 0L
    comparison.vectorDurationMillis should be >= 0L
    MultilateralNetting.isBalanced(comparison.vectorPositions) shouldBe true

  it should "produire le même digest avec le pipeline strict et la view" in:
    val transactions = S7TransactionGenerator.generateVector(10000)

    ScalabilityLab.strictPipelineDigest(transactions) shouldBe
      ScalabilityLab.viewPipelineDigest(transactions)

  it should "produire les mêmes scores CPU en séquentiel et en parallèle" in:
    val transactions = S7TransactionGenerator.generateVector(200)

    ScalabilityLab.sequentialScores(transactions, rounds = 20) shouldBe
      ScalabilityLab.parallelScores(transactions, rounds = 20)

  "ScalabilityLab.benchmark" should "rassembler les mesures sans imposer leur ordre" in:
    val report = ScalabilityLab.benchmark(
      size = 10000,
      cpuSampleSize = 200,
      cpuRounds = 20
    )

    report.transactionCount shouldBe 10000
    report.nettingEquivalent shouldBe true
    report.pipelineEquivalent shouldBe true
    report.cpuEquivalent shouldBe true
    report.balanced shouldBe true
    report.maxMemoryMb should be > 0L
    report.durations.values.foreach(_ should be >= 0L)

  "ScalabilityLab.render" should "rendre le volume, les durées et les invariants" in:
    val rendered = ScalabilityLab.render(
      ScalabilityLab.benchmark(
        size = 1000,
        cpuSampleSize = 100,
        cpuRounds = 10
      )
    )

    rendered should include("Transactions : 1000")
    rendered should include("Netting identique : true")
    rendered should include("Pipeline identique : true")
    rendered should include("CPU séquentiel/parallèle identique : true")
    rendered should include("Solde global nul : true")
