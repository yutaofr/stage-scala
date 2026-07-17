package clearing.v22

import scala.io.Source
import scala.util.Using

object V22IO:
  def read(path: String): Either[V22Error, String] =
    Using(Source.fromFile(path, "UTF-8"))(_.mkString).toEither.left.map:
      error =>
        V22TechnicalError(
          lineNumber = 0,
          transactionId = None,
          operation = "read-file",
          causeType = error.getClass.getSimpleName,
          detail = "fichier introuvable ou illisible"
        )
