package clearing.v13

import clearing.model.*
import java.time.{Clock, Instant, ZoneOffset}
import java.util.UUID
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Repository, Service}

final class SpringServicesSpec extends AnyFlatSpec with Matchers:
  private val fixedClock =
    Clock.fixed(Instant.parse("2026-07-14T08:30:00Z"), ZoneOffset.UTC)
  private val fixedUuid = UUID.fromString("11111111-2222-3333-4444-555555555555")

  private def transaction(
    id: Int,
    sender: String,
    receiver: String,
    amount: String,
    currency: Currency = Currency.MAD
  ): Transaction =
    Transaction(
      id = id,
      sender = sender,
      receiver = receiver,
      amount = BigDecimal(amount),
      transactionType = TransactionType.Transfer,
      status = TransactionStatus.Validated,
      currency = currency
    )

  "BankRepository" should "être un repository Java et protéger sa collection interne" in:
    classOf[BankRepository].isAnnotationPresent(classOf[Repository]) shouldBe true
    val repository = BankRepository()

    val firstRead = repository.getAllBanks()
    firstRead.size() shouldBe 5
    firstRead.clear()

    repository.getAllBanks().size() shouldBe 5

  "SpringTransactionValidator" should "utiliser un component à injection constructeur" in:
    classOf[SpringTransactionValidator]
      .isAnnotationPresent(classOf[Component]) shouldBe true
    classOf[SpringTransactionValidator]
      .getConstructors
      .exists(_.isAnnotationPresent(classOf[Autowired])) shouldBe true

  it should "rejeter seulement les transactions qui référencent une banque absente" in:
    val banks = java.util.List.of(
      Bank("ATH", "Attijariwafa Bank"),
      Bank("CIH", "CIH Bank")
    )
    val validator = SpringTransactionValidator(BankRepository(banks))
    val valid = transaction(1, "ATH", "CIH", "100")
    val unknownReceiver = transaction(2, "ATH", "BOA", "20")
    val unknownBoth = transaction(3, "SGMB", "BMCE", "30")

    val result = validator.validateBanks(
      List(valid, unknownReceiver, unknownBoth)
    )

    result.accepted shouldBe List(valid)
    result.rejected shouldBe List(
      RepositoryRejection(unknownReceiver, Set("BOA")),
      RepositoryRejection(unknownBoth, Set("BMCE", "SGMB"))
    )

  "ClearingService" should "être un service à injection constructeur" in:
    classOf[ClearingService].isAnnotationPresent(classOf[Service]) shouldBe true
    classOf[ClearingService]
      .getConstructors
      .exists(_.isAnnotationPresent(classOf[Autowired])) shouldBe true

  it should "enchaîner validation, conversion sécurisée et netting" in:
    val provider = new ExchangeRateProvider:
      def fetchRate(currency: Currency): Option[BigDecimal] = currency match
        case Currency.MAD => Some(BigDecimal("1"))
        case Currency.EUR => Some(BigDecimal("10"))
        case Currency.USD => None

    val repository = BankRepository()
    val service = ClearingService(SpringTransactionValidator(repository))
    val transactions = List(
      transaction(1, "ATH", "CIH", "100"),
      transaction(2, "CIH", "ATH", "4", Currency.EUR),
      transaction(3, "BOA", "ATH", "5", Currency.USD),
      transaction(4, "UNKNOWN", "ATH", "10")
    )

    val result = service.process(
      transactions,
      provider,
      fixedClock,
      () => fixedUuid
    )

    result.acceptedBankTransactions.map(_.id) shouldBe List(1, 2, 3)
    result.repositoryRejected.map(_.transaction.id) shouldBe List(4)
    result.convertedTransactions.map(_.id) shouldBe List(1, 2)
    result.convertedTransactions.map(_.amount) shouldBe
      List(BigDecimal("100.00"), BigDecimal("40.00"))
    result.missingRates shouldBe List(MissingRate(Currency.USD, List(3)))
    result.secureBatch.id shouldBe fixedUuid
    result.secureBatch.transactions.map(_.transactionId) shouldBe List(1, 2)
    result.bilateralSettlements.map(_.amount) shouldBe List(BigDecimal("60.00"))
    result.positions shouldBe Map(
      "ATH" -> BigDecimal("-60.00"),
      "CIH" -> BigDecimal("60.00")
    )
    result.positions.values.sum shouldBe BigDecimal(0)
