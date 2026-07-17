package clearing.v31

import clearing.v30.{EventCodec, KafkaSettings}
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class QualificationProducerSpec extends AnyFlatSpec with Matchers:
  "V31QualificationScenario" should "construire 485 uniques, 5 invalides et 10 replays" in:
    val records = V31QualificationScenario.records(seed = 1600L)
    val (invalid, valid) = records.partition(record =>
      EventCodec.decodeInput(record.value).isLeft
    )

    records should have size 500
    invalid should have size 5
    valid should have size 495
    valid.map(_.value).distinct should have size 485
    valid.groupBy(_.value).values.count(_.size == 2) shouldBe 10
    valid.groupBy(_.value).values.forall(_.size <= 2) shouldBe true

  "V31QualificationProducer" should "attendre les callbacks des 500 records" in:
    val kafka = new MockProducer[String, String](
      true,
      null,
      new StringSerializer,
      new StringSerializer
    )
    val producer = V31QualificationProducer(kafka)

    producer.send(V31QualificationScenario.records(1600L)) shouldBe
      clearing.v30.ProducerReport(500, 500, 0)
    kafka.history() should have size 500
    kafka.history().forEach(record => record.topic() shouldBe KafkaSettings.InputTopic)

  "QualificationCli" should "accepter seed et bootstrap" in:
    V31Cli.parse(
      List(
        "qualify",
        "--seed",
        "1700",
        "--bootstrap-servers",
        "kafka:29092"
      )
    ) shouldBe Right(
      V31Command.Qualify(QualificationCommand(1700L, "kafka:29092"))
    )
