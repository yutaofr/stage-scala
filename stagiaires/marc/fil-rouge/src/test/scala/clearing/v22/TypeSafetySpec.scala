package clearing.v22

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.compiletime.testing.{typeCheckErrors, typeChecks}

final class TypeSafetySpec extends AnyFlatSpec with Matchers:
  "DomainTypes" should "autoriser seulement les constructions explicites" in:
    typeChecks("""
      import clearing.v22.DomainTypes.*
      val code: BankCode = BankCode.unsafe("ATH")
      val iban: Iban = Iban.unsafe("MA64ATH00000000000000000")
      val amount: Money = Money(BigDecimal(10))
    """) shouldBe true

  it should "distinguer BankCode et Iban malgré leur représentation String" in:
    typeCheckErrors("""
      import clearing.v22.DomainTypes.*
      val code: BankCode = BankCode.unsafe("ATH")
      val iban: Iban = code
    """) should not be empty

  it should "refuser les primitives brutes à la place des opaques" in:
    typeCheckErrors("""
      import clearing.v22.DomainTypes.*
      val code: BankCode = "ATH"
    """) should not be empty

    typeCheckErrors("""
      import clearing.v22.DomainTypes.*
      val iban: Iban = "MA64ATH00000000000000000"
    """) should not be empty

    typeCheckErrors("""
      import clearing.v22.DomainTypes.*
      val amount: Money = BigDecimal(10)
    """) should not be empty

  it should "fermer les deux rails de la frontière de hash" in:
    typeCheckErrors("""
      import clearing.v22.*
      import clearing.v22.DomainTypes.*
      val leaking: HashBoundary = iban => Right(iban.value)
    """) should not be empty

    typeCheckErrors("""
      import clearing.v22.*
      import clearing.v22.DomainTypes.*
      val leakingError: HashBoundary = _ => Left(
        V22TechnicalError(0, None, "hash-iban", "Raw", "MA64ATH00000000000000000")
      )
    """) should not be empty
