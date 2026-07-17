package clearing.v20

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class DataCleanerSpec extends AnyFlatSpec with Matchers:
  private def transaction(
    id: Int = 1,
    amount: BigDecimal = BigDecimal("10.125"),
    sourceIban: String = " ma64 ath 000 ",
    destinationIban: String = " ma64 cih 000 "
  ): Transaction =
    Transaction(
      id = id,
      sender = "ATH",
      receiver = "CIH",
      amount = amount,
      transactionType = TransactionType.Transfer,
      status = TransactionStatus.Pending,
      sourceIban = sourceIban,
      destinationIban = destinationIban,
      currency = Currency.MAD
    )

  "DataCleaner.cleanIban" should "supprimer les espaces et mettre en majuscules" in:
    DataCleaner.cleanIban(" ma64 ath 000 ") shouldBe "MA64ATH000"

  "DataCleaner.formatAmount" should "arrondir à deux décimales en HALF_UP" in:
    DataCleaner.formatAmount(BigDecimal("10.125")) shouldBe
      BigDecimal("10.13")
    DataCleaner.formatAmount(BigDecimal("10.124")) shouldBe
      BigDecimal("10.12")

  "DataCleaner.validateStatus" should "décrire un montant non positif sans exception" in:
    val invalid = transaction(id = 7, amount = BigDecimal(0))

    DataCleaner.validateStatus(invalid) shouldBe Left(
      PureValidationError.NonPositiveAmount(7)
    )

  it should "conserver une transaction positive" in:
    val valid = transaction()

    DataCleaner.validateStatus(valid) shouldBe Right(valid)

  "DataCleaner.cleanTransaction" should "composer le nettoyage sans changer le domaine métier" in:
    val original = transaction()

    DataCleaner.cleanTransaction(original) shouldBe original.copy(
      amount = BigDecimal("10.13"),
      sourceIban = "MA64ATH000",
      destinationIban = "MA64CIH000"
    )

  "DataCleaner.andThenExample" should "appliquer les fonctions dans l'ordre de lecture" in:
    DataCleaner.andThenExample(" ATH ") shouldBe "[ATH]"

  "DataCleaner.composeExample" should "appliquer d'abord la fonction de droite" in:
    DataCleaner.composeExample(" ATH ") shouldBe "[ ATH ]"
