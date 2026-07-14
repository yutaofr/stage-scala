package clearing.v20

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class PureNettingCalculatorSpec extends AnyFlatSpec with Matchers:
  private def prepared(
    id: Int,
    sender: String,
    receiver: String,
    amount: String,
    status: TransactionStatus = TransactionStatus.Validated
  ): PreparedTransaction =
    PreparedTransaction(
      id = id,
      sender = sender,
      receiver = receiver,
      settlementAmount = BigDecimal(amount),
      transactionType = TransactionType.Transfer,
      status = status,
      referenceCurrency = Currency.MAD,
      fee = BigDecimal("0.10"),
      sourceIbanHash = "source-hash",
      destinationIbanHash = "destination-hash"
    )

  "PureNettingCalculator.positions" should "débiter et créditer en un fold immutable" in:
    val transactions = List(
      prepared(1, "ATH", "CIH", "100"),
      prepared(2, "CIH", "ATH", "40")
    )

    PureNettingCalculator.positions(transactions) shouldBe Map(
      "ATH" -> BigDecimal("-60"),
      "CIH" -> BigDecimal("60")
    )

  it should "ignorer un statut rejeté" in:
    val transactions = List(
      prepared(1, "ATH", "CIH", "100"),
      prepared(2, "BOA", "ATH", "50", TransactionStatus.Rejected)
    )

    PureNettingCalculator.positions(transactions) shouldBe Map(
      "ATH" -> BigDecimal("-100"),
      "CIH" -> BigDecimal("100")
    )

  it should "retourner une Map vide pour un batch vide" in:
    PureNettingCalculator.positions(Nil) shouldBe Map.empty

  it should "préserver la somme globale nulle" in:
    val result = PureNettingCalculator.positions(
      List(
        prepared(1, "ATH", "CIH", "100"),
        prepared(2, "CIH", "BOA", "25"),
        prepared(3, "BOA", "ATH", "10")
      )
    )

    result.values.sum shouldBe BigDecimal(0)

  it should "produire exactement le même résultat mille fois" in:
    val input = List(
      prepared(1, "ATH", "CIH", "100"),
      prepared(2, "CIH", "ATH", "40")
    )
    val snapshot = input
    val expected = PureNettingCalculator.positions(input)

    val results = List.fill(1000)(PureNettingCalculator.positions(input))

    all(results) shouldBe expected
    input shouldBe snapshot
