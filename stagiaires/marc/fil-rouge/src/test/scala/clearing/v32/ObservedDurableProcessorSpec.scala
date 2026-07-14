package clearing.v32

import ch.qos.logback.classic.{Level, Logger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import clearing.v30.*
import clearing.v31.*
import java.time.Instant
import java.util.concurrent.{ConcurrentHashMap, CountDownLatch, TimeUnit}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.{LoggerFactory, MDC}
import scala.jdk.CollectionConverters.*

final class ObservedDurableProcessorSpec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")

  "ObservedDurableProcessor" should "wrap the real v3.1 durable processor" in:
    val repository = InMemoryDurableRepository()
    val durable = DurableRecordProcessor(
      _ => validated(42),
      repository,
      DecisionPublisher((_, _) => Right(())),
      () => instant
    )
    val (logger, events) = testLogger("real")
    val observed = ObservedDurableProcessor(durable, logger)

    observed.process(envelope("42")) shouldBe DurableRecordOutcome.Published
    observed.process(envelope("42")) shouldBe DurableRecordOutcome.Duplicate

    events.list.asScala.map(_.getFormattedMessage).toList should contain allOf (
      "clearing record started",
      "clearing record completed status=success",
      "clearing record completed status=duplicate"
    )
    repository.snapshot shouldBe DurableSnapshot(1, 2, 2, 1)

  it should "restore the previous MDC after success and failure" in:
    val seen = collection.mutable.ListBuffer.empty[Map[String, String]]
    val delegate = new DurableProcessing:
      def process(envelope: RecordEnvelope): DurableRecordOutcome =
        seen += Option(MDC.getCopyOfContextMap)
          .fold(Map.empty[String, String])(_.asScala.toMap)
        if envelope.offset == 8L then throw new IllegalStateException("boom")
        DurableRecordOutcome.Published
    val observed = ObservedDurableProcessor(delegate, testLogger("restore")._1)
    MDC.put("requestId", "outer")

    try
      observed.process(envelope("42")) shouldBe DurableRecordOutcome.Published
      MDC.get("requestId") shouldBe "outer"
      MDC.get("txId") shouldBe null

      intercept[IllegalStateException]:
        observed.process(envelope("43").copy(offset = 8L))
      MDC.get("requestId") shouldBe "outer"
      MDC.get("txId") shouldBe null
    finally MDC.clear()

    seen.toList.map(_("requestId")) shouldBe List("outer", "outer")
    seen.toList.map(_("txId")) shouldBe List("42", "43")
    seen.toList.map(_("offset")) shouldBe List("7", "8")

  it should "isolate correlation fields across two concurrent virtual threads" in:
    val ready = CountDownLatch(2)
    val release = CountDownLatch(1)
    val seen = ConcurrentHashMap[String, Map[String, String]]()
    val after = ConcurrentHashMap[String, Option[String]]()
    val delegate = new DurableProcessing:
      def process(envelope: RecordEnvelope): DurableRecordOutcome =
        val txId = MDC.get("txId")
        seen.put(txId, MDC.getCopyOfContextMap.asScala.toMap)
        ready.countDown()
        release.await(5, TimeUnit.SECONDS) shouldBe true
        DurableRecordOutcome.Published
    val observed = ObservedDurableProcessor(delegate, testLogger("threads")._1)

    val threads = List("41", "42").map: txId =>
      startThread: () =>
        observed.process(envelope(txId))
        after.put(txId, Option(MDC.get("txId")))

    ready.await(5, TimeUnit.SECONDS) shouldBe true
    release.countDown()
    threads.foreach(_.join(5000))

    if virtualThreadsAvailable then
      threads.foreach(thread => isVirtual(thread) shouldBe true)
    seen.get("41")("txId") shouldBe "41"
    seen.get("42")("txId") shouldBe "42"
    after.asScala.values.toList shouldBe List(None, None)

  it should "keep payload and sensitive business values out of log events" in:
    val (logger, events) = testLogger("sensitive")
    val observed = ObservedDurableProcessor(
      new DurableProcessing:
        def process(envelope: RecordEnvelope) = DurableRecordOutcome.Published,
      logger
    )
    val sensitive =
      "MA64011519000001205000534921 amount=9999.99 client=secret"

    observed.process(envelope("42").copy(value = sensitive))

    val rendered = events.list.asScala
      .flatMap(event => List(event.getFormattedMessage, event.getMDCPropertyMap.toString))
      .mkString("\n")
    rendered should not include sensitive
    rendered should not include "MA64011519000001205000534921"
    rendered should not include "9999.99"
    rendered should not include "client=secret"

  private def envelope(txId: String): RecordEnvelope =
    RecordEnvelope(
      KafkaSettings.InputTopic,
      0,
      7L,
      Some("AWB"),
      Map("transaction-id" -> txId),
      "payload-a",
      instant
    )

  private def validated(id: Int): ProcessingDecision =
    ProcessingDecision.Validated(
      ValidatedEvent(
        id,
        "AWB",
        "CIH",
        "100.00",
        "1.00",
        "MAD",
        "TRANSFER",
        "Pending",
        "a" * 64,
        "b" * 64,
        "virement",
        Nil,
        instant
      )
    )

  private def testLogger(suffix: String): (Logger, ListAppender[ILoggingEvent]) =
    val logger = LoggerFactory
      .getLogger(s"clearing.v32.test.$suffix")
      .asInstanceOf[Logger]
    logger.setLevel(Level.INFO)
    logger.setAdditive(false)
    logger.detachAndStopAllAppenders()
    val appender = ListAppender[ILoggingEvent]()
    appender.start()
    logger.addAppender(appender)
    logger -> appender

  private def startThread(body: () => Unit): Thread =
    try
      classOf[Thread]
        .getMethod("startVirtualThread", classOf[Runnable])
        .invoke(null, (() => body()): Runnable)
        .asInstanceOf[Thread]
    catch
      case _: NoSuchMethodException =>
        val thread = Thread((() => body()): Runnable)
        thread.start()
        thread

  private def virtualThreadsAvailable: Boolean =
    classOf[Thread].getMethods.exists(_.getName == "startVirtualThread")

  private def isVirtual(thread: Thread): Boolean =
    classOf[Thread]
      .getMethod("isVirtual")
      .invoke(thread)
      .asInstanceOf[Boolean]
