package clearing.v23

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.PreparedTransaction
import clearing.v22.DomainTypes.*
import clearing.v22.DomainTypes.given
import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.PropertyCheckConfiguration

final class PropertySpec
    extends AnyFlatSpec
    with Matchers
    with ScalaCheckPropertyChecks:
  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10000)

  private val ath = BankCode.unsafe("ATH")
  private val boa = BankCode.unsafe("BOA")
  private val cih = BankCode.unsafe("CIH")
  private val sourceHash = IbanHash.from("a" * 64).toOption.get
  private val destinationHash = IbanHash.from("b" * 64).toOption.get

  private val bankPairGen: Gen[(BankCode, BankCode)] = Gen.oneOf(
    ath -> boa,
    ath -> cih,
    boa -> ath,
    boa -> cih,
    cih -> ath,
    cih -> boa
  )

  private val amountGen: Gen[Money] =
    Gen
      .choose(1L, 100000000L)
      .map(cents => Money(BigDecimal(cents) / 100))

  private val transactionGen: Gen[PreparedTransaction] =
    for
      id <- Gen.choose(1, Int.MaxValue)
      (sender, receiver) <- bankPairGen
      amount <- amountGen
    yield PreparedTransaction(
      id = id,
      sender = sender,
      receiver = receiver,
      settlementAmount = amount,
      transactionType = TransactionType.Transfer,
      status = TransactionStatus.Validated,
      referenceCurrency = Currency.MAD,
      fee = Money.zero,
      sourceIbanHash = sourceHash,
      destinationIbanHash = destinationHash,
      label = s"transaction-$id",
      warnings = Nil
    )

  private val batchGen: Gen[List[PreparedTransaction]] =
    Gen.listOfN(200, transactionGen)

  "V23Netting.positions" should "conserver une somme globale nulle sur 10 000 batchs" in:
    forAll(batchGen): batch =>
      V23Netting.positions(batch).values.sum shouldBe Money.zero

  it should "annuler exactement le débit et le crédit d'un auto-virement" in:
    val selfTransfer = PreparedTransaction(
      id = 1,
      sender = ath,
      receiver = ath,
      settlementAmount = Money(BigDecimal("125.75")),
      transactionType = TransactionType.Transfer,
      status = TransactionStatus.Validated,
      referenceCurrency = Currency.MAD,
      fee = Money.zero,
      sourceIbanHash = sourceHash,
      destinationIbanHash = destinationHash,
      label = "auto-virement",
      warnings = Nil
    )

    V23Netting.positions(List(selfTransfer)) shouldBe Map(ath -> Money.zero)
