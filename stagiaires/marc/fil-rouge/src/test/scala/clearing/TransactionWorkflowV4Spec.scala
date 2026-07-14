package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TransactionWorkflowV4Spec extends AnyFlatSpec with Matchers:
  private val valid: TransactionV2.Transaction =
    (1, "ATH", "CIH", BigDecimal("100"), "VIR")

  "validate" should "conserver une transaction conforme à toutes les règles" in:
    TransactionWorkflowV4.validate(valid, ValidationRules.defaultRules) shouldBe
      Some(valid)

  it should "refuser montant négatif, seuil audit et virement interne" in:
    TransactionWorkflowV4.validate(
      (2, "ATH", "CIH", BigDecimal("-1"), "VIR"),
      ValidationRules.defaultRules
    ) shouldBe None
    TransactionWorkflowV4.validate(
      (3, "ATH", "CIH", BigDecimal("100000"), "VIR"),
      ValidationRules.defaultRules
    ) shouldBe None
    TransactionWorkflowV4.validate(
      (4, "ATH", "ATH", BigDecimal("10"), "VIR"),
      ValidationRules.defaultRules
    ) shouldBe None

  "enrich" should "conserver une transaction dont les banques existent" in:
    TransactionWorkflowV4.enrich(valid) shouldBe Some(valid)

  it should "retourner None si une banque est inconnue" in:
    TransactionWorkflowV4.enrich(
      (5, "ATH", "UNKNOWN", BigDecimal("10"), "VIR")
    ) shouldBe None

  "parseValidateEnrich" should "composer les trois étapes avec Option" in:
    TransactionWorkflowV4.parseValidateEnrich(
      "1,ATH,CIH,100,VIR",
      ValidationRules.defaultRules
    ) shouldBe Some(valid)

  it should "propager None depuis parsing, validation ou enrichissement" in:
    TransactionWorkflowV4.parseValidateEnrich(
      "ligne,invalide",
      ValidationRules.defaultRules
    ) shouldBe None
    TransactionWorkflowV4.parseValidateEnrich(
      "2,ATH,CIH,-1,VIR",
      ValidationRules.defaultRules
    ) shouldBe None
    TransactionWorkflowV4.parseValidateEnrich(
      "3,ATH,UNKNOWN,10,VIR",
      ValidationRules.defaultRules
    ) shouldBe None

  it should "refuser colonnes vides et montant texte sans exception" in:
    TransactionWorkflowV4.parseValidateEnrich(
      "4,ATH,,10,VIR",
      ValidationRules.defaultRules
    ) shouldBe None
    TransactionWorkflowV4.parseValidateEnrich(
      "5,ATH,CIH,abc,VIR",
      ValidationRules.defaultRules
    ) shouldBe None

  "collectValid" should "aplatir les Options et compter les rejets" in:
    val lines = List(
      "1,ATH,CIH,100,VIR",
      "ligne,invalide",
      "2,ATH,CIH,-1,VIR",
      "3,ATH,UNKNOWN,10,VIR"
    )
    TransactionWorkflowV4.collectValid(
      lines,
      ValidationRules.defaultRules
    ) shouldBe (List(valid), 3)

  it should "accepter une configuration de règles différente" in:
    val permissiveRules = List(ValidationRules.positif)
    TransactionWorkflowV4.parseValidateEnrich(
      "6,ATH,CIH,100000,VIR",
      permissiveRules
    ) shouldBe Some((6, "ATH", "CIH", BigDecimal("100000"), "VIR"))
