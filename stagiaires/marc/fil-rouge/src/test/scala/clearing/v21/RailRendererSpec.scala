package clearing.v21

import clearing.model.*
import clearing.v20.PreparedTransaction
import java.util.Locale
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class RailRendererSpec extends AnyFlatSpec with Matchers:
  private val prepared = PreparedTransaction(
    id = 7,
    sender = "ATH",
    receiver = "CIH",
    settlementAmount = BigDecimal("100"),
    transactionType = TransactionType.Transfer,
    status = TransactionStatus.Validated,
    referenceCurrency = Currency.MAD,
    fee = BigDecimal("0.10"),
    sourceIbanHash = "source-hash",
    destinationIbanHash = "destination-hash"
  )
  private val success = RailSuccess(
    lineNumber = 3,
    prepared = prepared,
    label = "Facture juillet",
    warnings = Nil
  )

  "RailRenderer.renderLine" should "rendre le rail droit avec fold" in:
    RailRenderer.renderLine(Right(success)) shouldBe
      "Transaction OK : 100.00 MAD"

  it should "rendre une erreur de parsing précise avec fold" in:
    RailRenderer.renderLine(
      Left(ParsingError(3, ParsingFailure.InvalidAmount))
    ) shouldBe "REJET : PARSE_AMOUNT - montant illisible"

  it should "rendre validation, métier et système sans stack trace" in:
    val rendered = List(
      RailRenderer.renderLine(
        Left(
          TransactionValidationError(
            3,
            Some(7),
            List("MONTANT_NON_POSITIF", "IBAN_SOURCE_INVALIDE")
          )
        )
      ),
      RailRenderer.renderLine(
        Left(Iso20022Rejection(Iso20022Code.AM05, 7))
      ),
      RailRenderer.renderLine(
        Left(FileReadFailure("input.csv", "permission refusée"))
      ),
      RailRenderer.renderLine(
        Left(
          TechnicalError(
            "hash-iban",
            "ProviderException",
            "hachage impossible"
          )
        )
      )
    )

    rendered shouldBe List(
      "REJET : VALIDATION_TRANSACTION - MONTANT_NON_POSITIF+IBAN_SOURCE_INVALIDE",
      "REJET : AM05 - transaction 7 — opération dupliquée",
      "REJET : TECH_READ - lecture input.csv : permission refusée",
      "REJET : TECH_HASH_IBAN - ProviderException : hachage impossible"
    )
    rendered.mkString should not include "\n\tat "
    rendered.mkString should not include "java.security."

  it should "rendre la récupération sans donnée bancaire sensible" in:
    val recovered = success.copy(
      label = "NON RENSEIGNE",
      warnings = List(LightWarning.MissingLabel("NON RENSEIGNE"))
    )
    val rendered = RailRenderer.renderSuccess(recovered)

    rendered shouldBe List(
      "Transaction OK : 100.00 MAD",
      "AVERTISSEMENT : LABEL_MANQUANT -> NON RENSEIGNE"
    ).mkString("\n")
    rendered should not include "MA64"
    rendered should not include "source-hash"

  it should "sanitiser les anciennes erreurs qui transportent une valeur brute" in:
    val secretIban = "MA64SECRET000000000000000"
    val secretCsv =
      s"9,ATH,CIH,$secretIban,MA64CIH00000000000000000,100,VIR,MAD"
    val rendered = List(
      RailRenderer.renderError(InvalidIban(secretIban)),
      RailRenderer.renderError(MalformedCsv(secretCsv)),
      RailRenderer.renderError(
        FieldValidationError("sourceIban", s"valeur interdite : $secretIban")
      )
    )

    rendered shouldBe List(
      "REJET : VALIDATION_IBAN - IBAN invalide",
      "REJET : VALIDATION_CSV - format CSV invalide",
      "REJET : VALIDATION_FIELD - champ sourceIban invalide"
    )
    rendered.mkString should not include secretIban
    rendered.mkString should not include secretCsv

  it should "stabiliser les codes techniques quelle que soit la locale JVM" in:
    val previousLocale = Locale.getDefault

    try
      Locale.setDefault(Locale.forLanguageTag("tr"))
      RailRenderer.renderError(
        TechnicalError("hash-iban", "ProviderException", "indisponible")
      ) shouldBe
        "REJET : TECH_HASH_IBAN - ProviderException : indisponible"
    finally Locale.setDefault(previousLocale)
