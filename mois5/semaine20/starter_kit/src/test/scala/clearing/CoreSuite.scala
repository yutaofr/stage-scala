package clearing

import clearing.contract.*
import clearing.core.*
import clearing.model.*
import munit.FunSuite

final class CoreSuite extends FunSuite:
  private val awb = BankCode.unsafe("AWB")
  private val cih = BankCode.unsafe("CIH")
  private val knownBanks = Set(awb, cih)

  private def transaction(id: String, sender: BankCode, receiver: BankCode, amount: String) =
    Transaction(
      TransactionId.unsafe(id),
      sender,
      receiver,
      Money(BigDecimal(amount))
    )

  test("validation conserve la transaction et fixe le statut"):
    val result = TransactionValidator(knownBanks).validate(transaction("tx-1", awb, cih, "100"))
    assertEquals(result.map(_.status), Right(TransactionStatus.Validated))

  test("netting conserve la somme des positions"):
    val positions = PureNettingCalculator.calculateNetPositions(
      List(
        transaction("tx-1", awb, cih, "100"),
        transaction("tx-2", cih, awb, "40")
      )
    )
    assertEquals(positions.values.map(_.value).sum, BigDecimal(0))

  test("codec TransactionSubmittedV1 est réversible"):
    val event = TransactionSubmittedV1("tx-1", "AWB", "CIH", BigDecimal("100"))
    assertEquals(ContractCodec.decode(ContractCodec.encode(event)), Right(event))

  test("la frontière convertit les primitives vers les types opaques"):
    val event = TransactionSubmittedV1("tx-1", "awb", "CIH", BigDecimal("100"))
    val result = event.toDomain(knownBanks)
    assertEquals(result.map(_.sender.value), Right("AWB"))
