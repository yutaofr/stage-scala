package clearing.v21

import clearing.model.ClearingError

object RailRecovery:
  private val DefaultLabel = "NON RENSEIGNE"

  def recover(
    label: Option[String]
  )(
    rail: RailTransaction
  ): Either[ClearingError, RailTransaction] =
    label.map(_.trim).filter(_.nonEmpty) match
      case Some(value) => Right(rail.copy(label = Some(value)))
      case None =>
        Right(
          rail.copy(
            label = Some(DefaultLabel),
            warnings = rail.warnings :+
              LightWarning.MissingLabel(DefaultLabel)
          )
        )

  def recoverOnRight(
    result: Either[ClearingError, RailTransaction],
    label: Option[String]
  ): Either[ClearingError, RailTransaction] =
    result.flatMap(recover(label))
