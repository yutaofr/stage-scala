package clearing.v12

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class BilateralNettingSpec extends AnyFlatSpec with Matchers:
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

  "S7TransactionGenerator" should "générer cent transactions déterministes entre cinq banques" in:
    val first = S7TransactionGenerator.generateVector(100, seed = 42L)
    val second = S7TransactionGenerator.generateVector(100, seed = 42L)

    first shouldBe second
    first should have size 100
    first.map(_.sender).distinct should have size 5
    first.map(_.receiver).distinct should have size 5
    all(first.map(transaction => transaction.sender != transaction.receiver)) shouldBe true
    first.map(_.status).distinct shouldBe Vector(TransactionStatus.Validated)

  it should "varier le flux selon le seed et couvrir la matrice bilatérale" in:
    val first = S7TransactionGenerator.generateVector(1000, seed = 42L)
    val second = S7TransactionGenerator.generateVector(1000, seed = 99L)

    first should not be second
    first.map(transaction => (transaction.sender, transaction.receiver)).distinct should
      have size 20
    first.map(_.amount).distinct.size should be > 100

  it should "refuser une taille négative" in:
    an[IllegalArgumentException] should be thrownBy:
      S7TransactionGenerator.generateVector(-1)

  "BilateralNetting.indexByPair" should "grouper seulement les transactions validées" in:
    val transactions = List(
      transaction(1, "ATH", "CIH", "100"),
      transaction(2, "ATH", "CIH", "50"),
      transaction(3, "CIH", "ATH", "40"),
      transaction(
        4,
        "ATH",
        "CIH",
        "999",
        TransactionStatus.Rejected
      )
    )

    BilateralNetting.indexByPair(transactions).view
      .mapValues(_.size)
      .toMap shouldBe Map(
      ("ATH", "CIH") -> 2,
      ("CIH", "ATH") -> 1
    )

  "BilateralNetting.totalsByPair" should "agréger les montants dirigés avec mapValues" in:
    val totals = BilateralNetting.totalsByPair(
      List(
        transaction(1, "ATH", "CIH", "100"),
        transaction(2, "ATH", "CIH", "50"),
        transaction(3, "CIH", "ATH", "40")
      )
    )

    totals shouldBe Map(
      ("ATH", "CIH") -> BigDecimal("150"),
      ("CIH", "ATH") -> BigDecimal("40")
    )

  it should "exposer les mêmes totaux avec des BankPair nommées" in:
    val totals = BilateralNetting.namedTotalsByPair(
      List(transaction(1, "ATH", "CIH", "25"))
    )

    totals shouldBe Map(
      BankPair(
        BankDirectory.fromCode("ATH"),
        BankDirectory.fromCode("CIH")
      ) -> BigDecimal("25")
    )

  "BilateralNetting.getNetDuo" should "retourner le solde signé dans les deux sens" in:
    val totals = Map(
      ("ATH", "CIH") -> BigDecimal("150"),
      ("CIH", "ATH") -> BigDecimal("40")
    )

    BilateralNetting.getNetDuo("ATH", "CIH", totals) shouldBe
      BigDecimal("110")
    BilateralNetting.getNetDuo("CIH", "ATH", totals) shouldBe
      BigDecimal("-110")
    BilateralNetting.getNetDuo("ATH", "BOA", totals) shouldBe
      BigDecimal(0)

  "BilateralNetting.settlements" should "produire un seul règlement positif par duo" in:
    val settlements = BilateralNetting.settlements(
      List(
        transaction(1, "ATH", "CIH", "150"),
        transaction(2, "CIH", "ATH", "40"),
        transaction(3, "ATH", "BOA", "20"),
        transaction(4, "BOA", "ATH", "20")
      )
    )

    settlements shouldBe List(
      BilateralSettlement(
        debtor = BankDirectory.fromCode("ATH"),
        creditor = BankDirectory.fromCode("CIH"),
        amount = BigDecimal("110")
      )
    )
