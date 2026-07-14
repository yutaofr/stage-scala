package clearing.v31

import java.nio.file.{Files, Path, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CassandraComposeContractSpec extends AnyFlatSpec with Matchers:
  private val composePath = Paths.get("docker/docker-compose-v31.yml")
  private val schemaPath = Paths.get("docker/init-cassandra-v31.cql")

  private def requireFile(path: Path): String =
    withClue(s"fichier attendu: $path"):
      Files.exists(path) shouldBe true
    Files.readString(path)

  private def compose: String = requireFile(composePath)
  private def schema: String = requireFile(schemaPath)

  "Le laboratoire Cassandra S16" should "fixer les versions et attendre les services sains" in:
    compose should include("apache/kafka:4.3.0")
    compose should include("cassandra:4.1.11")
    compose should include("cqlsh")
    compose should include("condition: service_healthy")
    compose should include("./init-cassandra-v31.cql:/init.cql:ro")

  it should "conserver le double listener Kafka de v3.0" in:
    compose should include("EXTERNAL://localhost:9092")
    compose should include("INTERNAL://kafka:29092")
    compose should include("kafka-broker-api-versions.sh")

  it should "utiliser un keyspace orienté datacenter" in:
    schema should include("NetworkTopologyStrategy")
    schema should include("'datacenter1': 1")
    schema should not include "SimpleStrategy"

  it should "créer une table par requête sans scan ni compteur" in:
    val tables = List(
      "processing_state",
      "clearing_history_by_day",
      "transactions_by_bank_day",
      "bank_positions",
      "pair_activity_by_day"
    )
    tables.foreach: table =>
      schema should include(s"CREATE TABLE IF NOT EXISTS $table")

    schema should include(
      "PRIMARY KEY ((event_key), payload_fingerprint)"
    )
    schema.toUpperCase should not include "ALLOW FILTERING"
    raw"(?i)\bcounter\b".r.findFirstIn(schema) shouldBe None
