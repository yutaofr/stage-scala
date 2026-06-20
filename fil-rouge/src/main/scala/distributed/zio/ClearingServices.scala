package distributed.zio

import clearing.core.*
import clearing.model.*
import zio.*

trait ValidationService:
  def validate(tx: Transaction): IO[ClearingError, Transaction]

object ValidationService:
  def live(knownBanks: Set[BankCode]): ULayer[ValidationService] =
    ZLayer.succeed {
      val validator = TransactionValidator(knownBanks)
      new ValidationService:
        def validate(tx: Transaction): IO[ClearingError, Transaction] =
          ZIO.fromEither(validator.validate(tx))
    }

trait NettingService:
  def calculate(txs: List[Transaction]): UIO[Map[BankCode, Money]]

object NettingService:
  val live: ULayer[NettingService] =
    ZLayer.succeed {
      new NettingService:
        def calculate(txs: List[Transaction]): UIO[Map[BankCode, Money]] =
          ZIO.succeed(PureNettingCalculator.calculateNetPositions(txs))
    }
