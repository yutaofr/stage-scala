package clearing.v23

import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

final class FunctorLawSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaCheckPropertyChecks:
  private val increment: Int => Int = _ + 1
  private val render: Int => String = _.toString

  "Functor[List]" should "respecter les lois d'identité et de composition" in:
    import Functor.given

    forAll(Gen.listOf(Gen.choose(-10000, 10000))): values =>
      Functor.transform(values, identity[Int]) shouldBe values
      Functor.transform(values, increment andThen render) shouldBe
        Functor.transform(Functor.transform(values, increment), render)

  "Functor[Option]" should "respecter les lois d'identité et de composition" in:
    import Functor.given

    forAll(Gen.option(Gen.choose(-10000, 10000))): value =>
      Functor.transform(value, identity[Int]) shouldBe value
      Functor.transform(value, increment andThen render) shouldBe
        Functor.transform(Functor.transform(value, increment), render)

  "Functor[Box]" should "respecter les lois d'identité et de composition" in:
    import Box.given

    forAll(Gen.choose(-10000, 10000)): value =>
      val box = Box(value)
      Functor.transform(box, identity[Int]) shouldBe box
      Functor.transform(box, increment andThen render) shouldBe
        Functor.transform(Functor.transform(box, increment), render)
