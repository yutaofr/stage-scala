package clearing.v21

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class EitherCsvParserSpec extends AnyFlatSpec with Matchers:
  private val sourceIban = "MA64ATH00000000000000000"
  private val destinationIban = "MA64CIH00000000000000000"
  private val validCsv =
    s"42,ath,cih,$sourceIban,$destinationIban,100.25,vir,mad"

  "EitherCsvParser.parse" should "construire une transaction numérotée sans perdre les détails" in:
    EitherCsvParser.parse(NumberedLine(4, validCsv)) shouldBe Right(
      RailTransaction(
        lineNumber = 4,
        transaction = Transaction(
          id = 42,
          sender = "ATH",
          receiver = "CIH",
          amount = BigDecimal("100.25"),
          transactionType = TransactionType.Transfer,
          sourceIban = sourceIban,
          destinationIban = destinationIban,
          currency = Currency.MAD
        ),
        warnings = Nil
      )
    )

  it should "nommer un nombre de colonnes incorrect sans conserver la ligne" in:
    val result = EitherCsvParser.parse(NumberedLine(7, "1,ATH"))

    result shouldBe Left(
      ParsingError(7, ParsingFailure.ColumnCount(actual = 2))
    )
    result.toString should not include "1,ATH"

  it should "distinguer chaque champ syntaxique illisible" in:
    val invalidId =
      s"abc,ATH,CIH,$sourceIban,$destinationIban,100,VIR,MAD"
    val invalidAmount =
      s"1,ATH,CIH,$sourceIban,$destinationIban,cent,VIR,MAD"
    val invalidType =
      s"1,ATH,CIH,$sourceIban,$destinationIban,100,CARTE,MAD"
    val invalidCurrency =
      s"1,ATH,CIH,$sourceIban,$destinationIban,100,VIR,GBP"

    EitherCsvParser.parse(NumberedLine(1, invalidId)) shouldBe
      Left(ParsingError(1, ParsingFailure.InvalidId))
    EitherCsvParser.parse(NumberedLine(2, invalidAmount)) shouldBe
      Left(ParsingError(2, ParsingFailure.InvalidAmount))
    EitherCsvParser.parse(NumberedLine(3, invalidType)) shouldBe
      Left(ParsingError(3, ParsingFailure.InvalidTransactionType))
    EitherCsvParser.parse(NumberedLine(4, invalidCurrency)) shouldBe
      Left(ParsingError(4, ParsingFailure.InvalidCurrency))

  it should "préserver la première erreur de gauche" in:
    val severalInvalidFields =
      s"abc,ATH,CIH,$sourceIban,$destinationIban,cent,CARTE,GBP"

    EitherCsvParser.parse(NumberedLine(9, severalInvalidFields)) shouldBe
      Left(ParsingError(9, ParsingFailure.InvalidId))
