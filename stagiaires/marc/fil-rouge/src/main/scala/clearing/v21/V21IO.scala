package clearing.v21

import clearing.model.TechnicalError
import java.nio.charset.StandardCharsets
import scala.io.Source
import scala.util.Using

object V21IO:
  def read(path: String): Either[TechnicalError, String] =
    Using(
      Source.fromFile(path, StandardCharsets.UTF_8.name())
    )(_.mkString).toEither.left.map:
      TechnicalError.fromThrowable(
        operation = "read-file",
        detail = "fichier introuvable ou illisible"
      )
