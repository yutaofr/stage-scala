package clearing.v13

import clearing.model.Currency
import com.sun.net.httpserver.HttpServer
import java.net.{InetSocketAddress, URI}
import java.net.http.HttpClient
import java.nio.charset.StandardCharsets
import java.time.Duration
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.util.Using

final class ExchangeRateServiceSpec extends AnyFlatSpec with Matchers:
  private def client: HttpClient =
    HttpClient.newBuilder()
      .connectTimeout(Duration.ofMillis(500))
      .build()

  private def withRawServer[A](
    status: Int,
    body: String
  )(test: URI => A): A =
    val server = HttpServer.create(
      new InetSocketAddress("127.0.0.1", 0),
      0
    )
    server.createContext(
      "/rates/EUR",
      exchange =>
        val bytes = body.getBytes(StandardCharsets.UTF_8)
        exchange.sendResponseHeaders(status, bytes.length.toLong)
        val output = exchange.getResponseBody
        try output.write(bytes)
        finally
          output.close()
          exchange.close()
    )
    server.start()
    val uri = URI.create(s"http://127.0.0.1:${server.getAddress.getPort}/")

    try test(uri)
    finally server.stop(0)

  "HttpExchangeRateService.fetchRate" should "lire les trois taux du serveur local" in:
    Using.resource(LocalExchangeRateServer.start()): server =>
      val service = new HttpExchangeRateService(server.baseUri, client)

      service.fetchRate(Currency.MAD) shouldBe Some(BigDecimal("1"))
      service.fetchRate(Currency.EUR) shouldBe Some(BigDecimal("10.80"))
      service.fetchRate(Currency.USD) shouldBe Some(BigDecimal("9.90"))

  it should "retourner None pour un statut non réussi" in:
    val rates = Map(
      Currency.MAD -> BigDecimal("1"),
      Currency.EUR -> BigDecimal("10.80")
    )

    Using.resource(LocalExchangeRateServer.start(rates)): server =>
      val service = new HttpExchangeRateService(server.baseUri, client)

      service.fetchRate(Currency.USD) shouldBe None

  it should "retourner None pour un JSON sans taux" in:
    withRawServer(status = 200, body = "{\"currency\":\"EUR\"}"):
      baseUri =>
        val service = new HttpExchangeRateService(baseUri, client)
        service.fetchRate(Currency.EUR) shouldBe None

  it should "retourner None pour un taux nul ou négatif" in:
    List("0", "-1").foreach: rawRate =>
      withRawServer(status = 200, body = s"{\"rate\":$rawRate}"):
        baseUri =>
          val service = new HttpExchangeRateService(baseUri, client)
          service.fetchRate(Currency.EUR) shouldBe None

  it should "retourner None lorsque le serveur est inaccessible" in:
    val service = new HttpExchangeRateService(
      URI.create("http://127.0.0.1:1/"),
      client
    )

    service.fetchRate(Currency.EUR) shouldBe None

  "HttpExchangeRateService.fetchRateTry" should "exposer un succès typé pour une réponse valide" in:
    withRawServer(status = 200, body = "{\"rate\":10.80}"):
      baseUri =>
        val service = new HttpExchangeRateService(baseUri, client)

        service.fetchRateTry(Currency.EUR).toOption shouldBe
          Some(BigDecimal("10.80"))

  it should "exposer en Failure les statuts, corps et appels invalides" in:
    withRawServer(status = 503, body = "indisponible"):
      baseUri =>
        val service = new HttpExchangeRateService(baseUri, client)
        service.fetchRateTry(Currency.EUR).isFailure shouldBe true

    withRawServer(status = 200, body = "{\"currency\":\"EUR\"}"):
      baseUri =>
        val service = new HttpExchangeRateService(baseUri, client)
        service.fetchRateTry(Currency.EUR).isFailure shouldBe true

    val inaccessible = new HttpExchangeRateService(
      URI.create("http://127.0.0.1:1/"),
      client
    )
    inaccessible.fetchRateTry(Currency.EUR).isFailure shouldBe true

  it should "restaurer le drapeau lorsque le client Java est interrompu" in:
    withRawServer(status = 200, body = "{\"rate\":10.80}"):
      baseUri =>
        val service = new HttpExchangeRateService(baseUri, client)

        try
          Thread.currentThread().interrupt()
          service.fetchRateTry(Currency.EUR).isFailure shouldBe true
          Thread.currentThread().isInterrupted shouldBe true
        finally Thread.interrupted()

  "HttpExchangeRateService.fromUrl" should "construire seulement une URL HTTP absolue" in:
    HttpExchangeRateService.fromUrl(
      "http://127.0.0.1:8080/api",
      client
    ).map(_.baseUri) shouldBe Some(
      URI.create("http://127.0.0.1:8080/api/")
    )

    HttpExchangeRateService.fromUrl("://url-invalide", client) shouldBe None
    HttpExchangeRateService.fromUrl("rates/local", client) shouldBe None
    HttpExchangeRateService.fromUrl("ftp://localhost/rates", client) shouldBe None
