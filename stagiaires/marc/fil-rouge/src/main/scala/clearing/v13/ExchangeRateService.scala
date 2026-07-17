package clearing.v13

import clearing.model.Currency
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

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
    fetchRateTry(currency).toOption

  def fetchRateTry(currency: Currency): Try[BigDecimal] =
    try
      val request = HttpRequest.newBuilder()
        .uri(baseUri.resolve(s"rates/$currency"))
        .timeout(Duration.ofSeconds(2))
        .GET()
        .build()
      val response = client.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      )

      if response.statusCode() != 200 then
        Failure(
          ExchangeRateFailure(s"statut HTTP ${response.statusCode()}")
        )
      else
        parseRate(response.body()) match
          case Some(rate) => Success(rate)
          case None =>
            Failure(ExchangeRateFailure("taux absent ou invalide"))
    catch
      case error: InterruptedException =>
        Thread.currentThread().interrupt()
        Failure(error)
      case NonFatal(error) => Failure(error)

  private case class ExchangeRateFailure(detail: String)
      extends RuntimeException(detail)

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
