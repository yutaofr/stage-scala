package clearing.v23

import clearing.v22.{NumberedLine, V22Profiles}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.compiletime.testing.typeCheckErrors

final class ForComprehensionSpec extends AnyFlatSpec with Matchers:
  private val valid = NumberedLine(
    1,
    "1,ATH,CIH,MA64ATH00000000000000000,MA64CIH00000000000000000,100,VIR,MAD"
  )
  private val invalidBusiness = NumberedLine(
    2,
    "2,ATH,ATH,MA64ATH00000000000000000,MA64ATH00000000000000000,100,VIR,MAD"
  )

  "ForEquivalence" should "produire le même rail droit avec for et flatMap" in:
    ForEquivalence.withFor(
      V22Profiles.clearingMAD,
      Set.empty
    )(valid) shouldBe ForEquivalence.withFlatMap(
      V22Profiles.clearingMAD,
      Set.empty
    )(valid)

  it should "produire les mêmes rails gauches de parsing et validation" in:
    val lines = List(NumberedLine(7, "ligne,invalide"), invalidBusiness)

    lines.foreach: line =>
      ForEquivalence.withFor(
        V22Profiles.clearingMAD,
        Set.empty
      )(line) shouldBe ForEquivalence.withFlatMap(
        V22Profiles.clearingMAD,
        Set.empty
      )(line)

  "LoggedRailwayLab" should "rendre le for identique au chaînage explicite" in:
    LoggedRailwayLab.pipeline(
      V22Profiles.clearingMAD,
      Set.empty
    )(valid) shouldBe ForEquivalence.loggerWithFlatMap(
      V22Profiles.clearingMAD,
      Set.empty
    )(valid)

  "for-comprehension" should "refuser le mixage direct de Either et Option" in:
    typeCheckErrors("""
      val result =
        for
          x <- (Right(10): Either[String, Int])
          y <- Option(5)
        yield x + y
    """) should not be empty
