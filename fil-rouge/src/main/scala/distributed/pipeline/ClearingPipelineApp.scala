package distributed.pipeline

import clearing.contract.ContractCodec
import clearing.model.BankCode
import distributed.kafka.*
import distributed.persistence.*
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.TopicPartition
import zio.*

import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*

object ClearingPipelineApp extends ZIOAppDefault:
  private val knownBanks = Set("AWB", "CIH", "BCP", "BMCE").map(BankCode.unsafe)
  private val durableProcessor = new DurableProcessor(knownBanks)
  private val recordProcessor = new RecordProcessor(knownBanks)

  private def envelope(record: ConsumerRecord[String, String]): RecordEnvelope =
    RecordEnvelope(
      record.topic(),
      record.partition(),
      record.offset(),
      Instant.ofEpochMilli(record.timestamp()),
      Option(record.key()),
      record.value()
    )

  private def processRecord(
    record: ConsumerRecord[String, String],
    publisher: ResultPublisher
  ): ZIO[ClearingRepository, Throwable, Unit] =
    val source = envelope(record)
    val decision = recordProcessor.process(source)

    decision match
      case rejected: ProcessingDecision.Rejected =>
        ZIO.attemptBlocking(
          publisher.publish(rejected).get(5, TimeUnit.SECONDS)
        ).unit

      case validated: ProcessingDecision.Validated =>
        for
          event <- ZIO
            .fromEither(ContractCodec.decode(source.payload))
            .mapError(new IllegalArgumentException(_))
          _ <- durableProcessor
            .process(source, event)
            .mapError {
              case error: clearing.core.ClearingError =>
                new IllegalStateException(error.message)
              case error: Throwable => error
            }
          _ <- ZIO.attemptBlocking(
            publisher.publish(validated).get(5, TimeUnit.SECONDS)
          )
        yield ()

  private def processPartition(
    partition: TopicPartition,
    records: List[ConsumerRecord[String, String]],
    publisher: ResultPublisher
  ): ZIO[ClearingRepository, Nothing, Option[(TopicPartition, OffsetAndMetadata)]] =
    ZIO
      .foldLeft(records)(Option.empty[Long]) { (_, record) =>
        processRecord(record, publisher).as(Some(record.offset()))
      }
      .map(_.map(offset => partition -> new OffsetAndMetadata(offset + 1)))
      .catchAll { error =>
        ZIO.logError(
          s"partition.failed topic=${partition.topic()} partition=${partition.partition()} error=${error.getMessage}"
        ).as(None)
      }

  private def loop(
    consumer: KafkaConsumer[String, String],
    publisher: ResultPublisher
  ): ZIO[ClearingRepository, Nothing, Nothing] =
    val iteration =
      for
        records <- ZIO.attemptBlocking(consumer.poll(Duration.ofMillis(500)))
        offsets <- ZIO.foreach(records.partitions().asScala.toList) { partition =>
          processPartition(
            partition,
            records.records(partition).asScala.toList,
            publisher
          )
        }
        committed = offsets.flatten.toMap
        _ <- ZIO.when(committed.nonEmpty)(
          ZIO.attemptBlocking(consumer.commitSync(committed.asJava))
        )
      yield ()

    iteration
      .catchAll(error => ZIO.logError(s"poll.failed: ${error.getMessage}") *> ZIO.sleep(1.second))
      .forever

  private def program: ZIO[ClearingRepository, Throwable, Nothing] =
    val settings = KafkaSettings.fromEnvironment()

    ZIO.scoped {
      for
        consumer <- ZIO.acquireRelease(
          ZIO.attempt(KafkaClients.consumer(settings))
        )(consumer => ZIO.attempt(consumer.close()).orDie)
        producer <- ZIO.acquireRelease(
          ZIO.attempt(KafkaClients.producer(settings))
        )(producer => ZIO.attempt(producer.close()).orDie)
        _ <- ZIO.attempt(consumer.subscribe(java.util.List.of(settings.inputTopic)))
        result <- loop(consumer, new ResultPublisher(producer, settings))
      yield result
    }

  def run =
    program.provide(
      ZLayer.succeed(CassandraSettings.fromEnvironment()),
      CassandraSession.live,
      PreparedStatements.live,
      LiveClearingRepository.layer
    )
