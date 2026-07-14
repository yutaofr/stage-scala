package clearing.v32

import clearing.v30.{BatchReport, InputPartition}
import java.util.concurrent.atomic.AtomicBoolean
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ClearingAppV32Spec extends AnyFlatSpec with Matchers:
  "V32Settings" should "provide stable local observability defaults" in:
    V32Settings.from(Map.empty) shouldBe Right(
      V32Settings(
        bootstrapServers = "localhost:9092",
        groupId = "marc-clearing-v32",
        metricsPort = 8080,
        otlpEndpoint = "http://localhost:4317",
        serviceName = "clearing-engine"
      )
    )

  it should "read runtime endpoints and reject an invalid metrics port" in:
    V32Settings.from(
      Map(
        "KAFKA_BOOTSTRAP_SERVERS" -> "kafka:29092",
        "KAFKA_GROUP_ID" -> "runtime-gate",
        "METRICS_PORT" -> "18080",
        "OTEL_EXPORTER_OTLP_ENDPOINT" -> "http://otel-collector:4317",
        "OTEL_SERVICE_NAME" -> "clearing-engine-gate"
      )
    ) shouldBe Right(
      V32Settings(
        "kafka:29092",
        "runtime-gate",
        18080,
        "http://otel-collector:4317",
        "clearing-engine-gate"
      )
    )
    V32Settings.from(Map("METRICS_PORT" -> "70000")).isLeft shouldBe true

  "V32ConsumerRuntime" should "close consumer and observability resources in order" in:
    val closed = collection.mutable.ListBuffer.empty[String]
    val control = FakeConsumerControl(closed)
    val runtime = V32ConsumerRuntime(
      control,
      List(
        named("metrics-http", closed),
        named("telemetry", closed),
        named("registry", closed)
      )
    )

    runtime.close()

    closed.toList shouldBe
      List("consumer", "metrics-http", "telemetry", "registry")

  it should "continue closing after one resource fails" in:
    val closed = collection.mutable.ListBuffer.empty[String]
    val runtime = V32ConsumerRuntime(
      FakeConsumerControl(closed),
      List(
        () =>
          closed += "metrics-http"
          throw new IllegalStateException("close failed"),
        named("telemetry", closed),
        named("registry", closed)
      )
    )

    intercept[IllegalStateException](runtime.close()).getMessage shouldBe
      "close failed"
    closed.toList shouldBe
      List("consumer", "metrics-http", "telemetry", "registry")

  it should "combine bounded polls until the requested record count" in:
    val partition = InputPartition(0)
    val reports = collection.mutable.Queue(
      BatchReport(Map(partition -> 1L), 1, 0, Set.empty),
      BatchReport(Map(partition -> 3L), 1, 1, Set.empty)
    )
    val runtime = V32ConsumerRuntime(
      new ConsumerControl:
        def pollOnce(): BatchReport = reports.dequeue()
        def runUntil(stopRequested: () => Boolean): Unit = ()
        def wakeup(): Unit = ()
        def close(): Unit = (),
      Nil
    )

    runtime.runBounded(3) shouldBe
      BatchReport(Map(partition -> 3L), 2, 1, Set.empty)

  private def named(
    name: String,
    closed: collection.mutable.ListBuffer[String]
  ): AutoCloseable = () => closed += name

private final class FakeConsumerControl(
  closed: collection.mutable.ListBuffer[String]
) extends ConsumerControl:
  def pollOnce(): BatchReport = BatchReport(Map.empty, 0, 0, Set.empty)
  def runUntil(stopRequested: () => Boolean): Unit = ()
  def wakeup(): Unit = ()
  def close(): Unit = closed += "consumer"
