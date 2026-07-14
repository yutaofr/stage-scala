package clearing.v13

import clearing.model.Currency
import com.sun.net.httpserver.{HttpExchange, HttpServer}
import java.net.{InetSocketAddress, URI}
import java.nio.charset.StandardCharsets

final class LocalExchangeRateServer private (
  server: HttpServer,
  val baseUri: URI
) extends AutoCloseable:
  def close(): Unit = server.stop(0)

object LocalExchangeRateServer:
  val DefaultRates: Map[Currency, BigDecimal] = Map(
    Currency.MAD -> BigDecimal("1"),
    Currency.EUR -> BigDecimal("10.80"),
    Currency.USD -> BigDecimal("9.90")
  )

  private def respond(
    exchange: HttpExchange,
    status: Int,
    body: Option[String]
  ): Unit =
    val bytes = body
      .map(_.getBytes(StandardCharsets.UTF_8))
      .getOrElse(Array.emptyByteArray)
    exchange.getResponseHeaders.add(
      "Content-Type",
      "application/json; charset=utf-8"
    )
    exchange.sendResponseHeaders(
      status,
      if body.isDefined then bytes.length.toLong else -1L
    )
    val output = exchange.getResponseBody
    try
      if bytes.nonEmpty then output.write(bytes)
    finally
      output.close()
      exchange.close()

  def start(
    rates: Map[Currency, BigDecimal] = DefaultRates
  ): LocalExchangeRateServer =
    val server = HttpServer.create(
      new InetSocketAddress("127.0.0.1", 0),
      0
    )
    server.createContext(
      "/rates/",
      exchange =>
        val rawCurrency = exchange.getRequestURI.getPath
          .stripPrefix("/rates/")
        val rate = Currency.fromString(rawCurrency).flatMap(rates.get)

        if exchange.getRequestMethod != "GET" then
          respond(exchange, 405, None)
        else
          rate match
            case Some(value) =>
              val body =
                s"""{"currency":"${rawCurrency.toUpperCase}","rate":${value.bigDecimal.toPlainString}}"""
              respond(exchange, 200, Some(body))
            case None => respond(exchange, 404, None)
    )
    server.start()
    val baseUri = URI.create(
      s"http://127.0.0.1:${server.getAddress.getPort}/"
    )
    new LocalExchangeRateServer(server, baseUri)
