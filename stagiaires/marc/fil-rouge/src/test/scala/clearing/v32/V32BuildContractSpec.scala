package clearing.v32

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class V32BuildContractSpec extends AnyFlatSpec with Matchers:
  private val root = projectRoot()

  "The v3.2 build" should "pin the observability dependencies and main class" in:
    val build = read(root.resolve("build.sbt"))

    build should include("3.2.0-SNAPSHOT")
    build should include("clearing.v32.runClearingAppV32")
    build should not include "slf4j-nop"
    build should include("\"ch.qos.logback\" % \"logback-classic\" % \"1.5.38\"")
    build should include(
      "\"net.logstash.logback\" % \"logstash-logback-encoder\" % \"9.0\""
    )
    build should include(
      "\"io.micrometer\" % \"micrometer-registry-prometheus\" % \"1.16.5\""
    )
    build should include(
      "\"io.opentelemetry\" % \"opentelemetry-sdk\" % \"1.63.0\""
    )
    build should include(
      "\"io.opentelemetry\" % \"opentelemetry-exporter-otlp\" % \"1.63.0\""
    )
    build should include(
      "\"io.opentelemetry\" % \"opentelemetry-sdk-testing\" % \"1.63.0\" % Test"
    )

  it should "provide distinct readable and JSON Logback configurations" in:
    val readable = read(root.resolve("src/main/resources/logback.xml"))
    val json = read(root.resolve("src/main/resources/logback-json.xml"))

    readable should include("PatternLayoutEncoder")
    readable should not include "LogstashEncoder"
    json should include("LogstashEncoder")
    List("service", "environment").foreach: field =>
      json should include(field)
    json should include("clearing-engine")

  private def projectRoot(): Path =
    Iterator
      .iterate(Paths.get("").toAbsolutePath)(_.getParent)
      .takeWhile(_ != null)
      .find(path => Files.exists(path.resolve("build.sbt")))
      .getOrElse(
        throw new IllegalStateException("racine du projet sbt introuvable")
      )

  private def read(path: Path): String =
    Files.readString(path, StandardCharsets.UTF_8)
