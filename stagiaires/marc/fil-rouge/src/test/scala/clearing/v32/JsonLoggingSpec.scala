package clearing.v32

import ch.qos.logback.classic.{Level, LoggerContext}
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.{ILoggingEvent, LoggingEvent}
import ch.qos.logback.core.OutputStreamAppender
import io.circe.parser.parse
import java.nio.charset.StandardCharsets
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class JsonLoggingSpec extends AnyFlatSpec with Matchers:
  "logback-json.xml" should "emit one parseable JSON object with stable fields" in:
    val context = LoggerContext()

    try
      val configurator = JoranConfigurator()
      configurator.setContext(context)
      configurator.doConfigure(
        getClass.getResource("/logback-json.xml")
      )
      val logger = context.getLogger("clearing.v32.json-test")
      val appender = context
        .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        .getAppender("JSON_CONSOLE")
        .asInstanceOf[OutputStreamAppender[ILoggingEvent]]
      val event = LoggingEvent(
        getClass.getName,
        logger,
        Level.INFO,
        "json contract",
        null,
        Array.empty
      )
      event.setMDCPropertyMap(java.util.Map.of())
      val line = String(
        appender.getEncoder.encode(event),
        StandardCharsets.UTF_8
      )
      val json = parse(line).fold(error => fail(error.message), identity)
      val cursor = json.hcursor
      cursor.get[String]("message") shouldBe Right("json contract")
      cursor.get[String]("level") shouldBe Right("INFO")
      cursor.get[String]("service") shouldBe Right("clearing-engine")
      cursor.get[String]("environment") shouldBe Right("local")
      cursor.get[String]("@timestamp").isRight shouldBe true
    finally
      context.stop()
