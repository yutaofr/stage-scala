package clearing.v31

import clearing.v22.{HashBoundary, V22Profiles}
import clearing.v22.DomainTypes.*
import clearing.v30.*
import java.time.{Instant, LocalDate}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IntegrationV31Spec extends AnyFlatSpec with Matchers:
  private val instant = Instant.parse("2026-07-14T12:00:00Z")
  private val date = LocalDate.parse("2026-07-14")
  private val hash = IbanHash.from("a" * 64).toOption.get
  private val stableHash: HashBoundary = _ => Right(hash)

  private def await[A](stage: java.util.concurrent.CompletionStage[A]): A =
    stage.toCompletableFuture.get()

  "Le gate v3.1" should "classifier 500 records sans doubler les projections" in:
    val scenario = V31QualificationScenario.records(1600L)
    val repository = InMemoryDurableRepository()
    val decisions = collection.mutable.ListBuffer.empty[ProcessingDecision]
    val processor = DurableRecordProcessor(
      V30RecordProcessor(V22Profiles.clearingMAD, stableHash).process,
      repository,
      DecisionPublisher: (_, decision) =>
        decisions += decision
        Right(()),
      () => instant
    )
    val offsets = collection.mutable.Map.empty[Int, Long].withDefaultValue(0L)
    val envelopes = scenario.zipWithIndex.map: (record, index) =>
      val partition = Math.floorMod(record.key.hashCode, 3)
      val offset = offsets(partition)
      offsets.update(partition, offset + 1L)
      RecordEnvelope(
        KafkaSettings.InputTopic,
        partition,
        offset,
        Some(record.key),
        record.transactionId.fold(Map.empty[String, String])(id =>
          Map("transaction-id" -> id.toString)
        ),
        record.value,
        instant.plusMillis(index.toLong)
      )

    val report = DurableBatchCoordinator(processor).process(envelopes)

    report.published shouldBe 490
    report.duplicates shouldBe 10
    report.failedPartitions shouldBe empty
    report.retryOffsets shouldBe empty
    val validated = decisions.collect:
      case ProcessingDecision.Validated(event) => event
    validated should have size 485
    val rejected = decisions.collect:
      case ProcessingDecision.Rejected(event) => event
    rejected should have size 5
    repository.snapshot shouldBe DurableSnapshot(485, 970, 970, 485)
    await(repository.historyByDate(date)) should have size 485

    val uniqueInputs = scenario
      .flatMap(record => EventCodec.decodeInput(record.value).toOption)
      .distinctBy(_.id)
    val banks = uniqueInputs.flatMap(event => List(event.sender, event.receiver)).toSet
    val positions = banks.toList.map(bank =>
      await(repository.positionByBank(bank, date))
    )
    positions.map(_.amount).sum shouldBe BigDecimal(0)
    positions.map(_.transactionCount).sum shouldBe 970

  it should "ne conserver aucun IBAN brut dans les sorties ou projections" in:
    val repository = InMemoryDurableRepository()
    val encoded = collection.mutable.ListBuffer.empty[String]
    val processor = DurableRecordProcessor(
      V30RecordProcessor(V22Profiles.clearingMAD, stableHash).process,
      repository,
      DecisionPublisher: (_, decision) =>
        encoded += EventCodec.encodeDecision(decision)
        Right(()),
      () => instant
    )
    val scenario = V31QualificationScenario.records(1600L)
    val envelopes = scenario.zipWithIndex.map: (record, index) =>
      RecordEnvelope(
        KafkaSettings.InputTopic,
        index % 3,
        index.toLong,
        Some(record.key),
        Map.empty,
        record.value,
        instant
      )

    DurableBatchCoordinator(processor).process(envelopes)

    encoded.mkString("\n") should not fullyMatch regex (
      "(?s).*MA[A-Z0-9]{22}.*"
    )
    await(repository.historyByDate(date)).foreach: row =>
      row.toString should not fullyMatch regex ("(?s).*MA[A-Z0-9]{22}.*")
