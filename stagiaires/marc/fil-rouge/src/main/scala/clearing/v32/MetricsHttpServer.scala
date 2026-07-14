package clearing.v32

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

final class MetricsHttpServer private (
  server: HttpServer
) extends AutoCloseable:
  private val closed = AtomicBoolean(false)

  def port: Int = server.getAddress.getPort

  override def close(): Unit =
    if closed.compareAndSet(false, true) then server.stop(0)

object MetricsHttpServer:
  def start(
    registry: PrometheusMeterRegistry,
    port: Int = 8080
  ): MetricsHttpServer =
    require(port >= 0 && port <= 65535, "port HTTP invalide")
    val server = HttpServer.create(InetSocketAddress("0.0.0.0", port), 0)
    server.createContext(
      "/metrics",
      exchange => handle(exchange, registry.scrape())
    )
    server.createContext("/health", exchange => handle(exchange, "UP\n"))
    server.start()
    MetricsHttpServer(server)

  private def handle(exchange: HttpExchange, body: => String): Unit =
    try
      if exchange.getRequestMethod != "GET" then
        exchange.sendResponseHeaders(405, -1L)
      else
        val bytes = body.getBytes(StandardCharsets.UTF_8)
        exchange.getResponseHeaders.set(
          "Content-Type",
          "text/plain; version=0.0.4; charset=utf-8"
        )
        exchange.sendResponseHeaders(200, bytes.length.toLong)
        exchange.getResponseBody.write(bytes)
    finally exchange.close()
