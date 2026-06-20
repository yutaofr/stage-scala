package distributed.http

import clearing.contract.TransactionSubmittedV1
import clearing.model.BankCode
import distributed.kafka.{KafkaClients, KafkaSettings, TransactionProducer}
import sttp.model.StatusCode
import sttp.tapir.PublicEndpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.http.Server
import zio.json.*

final case class ApiError(code: String, message: String) derives JsonCodec
final case class Accepted(transactionId: String, status: String) derives JsonCodec
final case class Position(bankId: String, date: String, amount: BigDecimal) derives JsonCodec

object ClearingApi:
  private val knownBanks = Set("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe)

  val healthEndpoint: PublicEndpoint[Unit, Unit, String, Any] =
    endpoint.get
      .in("health")
      .out(stringBody)

  val ingestEndpoint
    : PublicEndpoint[
        TransactionSubmittedV1,
        (StatusCode, ApiError),
        (StatusCode, Accepted),
        Any
      ] =
    endpoint.post
      .in("api" / "v1" / "transactions")
      .in(jsonBody[TransactionSubmittedV1])
      .errorOut(statusCode.and(jsonBody[ApiError]))
      .out(statusCode)
      .out(jsonBody[Accepted])

  val positionEndpoint
    : PublicEndpoint[(String, String), ApiError, Position, Any] =
    endpoint.get
      .in("api" / "v1" / "banks" / path[String]("bankId") / "positions")
      .in(query[String]("date"))
      .errorOut(jsonBody[ApiError])
      .out(jsonBody[Position])

  private val healthServer: ZServerEndpoint[Any, Any] =
    healthEndpoint.zServerLogic[Any](_ => ZIO.succeed("UP"))

  private val ingestServer: ZServerEndpoint[Any, Any] =
    ingestEndpoint.zServerLogic[Any] { event =>
      for
        tx <- ZIO
          .fromEither(event.toDomain(knownBanks))
          .mapError(error => StatusCode.BadRequest -> ApiError(error.code, error.message))
        settings = KafkaSettings.fromEnvironment()
        _ <- ZIO
          .acquireReleaseWith(ZIO.attempt(KafkaClients.producer(settings)))(
            producer => ZIO.succeed(producer.close())
          ) { producer =>
            ZIO.attemptBlocking(
              TransactionProducer
                .send(producer, settings, tx)
                .get(5, java.util.concurrent.TimeUnit.SECONDS)
            )
          }
          .mapError(error =>
            StatusCode.ServiceUnavailable ->
              ApiError("KAFKA_UNAVAILABLE", Option(error.getMessage).getOrElse(error.toString))
          )
      yield StatusCode.Accepted -> Accepted(tx.id.value, "accepted")
    }

  private val positionServer: ZServerEndpoint[Any, Any] =
    positionEndpoint.zServerLogic[Any] { case (bankId, date) =>
      BankCode.from(bankId) match
        case Left(error) => ZIO.fail(ApiError("INVALID_BANK", error))
        case Right(_) =>
          // TODO S19 : déléguer la lecture au repository Cassandra.
          ZIO.succeed(Position(bankId.toUpperCase, date, BigDecimal(0)))
    }

  val applicationEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(healthServer, ingestServer, positionServer)

  val documentationEndpoints =
    SwaggerInterpreter()
      .fromServerEndpoints[Task](applicationEndpoints, "Clearing API", "4.0.0-rc1")

object ClearingServer extends ZIOAppDefault:
  private val port =
    sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)

  private val routes =
    ZioHttpInterpreter().toHttp(
      ClearingApi.applicationEndpoints ++ ClearingApi.documentationEndpoints
    )

  def run =
    Server
      .serve(routes)
      .provide(Server.defaultWithPort(port))
