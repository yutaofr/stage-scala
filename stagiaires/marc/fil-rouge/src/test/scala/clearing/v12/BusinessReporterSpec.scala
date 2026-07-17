package clearing.v12

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class BusinessReporterSpec extends AnyFlatSpec with Matchers:
  private def transaction(
    id: Int,
    sender: String,
    receiver: String,
    amount: String,
    status: TransactionStatus = TransactionStatus.Validated
  ): Transaction =
    Transaction(
      id,
      sender,
      receiver,
      BigDecimal(amount),
      TransactionType.Transfer,
      status
    )

  "BusinessReporter.totalVolume" should "additionner seulement les transactions validées" in:
    BusinessReporter.totalVolume(
      List(
        transaction(1, "ATH", "CIH", "100"),
        transaction(2, "CIH", "BOA", "50"),
        transaction(
          3,
          "BOA",
          "ATH",
          "999",
          TransactionStatus.Rejected
        )
      )
    ) shouldBe BigDecimal("150")

  "BusinessReporter.mostActiveBank" should "compter les deux extrémités et départager par code" in:
    BusinessReporter.mostActiveBank(
      List(
        transaction(1, "ATH", "CIH", "100"),
        transaction(2, "ATH", "BOA", "50")
      )
    ) shouldBe Some(
      BankActivity(BankDirectory.fromCode("ATH"), participationCount = 2)
    )

  it should "retourner None pour un flux vide" in:
    BusinessReporter.mostActiveBank(Nil) shouldBe None

  "BusinessReporter.partitionLogs" should "séparer résultats financiers et traces techniques avec partition" in:
    val partitioned = BusinessReporter.partitionLogs(
      List(
        TechnicalLog("lecture terminée"),
        FinancialLog("volume calculé"),
        TechnicalLog("batch équilibré"),
        FinancialLog("règlement produit")
      )
    )

    partitioned shouldBe LogPartition(
      financial = List(
        FinancialLog("volume calculé"),
        FinancialLog("règlement produit")
      ),
      technical = List(
        TechnicalLog("lecture terminée"),
        TechnicalLog("batch équilibré")
      )
    )

  "BusinessReporter.renderSettlement" should "afficher débiteurs puis créditeurs" in:
    val rendered = BusinessReporter.renderSettlement(
      Map(
        "ATH" -> BigDecimal("-100"),
        "CIH" -> BigDecimal("60"),
        "BOA" -> BigDecimal("40")
      )
    )

    rendered should include("BANQUES DÉBITRICES :")
    rendered should include("- ATH : -100 DH")
    rendered should include("BANQUES CRÉDITRICES :")
    rendered should include("- CIH : +60 DH")
    rendered.indexOf("BANQUES DÉBITRICES") should be <
      rendered.indexOf("BANQUES CRÉDITRICES")

  "BusinessReporter.renderBilateral" should "trier les dettes bilatérales par importance" in:
    val rendered = BusinessReporter.renderBilateral(
      List(
        BilateralSettlement(
          BankDirectory.fromCode("CIH"),
          BankDirectory.fromCode("BOA"),
          BigDecimal("20")
        ),
        BilateralSettlement(
          BankDirectory.fromCode("ATH"),
          BankDirectory.fromCode("CIH"),
          BigDecimal("100")
        )
      )
    )

    rendered.indexOf("ATH -> CIH : 100 DH") should be <
      rendered.indexOf("CIH -> BOA : 20 DH")
