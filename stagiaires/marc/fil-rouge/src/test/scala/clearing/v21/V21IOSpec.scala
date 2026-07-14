package clearing.v21

import clearing.model.TechnicalError
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class V21IOSpec extends AnyFlatSpec with Matchers with OptionValues:
  "V21IO.read" should "lire un fichier réel dans un Either" in:
    val path = Files.createTempFile("marc-s10-", ".csv")
    Files.writeString(path, "ligne-1\nligne-2", StandardCharsets.UTF_8)

    try V21IO.read(path.toString) shouldBe Right("ligne-1\nligne-2")
    finally Files.deleteIfExists(path)

  it should "transformer un chemin absent en erreur technique stable" in:
    val path = Path.of("target", "s10-fichier-absent.csv")

    val result = V21IO.read(path.toString)

    result.isLeft shouldBe true
    val error = result.swap.toOption.value
    error.operation shouldBe "read-file"
    error.detail shouldBe "fichier introuvable ou illisible"
    error.causeType should not be empty
