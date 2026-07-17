package clearing.v30

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ClearingAppV30Spec extends AnyFlatSpec with Matchers:
  "V30Cli" should "router le sous-commande producer" in:
    V30Cli.parse(List("producer", "--count", "50")) shouldBe Right(
      V30Command.Produce(
        ProducerCommand(50, 1500L, 10, "localhost:9092")
      )
    )

  it should "router le sous-commande consumer" in:
    V30Cli.parse(
      List("consumer", "--group-id", "demo", "--max-records", "50")
    ) shouldBe Right(
      V30Command.Consume(
        ConsumerCommand("localhost:9092", "demo", Some(50))
      )
    )

  it should "refuser une commande absente ou inconnue" in:
    V30Cli.parse(Nil) shouldBe Left(V30Cli.Usage)
    V30Cli.parse(List("unknown")) shouldBe Left(V30Cli.Usage)
