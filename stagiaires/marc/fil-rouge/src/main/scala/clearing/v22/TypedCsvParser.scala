package clearing.v22

import clearing.model.{Currency, TransactionType}
import clearing.v22.DomainTypes.*
import scala.util.Try

object TypedCsvParser:
  def parse(
    line: NumberedLine
  ): Either[V22Error, TypedTransactionLine] =
    line.value.split(",", -1).map(_.trim) match
      case Array(
            idRaw,
            senderRaw,
            receiverRaw,
            sourceIbanRaw,
            destinationIbanRaw,
            amountRaw,
            transactionTypeRaw,
            currencyRaw
          ) =>
        for
          id <- Try(idRaw.toInt).toEither.left.map(_ =>
            V22ParsingError(line.lineNumber, V22ParsingFailure.InvalidId)
          )
          sender <- BankCode.from(senderRaw).left.map(_ =>
            V22ParsingError(line.lineNumber, V22ParsingFailure.InvalidSender)
          )
          receiver <- BankCode.from(receiverRaw).left.map(_ =>
            V22ParsingError(line.lineNumber, V22ParsingFailure.InvalidReceiver)
          )
          sourceIban <- Iban.from(sourceIbanRaw).left.map(_ =>
            V22ParsingError(
              line.lineNumber,
              V22ParsingFailure.InvalidSourceIban
            )
          )
          destinationIban <- Iban.from(destinationIbanRaw).left.map(_ =>
            V22ParsingError(
              line.lineNumber,
              V22ParsingFailure.InvalidDestinationIban
            )
          )
          amount <- Money.parse(amountRaw).left.map(_ =>
            V22ParsingError(line.lineNumber, V22ParsingFailure.InvalidAmount)
          )
          transactionType <- TransactionType
            .fromCode(transactionTypeRaw)
            .toRight(
              V22ParsingError(
                line.lineNumber,
                V22ParsingFailure.InvalidTransactionType
              )
            )
          currency <- Currency
            .fromString(currencyRaw)
            .toRight(
              V22ParsingError(
                line.lineNumber,
                V22ParsingFailure.InvalidCurrency
              )
            )
        yield TypedTransactionLine(
          lineNumber = line.lineNumber,
          transaction = Transaction(
            id = id,
            sender = sender,
            receiver = receiver,
            sourceIban = sourceIban,
            destinationIban = destinationIban,
            amount = amount,
            transactionType = transactionType,
            currency = currency
          ),
          label = None,
          warnings = Nil
        )
      case columns =>
        Left(
          V22ParsingError(
            line.lineNumber,
            V22ParsingFailure.ColumnCount(columns.length)
          )
        )
