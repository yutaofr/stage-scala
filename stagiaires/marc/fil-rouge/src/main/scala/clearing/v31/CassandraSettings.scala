package clearing.v31

import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import java.net.InetSocketAddress

final case class CassandraSettings(
  host: String,
  port: Int,
  datacenter: String,
  keyspace: String
)

object CassandraSettings:
  def fromEnvironment(
    environment: Map[String, String] = sys.env
  ): CassandraSettings =
    val port = environment
      .get("CASSANDRA_PORT")
      .fold(9042): raw =>
        raw.toIntOption
          .filter(value => value > 0 && value <= 65535)
          .getOrElse(
            throw new IllegalArgumentException(
              "CASSANDRA_PORT doit être un port valide"
            )
          )

    CassandraSettings(
      host = environment.getOrElse("CASSANDRA_HOST", "localhost"),
      port = port,
      datacenter = environment.getOrElse(
        "CASSANDRA_DATACENTER",
        "datacenter1"
      ),
      keyspace = environment.getOrElse("CASSANDRA_KEYSPACE", "clearing")
    )

object CassandraSession:
  def open(settings: CassandraSettings): CqlSession =
    CqlSession
      .builder()
      .addContactPoint(new InetSocketAddress(settings.host, settings.port))
      .withLocalDatacenter(settings.datacenter)
      .withKeyspace(CqlIdentifier.fromCql(settings.keyspace))
      .build()
