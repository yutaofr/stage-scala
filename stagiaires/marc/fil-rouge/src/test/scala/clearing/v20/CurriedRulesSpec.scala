package clearing.v20

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CurriedRulesSpec extends AnyFlatSpec with Matchers:
  private def transaction(
    amount: String,
    transactionType: TransactionType = TransactionType.Transfer
  ): Transaction =
    Transaction(
      id = 1,
      sender = "ATH",
      receiver = "CIH",
      amount = BigDecimal(amount),
      transactionType = transactionType,
      status = TransactionStatus.Validated
    )

  "CurriedRules.checkLimit" should "créer un validateur spécialisé à seuil strict" in:
    val belowOneHundred = CurriedRules.checkLimit(BigDecimal("100"))

    belowOneHundred(transaction("99.99")) shouldBe true
    belowOneHundred(transaction("100")) shouldBe false
    belowOneHundred(transaction("101")) shouldBe false

  "CurriedRules.applyPreciseFee" should "ajouter un taux BigDecimal et arrondir à deux décimales" in:
    val addOnePercent = CurriedRules.applyPreciseFee(BigDecimal("0.01"))

    addOnePercent(BigDecimal("100")) shouldBe BigDecimal("101.00")
    addOnePercent(BigDecimal("10.125")) shouldBe BigDecimal("10.23")

  "CurriedRules.applyFee" should "adapter le Double pédagogique avec BigDecimal.valueOf" in:
    CurriedRules.applyFee(0.02d)(BigDecimal("100")) shouldBe
      BigDecimal("102.00")

  "CurriedRules.logWithBank" should "retourner un message sans l'afficher" in:
    CurriedRules.logWithBank("ATH")("batch accepté") shouldBe
      "[ATH] batch accepté"

  "CurriedRules.feeFunctions" should "sélectionner dynamiquement une fonction préconfigurée" in:
    val feeFunctions = CurriedRules.feeFunctions(
      Map(
        "ATH" -> BigDecimal("0.01"),
        "CIH" -> BigDecimal("0.02")
      )
    )

    feeFunctions("ATH")(BigDecimal("100")) shouldBe BigDecimal("101.00")
    feeFunctions("CIH")(BigDecimal("100")) shouldBe BigDecimal("102.00")

  "PureEngineProfiles" should "spécialiser les limites Transfer et Withdrawal" in:
    val config = PureEngineProfiles.clearingMAD
    val transferLimit = CurriedRules.checkLimit(
      config.limits(TransactionType.Transfer)
    )
    val withdrawalLimit = CurriedRules.checkLimit(
      config.limits(TransactionType.Withdrawal)
    )

    transferLimit(transaction("999999.99")) shouldBe true
    transferLimit(transaction("1000000")) shouldBe false
    withdrawalLimit(
      transaction("49999.99", TransactionType.Withdrawal)
    ) shouldBe true
    withdrawalLimit(
      transaction("50000", TransactionType.Withdrawal)
    ) shouldBe false

  it should "configurer le même moteur pour MAD et EUR" in:
    val mad = PureEngineProfiles.clearingMAD
    val eur = PureEngineProfiles.clearingEUR

    mad.referenceCurrency shouldBe Currency.MAD
    eur.referenceCurrency shouldBe Currency.EUR
    mad.limits shouldBe eur.limits
    mad.feeRates shouldBe eur.feeRates
    mad.ratesToReference should contain(
      Currency.EUR -> BigDecimal("10.80")
    )
    eur.ratesToReference should contain(
      Currency.EUR -> BigDecimal("1")
    )
    all(mad.ratesToReference.values.toList) should be > BigDecimal(0)
    all(eur.ratesToReference.values.toList) should be > BigDecimal(0)
    all(mad.feeRates.values.toList) should be >= BigDecimal(0)
