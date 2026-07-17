package clearing.v22

import clearing.model.{Currency, TransactionStatus, TransactionType}
import clearing.v22.DomainTypes.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TypedCsvParserSpec extends AnyFlatSpec with Matchers:
  private val sourceIban = "MA64ATH00000000000000000"
  private val destinationIban = "MA64CIH00000000000000000"

  private def csv(
    id: String = "1",
    sender: String = "ATH",
    receiver: String = "CIH",
    source: String = sourceIban,
    destination: String = destinationIban,
    amount: String = "100",
    transactionType: String = "VIR",
    currency: String = "MAD"
  ): String =
    List(
      id,
      sender,
      receiver,
      source,
      destination,
      amount,
      transactionType,
      currency
    ).mkString(",")

  "TypedCsvParser.parse" should "construire exclusivement le domaine opaque" in:
    val result = TypedCsvParser.parse(NumberedLine(7, csv()))

    val projected = result.map: rail =>
      (
        rail.lineNumber,
        rail.transaction.id,
        rail.transaction.sender.value,
        rail.transaction.receiver.value,
        rail.transaction.sourceIban.value,
        rail.transaction.destinationIban.value,
        rail.transaction.amount.value,
        rail.transaction.transactionType,
        rail.transaction.currency,
        rail.transaction.status
      )

    projected shouldBe Right(
      (
        7,
        1,
        "ATH",
        "CIH",
        sourceIban,
        destinationIban,
        BigDecimal(100),
        TransactionType.Transfer,
        Currency.MAD,
        TransactionStatus.Pending
      )
    )

  it should "nommer chaque cause syntaxique dans l'ordre des champs" in:
    val cases = List(
      "ATH,CIH" -> V22ParsingFailure.ColumnCount(2),
      csv(id = "abc") -> V22ParsingFailure.InvalidId,
      csv(sender = "A1H") -> V22ParsingFailure.InvalidSender,
      csv(receiver = "12") -> V22ParsingFailure.InvalidReceiver,
      csv(source = "FR64ATH00000000000000000") ->
        V22ParsingFailure.InvalidSourceIban,
      csv(destination = "MA64") ->
        V22ParsingFailure.InvalidDestinationIban,
      csv(amount = "cent") -> V22ParsingFailure.InvalidAmount,
      csv(transactionType = "XXX") ->
        V22ParsingFailure.InvalidTransactionType,
      csv(currency = "GBP") -> V22ParsingFailure.InvalidCurrency
    )

    cases.foreach: (line, failure) =>
      TypedCsvParser.parse(NumberedLine(3, line)) shouldBe
        Left(V22ParsingError(3, failure))

  it should "préserver la première erreur de gauche" in:
    TypedCsvParser.parse(
      NumberedLine(4, csv(id = "abc", sender = "A1", amount = "cent"))
    ) shouldBe Left(
      V22ParsingError(4, V22ParsingFailure.InvalidId)
    )

  it should "ne conserver ni ligne brute ni IBAN invalide dans l'erreur" in:
    val secret = "FR64SECRET000000000000000"
    val line = csv(source = secret)
    val error = TypedCsvParser.parse(NumberedLine(5, line)).left.toOption.get

    error.toString should not include secret
    error.toString should not include line
