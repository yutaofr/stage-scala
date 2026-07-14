package clearing.v13

import clearing.model.Currency
import java.io.IOException
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration

trait ExchangeRateProvider:
  def fetchRate(currency: Currency): Option[BigDecimal]

final class HttpExchangeRateService(
  val baseUri: URI,
  client: HttpClient
) extends ExchangeRateProvider:
  private val ratePattern =
    """"rate"\s*:\s*([0-9]+(?:\.[0-9]+)?)""".r

  private def parseRate(body: String): Option[BigDecimal] =
    ratePattern
      .findFirstMatchIn(body)
      .map(result => BigDecimal(result.group(1)))
      .filter(_ > 0)

  def fetchRate(currency: Currency): Option[BigDecimal] =
    val request = HttpRequest.newBuilder()
      .uri(baseUri.resolve(s"rates/$currency"))
      .timeout(Duration.ofSeconds(2))
      .GET()
      .build()

    try
      val response = client.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      )
      Option.when(response.statusCode() == 200)(response.body())
        .flatMap(parseRate)
    catch
      case _: IOException              => None
      case _: IllegalArgumentException => None
      case _: InterruptedException =>
        Thread.currentThread().interrupt()
        None

object HttpExchangeRateService:
  def fromUrl(
    raw: String,
    client: HttpClient
  ): Option[HttpExchangeRateService] =
    try
      val normalized = raw.trim.stripSuffix("/") + "/"
      val uri = URI.create(normalized)
      Option.when(
        uri.isAbsolute && Set("http", "https").contains(uri.getScheme)
      )(new HttpExchangeRateService(uri, client))
    catch case _: IllegalArgumentException => None
