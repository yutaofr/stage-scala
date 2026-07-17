package clearing.v30

import java.nio.file.{Files, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class KafkaComposeContractSpec extends AnyFlatSpec with Matchers:
  private val composePath = Paths.get("docker/docker-compose-kafka.yml")
  private val initPath = Paths.get("docker/create-topics.sh")

  private def compose: String = Files.readString(composePath)
  private def initScript: String = Files.readString(initPath)

  "Le laboratoire Kafka S15" should "utiliser KRaft sans ZooKeeper" in:
    compose should include("apache/kafka:4.3.0")
    compose should include("KAFKA_PROCESS_ROLES: broker,controller")
    compose.toLowerCase should not include "zookeeper"

  it should "séparer le listener hôte du listener conteneur" in:
    compose should include("EXTERNAL://localhost:9092")
    compose should include("INTERNAL://kafka:29092")
    compose should include("CONTROLLER://:9093")
    compose should include("1@kafka:9093")
    compose should include("kafka:29092")

  it should "contrôler la santé réelle du broker" in:
    compose should include("kafka-broker-api-versions.sh")
    compose should include("condition: service_healthy")

  it should "créer les trois topics à trois partitions" in:
    val topics = List(
      "clearing-input",
      "clearing-output",
      "clearing-dlq"
    )

    topics.foreach: topic =>
      initScript should include(topic)
    initScript should include("--partitions 3")
    initScript should include("--replication-factor 1")
    initScript should include("--if-not-exists")
