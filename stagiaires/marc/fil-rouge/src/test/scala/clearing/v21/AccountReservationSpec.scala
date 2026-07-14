package clearing.v21

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class AccountReservationSpec extends AnyFlatSpec with Matchers:
  private val account = RailAccount("A-1", BigDecimal("100"))
  private val accounts = Map(account.id -> account)

  "AccountReservation.reserve" should "réserver sur une copie immuable du compte" in:
    AccountReservation.reserve("A-1", BigDecimal("30"), accounts) shouldBe
      Right(account.copy(balance = BigDecimal("70")))
    accounts shouldBe Map("A-1" -> account)

  it should "court-circuiter un compte absent" in:
    AccountReservation.reserve("ABSENT", BigDecimal("30"), accounts) shouldBe
      Left(ReservationError.AccountNotFound("ABSENT"))
    accounts shouldBe Map("A-1" -> account)

  it should "refuser une réservation non positive" in:
    AccountReservation.reserve("A-1", BigDecimal(0), accounts) shouldBe
      Left(ReservationError.NonPositiveAmount(BigDecimal(0)))

  it should "préserver le solde lorsque les fonds sont insuffisants" in:
    AccountReservation.reserve("A-1", BigDecimal("101"), accounts) shouldBe
      Left(
        ReservationError.InsufficientBalance(
          accountId = "A-1",
          available = BigDecimal("100"),
          requested = BigDecimal("101")
        )
      )
    accounts("A-1").balance shouldBe BigDecimal("100")
