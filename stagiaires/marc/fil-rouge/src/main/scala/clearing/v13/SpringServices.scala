package clearing.v13

import clearing.model.*
import clearing.v12.{BilateralNetting, BilateralSettlement, MultilateralNetting}
import java.time.Clock
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}
import scala.jdk.CollectionConverters.*

case class RepositoryRejection(
  transaction: Transaction,
  missingCodes: Set[String]
)

case class BankValidationResult(
  accepted: List[Transaction],
  rejected: List[RepositoryRejection]
)

@Component
final class SpringTransactionValidator @Autowired() (
  repository: BankRepository
):
  def validateBanks(transactions: List[Transaction]): BankValidationResult =
    val knownCodes = repository.getAllBanks().asScala.iterator
      .map(_.code)
      .toSet

    val (accepted, rejected) = transactions.foldLeft(
      (List.empty[Transaction], List.empty[RepositoryRejection])
    ):
      case ((valid, invalid), transaction) =>
        val referencedCodes = Set(transaction.sender, transaction.receiver)
        val missingCodes = referencedCodes.diff(knownCodes)
        if missingCodes.isEmpty then
          (transaction :: valid, invalid)
        else
          (valid, RepositoryRejection(transaction, missingCodes) :: invalid)

    BankValidationResult(accepted.reverse, rejected.reverse)

case class ConnectedClearingResult(
  acceptedBankTransactions: List[Transaction],
  repositoryRejected: List[RepositoryRejection],
  convertedTransactions: List[Transaction],
  missingRates: List[MissingRate],
  rates: Map[Currency, BigDecimal],
  secureBatch: SecureBatch,
  bilateralSettlements: List[BilateralSettlement],
  positions: Map[String, BigDecimal]
)

@Service
final class ClearingService @Autowired() (
  validator: SpringTransactionValidator
):
  def process(
    transactions: List[Transaction],
    provider: ExchangeRateProvider,
    clock: Clock,
    uuidSupplier: () => UUID
  ): ConnectedClearingResult =
    val bankValidation = validator.validateBanks(transactions)
    val conversion = CurrencyConversion.convertToMad(
      bankValidation.accepted,
      provider
    )
    val secureBatch = SecureBatchFactory.create(
      conversion.converted,
      clock,
      uuidSupplier
    )

    ConnectedClearingResult(
      acceptedBankTransactions = bankValidation.accepted,
      repositoryRejected = bankValidation.rejected,
      convertedTransactions = conversion.converted,
      missingRates = conversion.rejected,
      rates = conversion.rates,
      secureBatch = secureBatch,
      bilateralSettlements = BilateralNetting.settlements(
        conversion.converted
      ),
      positions = MultilateralNetting.computePositions(
        conversion.converted
      )
    )
