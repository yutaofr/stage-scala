package clearing

import java.io.ByteArrayOutputStream
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CsvParserSpec extends AnyFlatSpec with Matchers:
  "parseLine" should "parser le format final à cinq colonnes" in:
    CsvParser.parseLine("1,ATH,CIH,150.50,VIR") shouldBe Some(
      (1, "ATH", "CIH", BigDecimal("150.50"), "VIR")
    )

  it should "nettoyer les espaces du format pédagogique à trois colonnes" in:
    CsvParser.parseLine(" ATH , CIH , 150.50 ") shouldBe Some(
      (0, "ATH", "CIH", BigDecimal("150.50"), "VIR")
    )

  it should "refuser une ligne vide" in:
    CsvParser.parseLine("   ") shouldBe None

  it should "refuser un nombre de colonnes incorrect" in:
    CsvParser.parseLine("ATH,500") shouldBe None

  it should "refuser une virgule décimale qui crée une colonne supplémentaire" in:
    CsvParser.parseLine("ATH,CIH,150,50") shouldBe None

  it should "refuser un ID ou un montant non numérique" in:
    CsvParser.parseLine("id,ATH,CIH,150.50,VIR") shouldBe None
    CsvParser.parseLine("1,ATH,CIH,abc,VIR") shouldBe None

  it should "valider le format des codes avec une regex" in:
    CsvParser.parseLine("1,A!H,CIH,10,VIR") shouldBe None
    CsvParser.parseLine("1,ATH,CIH,10,V!R") shouldBe None

  "parseLines" should "conserver les lignes valides et expliquer les rejets" in:
    val errors = ByteArrayOutputStream()
    val parsed = Console.withErr(errors):
      CsvParser.parseLines(
        List(
          "1,ATH,CIH,100,VIR",
          "ATH,500",
          "",
          "2,CIH,BOA,40,PRE"
        )
      )

    parsed should have size 2
    val text = errors.toString("UTF-8")
    text should include("colonnes attendues: 3 ou 5")
    text should include("ligne vide")

  "Transaction.apply" should "déléguer la construction au parser" in:
    Transaction("3,BOA,ATH,20,CHQ") shouldBe Some(
      (3, "BOA", "ATH", BigDecimal("20"), "CHQ")
    )
