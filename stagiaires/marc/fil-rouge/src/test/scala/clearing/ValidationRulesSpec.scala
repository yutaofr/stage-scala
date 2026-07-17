package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ValidationRulesSpec extends AnyFlatSpec with Matchers:
  private val amounts = List(
    BigDecimal("100"),
    BigDecimal("101"),
    BigDecimal("300"),
    BigDecimal("600"),
    BigDecimal("1500")
  )

  "filtrerTransactions" should "accepter un critère reçu en paramètre" in:
    val pair = (amount: BigDecimal) => amount.remainder(BigDecimal(2)) == 0
    ValidationRules.filtrerTransactions(amounts, pair) shouldBe
      List(BigDecimal("100"), BigDecimal("300"), BigDecimal("600"), BigDecimal("1500"))

  it should "composer des critères spécialisés" in:
    val superieurA1000 = (amount: BigDecimal) => amount > 1000
    val entre100Et500 = (amount: BigDecimal) => amount >= 100 && amount <= 500
    ValidationRules.filtrerTransactions(amounts, superieurA1000) shouldBe
      List(BigDecimal("1500"))
    ValidationRules.filtrerTransactions(amounts, entre100Et500) shouldBe
      List(BigDecimal("100"), BigDecimal("101"), BigDecimal("300"))

  "appliquerFrais" should "ajouter un pour cent pour ATH" in:
    ValidationRules.appliquerFrais("ATH")(BigDecimal("100")) shouldBe
      BigDecimal("101.00")

  it should "ajouter deux pour cent pour une autre banque" in:
    ValidationRules.appliquerFrais("CIH")(BigDecimal("100")) shouldBe
      BigDecimal("102.00")

  it should "permettre une configuration partielle réutilisable" in:
    ValidationRules.fraisATH(BigDecimal("200")) shouldBe BigDecimal("202.00")
    ValidationRules.fraisGenerique(BigDecimal("200")) shouldBe BigDecimal("204.00")

  "validateAll" should "exiger que toutes les règles soient vraies" in:
    ValidationRules.validateAll(BigDecimal("10"), ValidationRules.defaultRules) shouldBe true
    ValidationRules.validateAll(BigDecimal("0"), ValidationRules.defaultRules) shouldBe false
    ValidationRules.validateAll(BigDecimal("100000"), ValidationRules.defaultRules) shouldBe false

  it should "être vrai pour une liste de règles vide" in:
    ValidationRules.validateAll(BigDecimal("10"), Nil) shouldBe true

  "validateAny" should "accepter dès qu'une règle est vraie" in:
    val rules: List[ValidationRules.Rule] = List(_ > 100, _ < 0)
    ValidationRules.validateAny(BigDecimal("150"), rules) shouldBe true
    ValidationRules.validateAny(BigDecimal("50"), rules) shouldBe false

  it should "être faux pour une liste de règles vide" in:
    ValidationRules.validateAny(BigDecimal("10"), Nil) shouldBe false
