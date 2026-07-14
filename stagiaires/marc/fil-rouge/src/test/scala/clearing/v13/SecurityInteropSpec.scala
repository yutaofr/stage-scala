package clearing.v13

import clearing.model.*
import java.time.{Clock, Instant, ZoneOffset}
import java.util.UUID
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class SecurityInteropSpec extends AnyFlatSpec with Matchers:
  private val sourceIban = "MA64ATH00000000000000000"
  private val destinationIban = "MA64CIH00000000000000000"

  private def transaction(id: Int): Transaction =
    Transaction(
      id = id,
      sender = "ATH",
      receiver = "CIH",
      amount = BigDecimal("125.50"),
      transactionType = TransactionType.Transfer,
      status = TransactionStatus.Validated,
      sourceIban = sourceIban,
      destinationIban = destinationIban,
      currency = Currency.MAD
    )

  "SecurityUtils.hashIban" should "produire le SHA-256 UTF-8 hexadécimal attendu" in:
    SecurityUtils.hashIban(sourceIban) shouldBe
      "f528910ebd6e1661f465f3538e4e0b3f2e203e8d29fe4412e6c1c350f7fdecfc"

  it should "être déterministe et produire soixante-quatre caractères minuscules" in:
    val first = SecurityUtils.hashIban(destinationIban)
    val second = SecurityUtils.hashIban(destinationIban)

    first shouldBe second
    first should fullyMatch regex "[0-9a-f]{64}"
    first should not be SecurityUtils.hashIban(sourceIban)

  "BankTime.format" should "utiliser la date bancaire marocaine" in:
    val timestamp = Instant
      .parse("2026-07-14T12:34:56Z")
      .atZone(BankTime.MoroccoZone)

    BankTime.format(timestamp) shouldBe "14/07/2026 13:34:56"

  "SecureBatchFactory.create" should "dater et anonymiser chaque transaction avec le UUID fourni" in:
    val clock = Clock.fixed(
      Instant.parse("2026-07-14T12:34:56Z"),
      ZoneOffset.UTC
    )
    val batchId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

    val batch = SecureBatchFactory.create(
      List(transaction(1), transaction(2)),
      clock,
      () => batchId
    )

    batch.id shouldBe batchId
    batch.processedAt.getZone shouldBe BankTime.MoroccoZone
    batch.transactions.map(_.transactionId) shouldBe List(1, 2)
    batch.transactions.map(_.amount) shouldBe List.fill(2)(BigDecimal("125.50"))
    batch.transactions.map(_.formattedAt).distinct shouldBe
      List("14/07/2026 13:34:56")
    all(batch.transactions.map(_.sourceIbanHash)) shouldBe
      SecurityUtils.hashIban(sourceIban)
    all(batch.transactions.map(_.destinationIbanHash)) shouldBe
      SecurityUtils.hashIban(destinationIban)
    batch.transactions.mkString should not include sourceIban
    batch.transactions.mkString should not include destinationIban

  it should "créer un identifiant différent pour deux batches réels" in:
    val clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)

    val first = SecureBatchFactory.create(
      Nil,
      clock,
      () => UUID.randomUUID()
    )
    val second = SecureBatchFactory.create(
      Nil,
      clock,
      () => UUID.randomUUID()
    )

    first.id should not be second.id
