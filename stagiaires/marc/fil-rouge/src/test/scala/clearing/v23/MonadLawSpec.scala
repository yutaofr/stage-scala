package clearing.v23

import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

final class MonadLawSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaCheckPropertyChecks:
  private val monad = summon[Monad[MonadicLogger]]

  private val logGen: Gen[List[String]] =
    Gen.listOf(Gen.choose(0, 9).map(value => s"trace-$value"))

  private val loggerGen: Gen[MonadicLogger[Int]] =
    for
      value <- Gen.choose(-10000, 10000)
      logs <- logGen
    yield MonadicLogger(value, logs)

  private val double: Int => MonadicLogger[Int] = value =>
    MonadicLogger(value * 2, List("double"))

  private val render: Int => MonadicLogger[String] = value =>
    MonadicLogger(value.toString, List("render"))

  "Monad[MonadicLogger]" should "respecter l'identité gauche" in:
    forAll(Gen.choose(-10000, 10000)): value =>
      monad.flatMap(monad.pure(value))(double) shouldBe double(value)

  it should "respecter l'identité droite" in:
    forAll(loggerGen): logger =>
      monad.flatMap(logger)(monad.pure) shouldBe logger

  it should "respecter l'associativité" in:
    forAll(loggerGen): logger =>
      monad.flatMap(monad.flatMap(logger)(double))(render) shouldBe
        monad.flatMap(logger)(value => monad.flatMap(double(value))(render))
