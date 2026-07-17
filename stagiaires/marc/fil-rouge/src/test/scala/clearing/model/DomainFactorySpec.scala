package clearing.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class DomainFactorySpec extends AnyFlatSpec with Matchers:
  private val athIban = "MA64ATH00000000000000000"
  private val cihIban = "MA64CIH00000000000000000"

  "Currency.fromString" should "reconnaître les trois devises sans tenir compte de la casse" in:
    Currency.fromString("mad") shouldBe Some(Currency.MAD)
    Currency.fromString(" EUR ") shouldBe Some(Currency.EUR)
    Currency.fromString("usd") shouldBe Some(Currency.USD)
    Currency.fromString("GBP") shouldBe None

  "Iban.apply" should "construire seulement un IBAN marocain de 24 caractères" in:
    Iban(athIban.toLowerCase).map(_.value) shouldBe Some(athIban)
    Iban("FR64ATH00000000000000000") shouldBe None
    Iban("MA64COURT") shouldBe None

  "Iban.unapply" should "extraire pays, banque et compte" in:
    Iban.unapply(athIban) shouldBe Some(
      ("MA", "ATH00", "000000000000000")
    )

  it should "refuser la construction directe hors du compagnon" in:
    assertDoesNotCompile(
      "new clearing.model.Iban(\"MA64ATH00000000000000000\")"
    )

  "Transaction.fromCsv" should "construire un candidat v1.1 à huit colonnes" in:
    val parsed = Transaction.fromCsv(
      s"7,ATH,CIH,$athIban,$cihIban,125.50,VIR,eur"
    )

    parsed.map(_.id) shouldBe Some(7)
    parsed.map(_.sourceIban) shouldBe Some(athIban)
    parsed.map(_.destinationIban) shouldBe Some(cihIban)
    parsed.map(_.amount) shouldBe Some(BigDecimal("125.50"))
    parsed.map(_.transactionType) shouldBe Some(TransactionType.Transfer)
    parsed.map(_.currency) shouldBe Some(Currency.EUR)

  it should "rejeter la forme, les nombres, le type et la devise illisibles" in:
    Transaction.fromCsv("1,ATH") shouldBe None
    Transaction.fromCsv(
      s"id,ATH,CIH,$athIban,$cihIban,10,VIR,MAD"
    ) shouldBe None
    Transaction.fromCsv(
      s"1,ATH,CIH,$athIban,$cihIban,dix,VIR,MAD"
    ) shouldBe None
    Transaction.fromCsv(
      s"1,ATH,CIH,$athIban,$cihIban,10,CB,MAD"
    ) shouldBe None
    Transaction.fromCsv(
      s"1,ATH,CIH,$athIban,$cihIban,10,VIR,GBP"
    ) shouldBe None

  it should "conserver une valeur métier invalide pour le validateur détaillé" in:
    Transaction.fromCsv(
      s"8,ATH,CIH,XX64ATH00000000000000000,$cihIban,-10,VIR,MAD"
    ).map(_.amount) shouldBe Some(BigDecimal("-10"))

  "Les valeurs par défaut S5" should "préserver les anciens constructeurs" in:
    val transaction = Transaction(
      1,
      "ATH",
      "CIH",
      BigDecimal("10"),
      TransactionType.Transfer
    )

    transaction.sourceIban shouldBe Transaction.DefaultSourceIban
    transaction.destinationIban shouldBe Transaction.DefaultDestinationIban
    transaction.currency shouldBe Currency.MAD
