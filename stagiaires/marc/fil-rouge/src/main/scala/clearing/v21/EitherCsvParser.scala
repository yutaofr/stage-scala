package clearing.v21

import clearing.model.*
import java.util.Locale
import scala.util.Try

object EitherCsvParser:
  def parse(line: NumberedLine): Either[ParsingError, RailTransaction] =
    line.value.split(",", -1).map(_.trim) match
      case Array(
            idRaw,
            sender,
            receiver,
            sourceIban,
            destinationIban,
            amountRaw,
            transactionTypeRaw,
            currencyRaw
          ) =>
        for
          id <- Try(idRaw.toInt).toEither.left.map(_ =>
            ParsingError(line.lineNumber, ParsingFailure.InvalidId)
          )
          amount <- Try(BigDecimal(amountRaw)).toEither.left.map(_ =>
            ParsingError(line.lineNumber, ParsingFailure.InvalidAmount)
          )
          transactionType <- TransactionType
            .fromCode(transactionTypeRaw)
            .toRight(
              ParsingError(
                line.lineNumber,
                ParsingFailure.InvalidTransactionType
              )
            )
          currency <- Currency
            .fromString(currencyRaw)
            .toRight(
              ParsingError(line.lineNumber, ParsingFailure.InvalidCurrency)
            )
        yield RailTransaction(
          lineNumber = line.lineNumber,
          transaction = Transaction(
            id = id,
            sender = sender.toUpperCase(Locale.ROOT),
            receiver = receiver.toUpperCase(Locale.ROOT),
            amount = amount,
            transactionType = transactionType,
            sourceIban = sourceIban.toUpperCase(Locale.ROOT),
            destinationIban = destinationIban.toUpperCase(Locale.ROOT),
            currency = currency
          ),
          warnings = Nil
        )
      case columns =>
        Left(
          ParsingError(
            line.lineNumber,
            ParsingFailure.ColumnCount(columns.length)
          )
        )
