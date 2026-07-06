package distributed.kafka

final case class KafkaSettings(
  bootstrapServers: String,
  inputTopic: String = "clearing-input",
  outputTopic: String = "clearing-output",
  dlqTopic: String = "clearing-dlq",
  groupId: String = "clearing-engine"
)

object KafkaSettings:
  def fromEnvironment(env: Map[String, String] = sys.env): KafkaSettings =
    KafkaSettings(
      bootstrapServers = env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
      inputTopic = env.getOrElse("KAFKA_INPUT_TOPIC", "clearing-input"),
      outputTopic = env.getOrElse("KAFKA_OUTPUT_TOPIC", "clearing-output"),
      dlqTopic = env.getOrElse("KAFKA_DLQ_TOPIC", "clearing-dlq"),
      groupId = env.getOrElse("KAFKA_GROUP_ID", "clearing-engine")
    )
