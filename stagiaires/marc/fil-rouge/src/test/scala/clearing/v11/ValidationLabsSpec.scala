package clearing.v11

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ValidationLabsSpec extends AnyFlatSpec with Matchers:
  private val athIban = "MA64ATH00000000000000000"
  private val cihIban = "MA64CIH00000000000000000"

  private val transfer = Transaction(
    1,
    "ATH",
    "CIH",
    BigDecimal("100"),
    TransactionType.Transfer,
    sourceIban = athIban,
    destinationIban = cihIban
  )

  "ErrorQueries" should "extraire les messages de validation avec une for-comprehension" in:
    val errors: List[ClearingError] = List(
      InvalidAmount(BigDecimal("-1")),
      DuplicateTransaction,
      FieldValidationError("iban", "est absent")
    )

    ErrorQueries.validationMessages(errors) shouldBe List(
      "-1 DH est invalide",
      "est absent"
    )

  it should "extraire les codes des erreurs métier" in:
    val errors: List[ClearingError] = List(
      UnknownBank("ZZZ"),
      DuplicateTransaction,
      Iso20022Rejection(Iso20022Code.AM04, 9)
    )

    ErrorQueries.businessCodes(errors) shouldBe List("DUPL", "AM04")

  "FeePipeline" should "exposer les trois fonctions Option demandées" in:
    FeePipeline.fetchTransaction(1) shouldBe Some(transfer)
    FeePipeline.fetchTransaction(999) shouldBe None
    FeePipeline.fetchExchangeRate("EUR") shouldBe Some(BigDecimal("0.092"))
    FeePipeline.fetchExchangeRate("GBP") shouldBe None
    FeePipeline.calculateFees(BigDecimal("100")) shouldBe Some(BigDecimal("1.00"))
    FeePipeline.calculateFees(BigDecimal("-1")) shouldBe None

  it should "chaîner transaction, taux et frais dans un for-yield" in:
    FeePipeline.finalAmount(1, "EUR") shouldBe Some(BigDecimal("9.29"))
    FeePipeline.finalAmount(999, "EUR") shouldBe None
    FeePipeline.finalAmount(1, "GBP") shouldBe None

  "CleanTransferPipeline" should "conserver seulement les virements positifs parsables" in:
    val lines = List(
      s"1,ATH,CIH,$athIban,$cihIban,100,VIR,MAD",
      s"2,ATH,CIH,$athIban,$cihIban,50,PRE,MAD",
      s"3,ATH,CIH,$athIban,$cihIban,-5,VIR,MAD",
      "ligne,invalide"
    )

    CleanTransferPipeline.clean(lines).map(_.id) shouldBe List(1)

  "InternationalFeePipeline" should "appliquer les frais depuis les lignes CSV" in:
    val lines = List(
      s"1,ATH,CIH,FR64ATH00000000000000000,$cihIban,100,VIR,EUR",
      s"2,ATH,CIH,$athIban,$cihIban,100,VIR,MAD"
    )

    InternationalFeePipeline.adjust(lines).map(_.amount) shouldBe List(
      BigDecimal("102.00"),
      BigDecimal("100")
    )

  "BatchValidationLab" should "produire l'adaptateur tuple exact du cours" in:
    val invalid = transfer.copy(amount = BigDecimal("-1"))
    val pairs: List[(Transaction, List[ClearingError])] =
      BatchValidationLab.assessmentPairs(List(transfer, invalid))

    pairs.head shouldBe (transfer, Nil)
    pairs(1) shouldBe (invalid, List(InvalidAmount(BigDecimal("-1"))))
