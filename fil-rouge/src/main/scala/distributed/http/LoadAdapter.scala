package distributed.http

import clearing.contract.ContractCodec
import clearing.model.BankCode
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import distributed.kafka.{KafkaClients, KafkaSettings, TransactionProducer}

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

object LoadAdapter:
  private val knownBanks = Set("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe)

  private def respond(exchange: HttpExchange, status: Int, body: String): Unit =
    val bytes = body.getBytes(StandardCharsets.UTF_8)
    exchange.getResponseHeaders.set("Content-Type", "application/json")
    exchange.sendResponseHeaders(status, bytes.length)
    val output = exchange.getResponseBody
    try output.write(bytes)
    finally output.close()

  final class IngestionHandler(settings: KafkaSettings) extends HttpHandler:
    def handle(exchange: HttpExchange): Unit =
      if exchange.getRequestMethod != "POST" then
        respond(exchange, 405, """{"code":"METHOD_NOT_ALLOWED"}""")
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
            respond(exchange, 400, s"""{"code":"INVALID_TRANSACTION","message":"$error"}""")
          case Right(tx) =>
            val producer = KafkaClients.producer(settings)
            try
              TransactionProducer.send(producer, settings, tx).get(5, TimeUnit.SECONDS)
              respond(
                exchange,
                202,
                s"""{"transactionId":"${tx.id.value}","status":"accepted"}"""
              )
            catch
              case error: Throwable =>
                respond(
                  exchange,
                  503,
                  s"""{"code":"KAFKA_UNAVAILABLE","message":"${error.getMessage}"}"""
                )
            finally producer.close()

@main def runLoadAdapter(): Unit =
  val settings = KafkaSettings.fromEnvironment()
  val port = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)
  val server = HttpServer.create(new InetSocketAddress(port), 0)
  val executor = Executors.newCachedThreadPool()

  server.createContext("/api/v1/transactions", new LoadAdapter.IngestionHandler(settings))
  server.setExecutor(executor)
  server.start()
  println(s"LoadAdapter started on port $port")

  try Thread.currentThread().join()
  finally
    server.stop(0)
    executor.shutdown()
