package clearing.v30

enum V30Command:
  case Produce(command: ProducerCommand)
  case Consume(command: ConsumerCommand)

object V30Cli:
  val Usage = "Usage : run producer|consumer [options]"

  def parse(args: List[String]): Either[String, V30Command] = args match
    case "producer" :: tail =>
      ProducerCli.parse(tail).map(V30Command.Produce.apply)
    case "consumer" :: tail =>
      ConsumerCli.parse(tail).map(V30Command.Consume.apply)
    case _ => Left(Usage)

object V30Renderer:
  def producer(report: ProducerReport): String =
    s"PRODUCER_V30 attempted=${report.attempted} " +
      s"acknowledged=${report.acknowledged} failed=${report.failed}"

  def consumer(report: BatchReport): String =
    s"CONSUMER_V30 published=${report.published} " +
      s"duplicates=${report.duplicates} " +
      s"failedPartitions=${report.failedPartitions.size}"

object V30Reporter:
  def print(message: String): Unit = println(message)

object ClearingAppV30:
  def run(command: V30Command): Unit = command match
    case V30Command.Produce(producerCommand) =>
      V30Reporter.print(
        V30Renderer.producer(ProducerApp.run(producerCommand))
      )
    case V30Command.Consume(consumerCommand) =>
      consumerCommand.maxRecords match
        case None =>
          ConsumerApp.run(
            consumerCommand.bootstrapServers,
            consumerCommand.groupId
          )
        case Some(maxRecords) =>
          val loop = KafkaConsumerLoop.live(
            consumerCommand.bootstrapServers,
            consumerCommand.groupId
          )
          try
            V30Reporter.print(
              V30Renderer.consumer(
                ConsumerApp.runBounded(loop, maxRecords)
              )
            )
          finally loop.close()

@main def runClearingAppV30(args: String*): Unit =
  V30Cli.parse(args.toList) match
    case Left(error)    => V30Reporter.print(error)
    case Right(command) => ClearingAppV30.run(command)
