package clearing.v31

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CassandraRepositoryContractSpec extends AnyFlatSpec with Matchers:
  "CassandraSettings" should "fournir le laboratoire local par défaut" in:
    CassandraSettings.fromEnvironment(Map.empty) shouldBe CassandraSettings(
      host = "localhost",
      port = 9042,
      datacenter = "datacenter1",
      keyspace = "clearing"
    )

  it should "lire toutes les variables Cassandra" in:
    CassandraSettings.fromEnvironment(
      Map(
        "CASSANDRA_HOST" -> "cassandra",
        "CASSANDRA_PORT" -> "9142",
        "CASSANDRA_DATACENTER" -> "dc-stage",
        "CASSANDRA_KEYSPACE" -> "clearing_test"
      )
    ) shouldBe CassandraSettings(
      "cassandra",
      9142,
      "dc-stage",
      "clearing_test"
    )

  it should "refuser un port invalide" in:
    an[IllegalArgumentException] should be thrownBy
      CassandraSettings.fromEnvironment(Map("CASSANDRA_PORT" -> "abc"))

  "CassandraStatementQueries" should "préparer une requête bornée par usage" in:
    CassandraStatementQueries.all.keySet shouldBe Set(
      "selectStates",
      "markState",
      "saveHistory",
      "saveMovement",
      "savePosition",
      "savePair",
      "selectMovements",
      "selectPositions",
      "selectHistoryBucket",
      "selectPairs"
    )

    CassandraStatementQueries.all.values.foreach: query =>
      query.toUpperCase should not include "ALLOW FILTERING"
      query.toUpperCase should not include "SELECT *"

    CassandraStatementQueries.selectStates should include("event_key = ?")
    CassandraStatementQueries.selectMovements should include(
      "bank_id = ? AND clearing_date = ?"
    )
    CassandraStatementQueries.selectHistoryBucket should include(
      "clearing_date = ? AND bucket = ?"
    )
    CassandraStatementQueries.selectPairs should include("clearing_date = ?")
