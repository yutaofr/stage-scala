package clearing

import java.io.ByteArrayOutputStream
import scala.util.Random
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TransactionV2Spec extends AnyFlatSpec with Matchers:
  private val transactions: List[TransactionV2.Transaction] = List(
    (1, "ATH", "CIH", BigDecimal("100"), "VIR"),
    (2, "ATH", "CIH", BigDecimal("50"), "PRE"),
    (3, "CIH", "ATH", BigDecimal("40"), "VIR")
  )

  "generateBatch" should "produire n IDs ordonnés et deux banques distinctes" in:
    val generated = TransactionV2.generateBatch(20, new Random(0L))
    generated should have size 20
    generated.map(_._1) shouldBe (1 to 20).toList
    generated.forall { case (_, sender, receiver, _, _) =>
      sender != receiver
    } shouldBe true

  "bilateralStats" should "compter et sommer chaque direction" in:
    ReportGenerator.bilateralStats(transactions) shouldBe Map(
      ("ATH", "CIH") -> (2, BigDecimal("150")),
      ("CIH", "ATH") -> (1, BigDecimal("40"))
    )

  "bilateralReport" should "afficher les statistiques dirigées" in:
    val output = ByteArrayOutputStream()
    Console.withOut(output):
      ReportGenerator.bilateralReport(transactions)
    output.toString("UTF-8") should include("ATH -> CIH : 2 transaction(s), 150 DH")

  "topTransactions" should "trier par montant absolu décroissant" in:
    val withNegative = transactions :+
      (4, "BOA", "ATH", BigDecimal("-200"), "CHQ")
    ReportGenerator.topTransactions(withNegative, 3).map(_._4) shouldBe
      List(BigDecimal("-200"), BigDecimal("100"), BigDecimal("50"))

  "bilateralMatrix" should "être antisymétrique" in:
    val matrix = ReportGenerator.bilateralMatrix(transactions)
    matrix(("ATH", "CIH")) shouldBe BigDecimal("110")
    matrix(("CIH", "ATH")) shouldBe BigDecimal("-110")
    matrix(("ATH", "CIH")) shouldBe -matrix(("CIH", "ATH"))

  "renderMatrix" should "afficher une matrice lisible avec une diagonale vide" in:
    val rendered = ReportGenerator.renderMatrix(
      transactions,
      List("ATH", "CIH")
    )
    rendered should include("BANQUE | ATH | CIH")
    rendered should include("ATH | - | 110")
    rendered should include("CIH | -110 | -")

  "route" should "reconnaître un virement interne" in:
    TransactionRouter.route(
      (1, "ATH", "ATH", BigDecimal("10"), "VIR")
    ) shouldBe "VIREMENT_INTERNE"

  it should "envoyer un gros virement interbancaire en audit" in:
    TransactionRouter.route(
      (2, "ATH", "CIH", BigDecimal("50000.01"), "VIR")
    ) shouldBe "AUDIT_REQUIS"

  it should "reconnaître un prélèvement" in:
    TransactionRouter.route(
      (3, "ATH", "CIH", BigDecimal("100"), "PRE")
    ) shouldBe "PRELEVEMENT"

  it should "utiliser la route standard pour les autres cas" in:
    TransactionRouter.route(
      (4, "ATH", "CIH", BigDecimal("100"), "CB")
    ) shouldBe "TRAITEMENT_STANDARD"

  "describeValue" should "matcher les types connus et le fallback" in:
    TransactionRouter.describeValue(42) shouldBe "Entier: 42"
    TransactionRouter.describeValue("ATH") shouldBe "Texte: ATH"
    TransactionRouter.describeValue(BigDecimal("10.50")) shouldBe
      "Montant: 10.50"
    TransactionRouter.describeValue(true) shouldBe "Type inconnu"

  "extractStats" should "extraire le nombre, le total et les types" in:
    TransactionRouter.extractStats(transactions) shouldBe
      (
        3,
        BigDecimal("190"),
        Map("VIR" -> 2, "PRE" -> 1)
      )

  "statsReport" should "présenter toutes les statistiques extraites" in:
    val report = TransactionRouter.statsReport(transactions)
    report should include("Transactions : 3")
    report should include("Montant total : 190 DH")
    report should include("PRE=1")
    report should include("VIR=2")
