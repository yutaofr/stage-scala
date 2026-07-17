package clearing.v23

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class FunctorSpec extends AnyFlatSpec with Matchers:
  private val increment: Int => Int = _ + 1
  private val render: Int => String = _.toString

  "Functor laws" should "préserver l'identité et la composition de List" in:
    import Functor.given

    val values = List(1, 2, 3)
    Functor.transform(values, identity[Int]) shouldBe values
    Functor.transform(
      values,
      increment andThen render
    ) shouldBe Functor.transform(
      Functor.transform(values, increment),
      render
    )

  it should "préserver l'identité et la composition de Option" in:
    import Functor.given

    val value: Option[Int] = Option(42)
    Functor.transform(value, identity[Int]) shouldBe value
    Functor.transform(
      value,
      increment andThen render
    ) shouldBe Functor.transform(
      Functor.transform(value, increment),
      render
    )

  "Functor.transform" should "transformer List et Option avec la même fonction" in:
    import Functor.given

    Functor.transform(List(1, 2), increment) shouldBe List(2, 3)
    Functor.transform(Option(1), increment) shouldBe Option(2)

  "Box" should "recevoir map depuis son instance Functor" in:
    import Box.given

    Box(41).map(_ + 1) shouldBe Box(42)
