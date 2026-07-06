package distributed.http

import clearing.contract.ContractCodec
import clearing.model.BankCode
import distributed.kafka.{KafkaClients, KafkaSettings, TransactionProducer}
import io.circe.*
import io.circe.syntax.*

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

final case class ApiError(code: String, message: String) derives Encoder, Decoder
final case class Accepted(transactionId: String, status: String) derives Encoder, Decoder
final case class Position(bankId: String, date: String, amount: BigDecimal) derives Encoder, Decoder

object ClearingApi:
  private val knownBanks = Set("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe)

  private def respond(exchange: HttpExchange, status: Int, body: String): Unit =
    val bytes = body.getBytes(StandardCharsets.UTF_8)
    exchange.getResponseHeaders.set("Content-Type", "application/json")
    exchange.sendResponseHeaders(status, bytes.length)
    val output = exchange.getResponseBody
    try output.write(bytes)
    finally output.close()

  final class HealthHandler extends HttpHandler:
    def handle(exchange: HttpExchange): Unit =
      if exchange.getRequestMethod == "GET" then
        respond(exchange, 200, """{"status":"UP"}""")
      else
        respond(exchange, 405, ApiError("METHOD_NOT_ALLOWED", "GET only").asJson.noSpaces)

  final class IngestionHandler(settings: KafkaSettings) extends HttpHandler:
    def handle(exchange: HttpExchange): Unit =
      if exchange.getRequestMethod != "POST" then
        respond(exchange, 405, ApiError("METHOD_NOT_ALLOWED", "POST only").asJson.noSpaces)
      else
        val payload = String(
          exchange.getRequestBody.readAllBytes(),
          StandardCharsets.UTF_8
        )
        val result =
          for
            event <- ContractCodec.decode(payload)
            tx <- event.toDomain(knownBanks).left.map(_.message)
          yield tx

        result match
          case Left(error) =>
            respond(
              exchange,
              400,
              ApiError("INVALID_TRANSACTION", error).asJson.noSpaces
            )
          case Right(tx) =>
            val producer = KafkaClients.producer(settings)
            try
              TransactionProducer.send(producer, settings, tx).get(5, TimeUnit.SECONDS)
              respond(
                exchange,
                202,
                Accepted(tx.id.value, "accepted").asJson.noSpaces
              )
            catch
              case error: Throwable =>
                respond(
                  exchange,
                  503,
                  ApiError("KAFKA_UNAVAILABLE", Option(error.getMessage).getOrElse(error.toString)).asJson.noSpaces
                )
            finally producer.close()

  final class PositionHandler extends HttpHandler:
    def handle(exchange: HttpExchange): Unit =
      if exchange.getRequestMethod != "GET" then
        respond(exchange, 405, ApiError("METHOD_NOT_ALLOWED", "GET only").asJson.noSpaces)
      else
        val path = exchange.getRequestURI.getPath
        val segments = path.split("/").filter(_.nonEmpty)
        // Expected: api/v1/banks/{bankId}/positions
        if segments.length >= 5 && segments(3) != "" then
          val bankId = segments(3)
          val query = Option(exchange.getRequestURI.getQuery).getOrElse("")
          val date = query.split("&").flatMap { param =>
            val parts = param.split("=", 2)
            if parts.length == 2 && parts(0) == "date" then Some(parts(1))
            else None
          }.headOption.getOrElse("")

          BankCode.from(bankId) match
            case Left(error) =>
              respond(exchange, 400, ApiError("INVALID_BANK", error).asJson.noSpaces)
            case Right(_) =>
              // TODO S19 : déléguer la lecture au repository Cassandra.
              respond(
                exchange,
                200,
                Position(bankId.toUpperCase, date, BigDecimal(0)).asJson.noSpaces
              )
        else
          respond(exchange, 400, ApiError("INVALID_PATH", "expected /api/v1/banks/{bankId}/positions").asJson.noSpaces)

@main def runClearingServer(): Unit =
  val settings = KafkaSettings.fromEnvironment()
  val port = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)
  val server = HttpServer.create(new InetSocketAddress(port), 0)
  val executor = Executors.newCachedThreadPool()

  server.createContext("/health", new ClearingApi.HealthHandler)
  server.createContext("/api/v1/transactions", new ClearingApi.IngestionHandler(settings))
  server.createContext("/api/v1/banks", new ClearingApi.PositionHandler)
  server.setExecutor(executor)
  server.start()
  println(s"ClearingServer started on port $port")

  try Thread.currentThread().join()
  finally
    server.stop(0)
    executor.shutdown()
