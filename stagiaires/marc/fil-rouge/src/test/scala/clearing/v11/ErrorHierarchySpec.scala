package clearing.v11

import clearing.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ErrorHierarchySpec extends AnyFlatSpec with Matchers:
  "ClearingError" should "organiser les erreurs par niveau et par nature" in:
    CorruptedFile("entête absente") shouldBe a[HighLevelError]
    EmptyFile shouldBe a[HighLevelError]
    FileReadFailure("input.csv", "permission refusée") shouldBe a[SystemError]
    InvalidIban("FR001") shouldBe a[ValidationError]
    UnknownBank("ZZZ") shouldBe a[ValidationError]
    DuplicateTransaction shouldBe a[BusinessError]

  "Iso20022Code" should "modéliser les dix codes fréquents du cours" in:
    Iso20022Code.values.toList shouldBe List(
      Iso20022Code.AC01,
      Iso20022Code.AC04,
      Iso20022Code.AC06,
      Iso20022Code.AG01,
      Iso20022Code.AM04,
      Iso20022Code.AM05,
      Iso20022Code.FF01,
      Iso20022Code.MD01,
      Iso20022Code.RC01,
      Iso20022Code.RR01
    )
    Iso20022Code.AC01.description should include("compte")

  "ErrorAnalysis" should "extraire les erreurs de ligne et compter les blocages" in:
    val errors: List[ClearingError] = List(
      CorruptedFile("checksum invalide"),
      InvalidAmount(BigDecimal("-1")),
      Iso20022Rejection(Iso20022Code.AC04, transactionId = 7),
      FileReadFailure("input.csv", "illisible")
    )

    ErrorAnalysis.lineErrors(errors) shouldBe List(
      InvalidAmount(BigDecimal("-1")),
      Iso20022Rejection(Iso20022Code.AC04, transactionId = 7)
    )
    ErrorAnalysis.highLevelCount(errors) shouldBe 1

  "DetailedErrorReporter" should "rendre chaque branche de la hiérarchie" in:
    DetailedErrorReporter.detailedReport(CorruptedFile("format inconnu")) shouldBe
      "Erreur bloquante — fichier corrompu : format inconnu"
    DetailedErrorReporter.detailedReport(EmptyFile) shouldBe
      "Erreur bloquante — fichier vide"
    DetailedErrorReporter.detailedReport(
      FileReadFailure("input.csv", "permission refusée")
    ) shouldBe "Erreur système — lecture input.csv : permission refusée"
    DetailedErrorReporter.detailedReport(InvalidIban("FR001")) shouldBe
      "Erreur de validation [iban] : FR001 est invalide"
    DetailedErrorReporter.detailedReport(
      FieldValidationError("amount", "doit être renseigné")
    ) shouldBe "Erreur de validation [amount] : doit être renseigné"
    DetailedErrorReporter.detailedReport(DuplicateTransaction) shouldBe
      "Erreur métier [DUPL] : transaction dupliquée"
    DetailedErrorReporter.detailedReport(
      Iso20022Rejection(Iso20022Code.AC06, transactionId = 12)
    ) shouldBe "Erreur métier [AC06] : transaction 12 — compte bloqué"
    DetailedErrorReporter.detailedReport(
      ParsingError(8, ParsingFailure.InvalidAmount)
    ) shouldBe "Erreur de parsing [ligne 8] : montant illisible"
    DetailedErrorReporter.detailedReport(
      TechnicalError("hash-iban", "ProviderException", "hachage impossible")
    ) shouldBe
      "Erreur système — hash-iban [ProviderException] : hachage impossible"
