package clearing.v23

import clearing.v22.{NumberedLine, V22Profiles}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class LoggedRailwayLabSpec extends AnyFlatSpec with Matchers:
  private val validLine = NumberedLine(
    1,
    "1,ATH,CIH,MA64ATH00000000000000000,MA64CIH00000000000000000,100,VIR,MAD"
  )

  "LoggedRailwayLab.pipeline" should "accumuler parse, validation et sauvegarde sur le cas nominal" in:
    val result = LoggedRailwayLab.pipeline(
      V22Profiles.clearingMAD,
      Set.empty
    )(validLine)

    result.value.map(_.transaction.id) shouldBe Right(1)
    result.logs shouldBe List(
      "Parsing OK : 1",
      "Validation OK : 1",
      "Sauvegarde observée : 1"
    )

  it should "montrer que Writer continue à journaliser après un Left" in:
    val result = LoggedRailwayLab.pipeline(
      V22Profiles.clearingMAD,
      Set.empty
    )(NumberedLine(7, "ligne,invalide"))

    result.value.isLeft shouldBe true
    result.logs shouldBe List(
      "Parsing rejeté : ligne 7",
      "Validation ignorée : rail gauche",
      "Sauvegarde ignorée : rail gauche"
    )
    result.logs.mkString should not include "ligne,invalide"
