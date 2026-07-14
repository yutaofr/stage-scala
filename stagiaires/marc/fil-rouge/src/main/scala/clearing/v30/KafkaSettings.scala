package clearing.v30

import java.util.Properties
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object KafkaSettings:
  val InputTopic = "clearing-input"
  val OutputTopic = "clearing-output"
  val DlqTopic = "clearing-dlq"

object KafkaProducerSettings:
  def properties(bootstrapServers: String): Properties =
    val values = new Properties()
    values.setProperty(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
      bootstrapServers
    )
    values.setProperty(ProducerConfig.ACKS_CONFIG, "all")
    values.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
    values.setProperty(
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      classOf[StringSerializer].getName
    )
    values.setProperty(
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      classOf[StringSerializer].getName
    )
    values.setProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "120000")
    values.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "marc-v30-producer")
    values

object KafkaConsumerSettings:
  def properties(
    bootstrapServers: String,
    groupId: String
  ): Properties =
    val values = new Properties()
    values.setProperty(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
      bootstrapServers
    )
    values.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    values.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    values.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    values.setProperty(
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
      classOf[StringDeserializer].getName
    )
    values.setProperty(
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
      classOf[StringDeserializer].getName
    )
    values.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "marc-v30-consumer")
    values
