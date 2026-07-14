package clearing.v10

import clearing.model.*
import scala.util.Random
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TypedInputsSpec extends AnyFlatSpec with Matchers:
  private def transaction(status: TransactionStatus): Transaction =
    Transaction(
      id = 7,
      sender = "ATH",
      receiver = "CIH",
      amount = BigDecimal("100"),
      transactionType = TransactionType.Transfer,
      status = status
    )

  "CsvParserV10" should "parser le format historique avec des valeurs métier par défaut" in:
    CsvParserV10.parseLine("ATH,CIH,125.50") shouldBe Some(
      Transaction(
        0,
        "ATH",
        "CIH",
        BigDecimal("125.50"),
        TransactionType.Transfer,
        TransactionStatus.Pending
      )
    )

  it should "parser les trois types du format v1" in:
    val lines = List(
      "1,ATH,CIH,10,VIR",
      "2,CIH,BOA,20,PRE",
      "3,BOA,ATH,30,CHQ"
    )
    CsvParserV10.parseLines(lines).map(_.transactionType) shouldBe List(
      TransactionType.Transfer,
      TransactionType.Withdrawal,
      TransactionType.Check
    )

  it should "attribuer des identifiants distincts à plusieurs lignes historiques" in:
    val parsed = CsvParserV10.parseLines(
      List(
        "5,BOA,ATH,30,CHQ",
        "ATH,CIH,10",
        "CIH,BOA,20"
      )
    )

    parsed.map(_.id) shouldBe List(5, 6, 7)
    TransactionValidator.partition(parsed).invalid shouldBe empty

  it should "rejeter une ligne vide, un nombre invalide et un type inconnu" in:
    CsvParserV10.parseLine("") shouldBe None
    CsvParserV10.parseLine("1,ATH,CIH,dix,VIR") shouldBe None
    CsvParserV10.parseLine("1,ATH,CIH,10,CB") shouldBe None

  "TransactionGeneratorV10" should "produire des transactions nommées et cohérentes" in:
    val generated = TransactionGeneratorV10.generateBatch(30, new Random(0L))

    generated should have size 30
    generated.map(_.id) shouldBe (1 to 30).toList
    generated.forall(tx => tx.sender != tx.receiver) shouldBe true
    generated.forall(_.status == TransactionStatus.Pending) shouldBe true
    generated.map(_.transactionType).toSet shouldBe TransactionType.values.toSet

  "TransactionRouterV10" should "router exhaustivement chaque statut" in:
    TransactionRouterV10.route(transaction(TransactionStatus.Pending)) shouldBe
      "EN_ATTENTE"
    TransactionRouterV10.route(transaction(TransactionStatus.Validated)) shouldBe
      "COMPENSATION"
    TransactionRouterV10.route(transaction(TransactionStatus.Rejected)) shouldBe
      "REJET"
    TransactionRouterV10.route(transaction(TransactionStatus.Suspicious)) shouldBe
      "CONTROLE_MANUEL"
