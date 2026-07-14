package clearing.v12

import clearing.model.Bank

case class BankPair(sender: Bank, receiver: Bank)

case class BilateralSettlement(
  debtor: Bank,
  creditor: Bank,
  amount: BigDecimal
)

case class BankPosition(bank: Bank, balance: BigDecimal)

case class PartitionSummary(
  validatedCount: Int,
  otherCount: Int,
  validatedAmount: BigDecimal,
  otherAmount: BigDecimal
)

case class BatchNetting(
  number: Int,
  transactionCount: Int,
  positions: Map[String, BigDecimal]
)

case class FraudWindowAlert(ids: List[Int], totalAmount: BigDecimal)

case class BankActivity(bank: Bank, participationCount: Int)

sealed trait EngineLog:
  def message: String

case class FinancialLog(message: String) extends EngineLog

case class TechnicalLog(message: String) extends EngineLog

case class LogPartition(
  financial: List[FinancialLog],
  technical: List[TechnicalLog]
)

object BankDirectory:
  val all: Vector[Bank] = Vector(
    Bank("ATH", "Attijariwafa Bank"),
    Bank("CIH", "CIH Bank"),
    Bank("BOA", "Bank of Africa"),
    Bank("BMCE", "BMCE Capital"),
    Bank("SGMB", "Société Générale Maroc")
  )

  private val byCode: Map[String, Bank] =
    all.map(bank => bank.code -> bank).toMap

  def fromCode(code: String): Bank =
    val normalized = code.trim.toUpperCase
    byCode.getOrElse(normalized, Bank(normalized, "Banque inconnue"))
