package clearing.v21

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class RailRecoverySpec extends AnyFlatSpec with Matchers:
  private val rail = RailTransaction(
    lineNumber = 2,
    transaction = Transaction(
      id = 9,
      sender = "ATH",
      receiver = "CIH",
      amount = BigDecimal("100"),
      transactionType = TransactionType.Transfer
    ),
    warnings = Nil
  )

  "RailRecovery.recover" should "conserver un libellé présent sans avertissement" in:
    RailRecovery.recover(Some("  Facture juillet  "))(rail) shouldBe Right(
      rail.copy(label = Some("Facture juillet"))
    )

  it should "remplacer un libellé absent et tracer la récupération" in:
    RailRecovery.recover(None)(rail) shouldBe Right(
      rail.copy(
        label = Some("NON RENSEIGNE"),
        warnings = List(LightWarning.MissingLabel("NON RENSEIGNE"))
      )
    )
    RailRecovery.recover(Some("   "))(rail) shouldBe Right(
      rail.copy(
        label = Some("NON RENSEIGNE"),
        warnings = List(LightWarning.MissingLabel("NON RENSEIGNE"))
      )
    )

  "RailRecovery.recoverOnRight" should "laisser les erreurs bloquantes sur le rail gauche" in:
    val errors: List[ClearingError] = List(
      ParsingError(2, ParsingFailure.InvalidAmount),
      TransactionValidationError(2, Some(9), List("IBAN_SOURCE_INVALIDE")),
      Iso20022Rejection(Iso20022Code.AM05, 9),
      FileReadFailure("input.csv", "illisible")
    )

    errors.foreach: error =>
      RailRecovery.recoverOnRight(Left(error), None) shouldBe Left(error)
