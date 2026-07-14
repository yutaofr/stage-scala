package clearing.v21

import clearing.model.Transaction

case class NumberedLine(lineNumber: Int, value: String)

enum LightWarning:
  case MissingLabel(defaultLabel: String)

case class RailTransaction(
  lineNumber: Int,
  transaction: Transaction,
  warnings: List[LightWarning],
  label: Option[String] = None
)
