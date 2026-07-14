package clearing.v30

import java.util.concurrent.atomic.AtomicInteger
import org.apache.kafka.clients.producer.{MockProducer, ProducerConfig}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.serialization.StringSerializer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TransactionProducerSpec extends AnyFlatSpec with Matchers:
  "ProducerCli" should "fournir le scénario J2 par défaut" in:
    ProducerCli.parse(Nil) shouldBe Right(
      ProducerCommand(
        count = 50,
        seed = 1500L,
        rate = 10,
        bootstrapServers = "localhost:9092"
      )
    )

  it should "contrôler les paramètres explicites" in:
    ProducerCli.parse(
      List(
        "--count",
        "1000",
        "--seed",
        "42",
        "--rate",
        "100",
        "--reject-every",
        "10",
        "--bootstrap-servers",
        "kafka:29092"
      )
    ) shouldBe Right(
      ProducerCommand(1000, 42L, 100, "kafka:29092", Some(10))
    )

    ProducerCli.parse(List("--rate", "0")) shouldBe Left(ProducerCli.Usage)
    ProducerCli.parse(List("--unknown")) shouldBe Left(ProducerCli.Usage)

  "KafkaProducerSettings" should "activer les garanties du producteur" in:
    val properties = KafkaProducerSettings.properties("localhost:9092")

    properties.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) shouldBe
      "localhost:9092"
    properties.getProperty(ProducerConfig.ACKS_CONFIG) shouldBe "all"
    properties.getProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG) shouldBe
      "true"
    properties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG) shouldBe
      classOf[StringSerializer].getName
    properties.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG) shouldBe
      classOf[StringSerializer].getName

  "TransactionGenerator" should "produire un batch déterministe et indexé" in:
    val first = TransactionGenerator.generate(count = 50, seed = 1500L)
    val second = TransactionGenerator.generate(count = 50, seed = 1500L)

    first shouldBe second
    first should have size 50
    first.map(_.id) shouldBe (1 to 50).toList
    all(first.map(_.amount)) should be > BigDecimal(0)

  it should "injecter des rejets déterministes pour le gate J5" in:
    val mixed = TransactionGenerator.generateMixed(
      count = 1000,
      seed = 1500L,
      rejectEvery = 10
    )

    mixed.count(event => event.sender == event.receiver) shouldBe 100

  "TransactionProducer" should "envoyer clé, header et JSON puis attendre tous les callbacks" in:
    val kafka = new MockProducer[String, String](
      true,
      null,
      new StringSerializer,
      new StringSerializer
    )
    val waits = new AtomicInteger(0)
    val producer = TransactionProducer(
      kafka,
      Pacer(() => waits.incrementAndGet())
    )
    val events = TransactionGenerator.generate(count = 3, seed = 42L)

    producer.send(events) shouldBe ProducerReport(
      attempted = 3,
      acknowledged = 3,
      failed = 0
    )
    val records = kafka.history()
    records.size shouldBe 3
    records.forEach: record =>
      val event = EventCodec.decodeInput(record.value()).toOption.get
      record.topic() shouldBe KafkaSettings.InputTopic
      record.key() shouldBe event.sender
      new String(
        record.headers().lastHeader("transaction-id").value()
      ) shouldBe event.id.toString
    waits.get() shouldBe 2

    producer.close()
    kafka.closed() shouldBe true

  it should "compter un callback en échec sans perdre les suivants" in:
    val kafka = new MockProducer[String, String](
      false,
      null,
      new StringSerializer,
      new StringSerializer
    )
    val producer = TransactionProducer(
      kafka,
      Pacer(() => kafka.errorNext(new KafkaException("indisponible")))
    )

    producer.send(TransactionGenerator.generate(2, 7L)) shouldBe
      ProducerReport(attempted = 2, acknowledged = 1, failed = 1)
