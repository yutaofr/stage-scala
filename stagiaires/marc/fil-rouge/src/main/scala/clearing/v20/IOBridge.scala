package clearing.v20

import scala.io.Source
import scala.util.{Failure, Success, Using}

enum FileReadResult:
  case Success(path: String, content: String)
  case Failure(path: String, reason: String)

object IOBridge:
  def read(path: String): FileReadResult =
    Using(Source.fromFile(path))(_.mkString) match
      case Success(content) => FileReadResult.Success(path, content)
      case Failure(_) =>
        FileReadResult.Failure(path, "fichier introuvable ou illisible")
