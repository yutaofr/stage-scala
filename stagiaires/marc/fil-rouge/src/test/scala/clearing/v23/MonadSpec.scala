package clearing.v23

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class MonadSpec extends AnyFlatSpec with Matchers:
  "MonadicLogger.map" should "transformer la valeur et conserver le journal" in:
    MonadicLogger(10, List("départ")).map(_ + 5) shouldBe
      MonadicLogger(15, List("départ"))

  "MonadicLogger.flatMap" should "accumuler le journal dans l'ordre chronologique" in:
    val result = MonadicLogger(10, List("départ"))
      .flatMap(value => MonadicLogger(value * 2, List("double")))
      .flatMap(value => MonadicLogger(value - 1, List("moins un")))

    result shouldBe
      MonadicLogger(19, List("départ", "double", "moins un"))

  "MonadicLogger.pure" should "placer une valeur dans un journal neutre" in:
    MonadicLogger.pure(42) shouldBe MonadicLogger(42, Nil)

  "Monad.chain" should "orchestrer trois calculs sans connaître le conteneur" in:
    import MonadicLogger.given

    val result = Monad.chain(
      MonadicLogger(2, List("lecture")),
      value => MonadicLogger(value * 3, List("multiplication")),
      value => MonadicLogger(value + 4, List("addition"))
    )

    result shouldBe
      MonadicLogger(10, List("lecture", "multiplication", "addition"))

  it should "dériver map de flatMap et pure" in:
    import MonadicLogger.given

    val monad = summon[Monad[MonadicLogger]]
    monad.map(MonadicLogger(4, List("source")))(_ * 2) shouldBe
      MonadicLogger(8, List("source"))
