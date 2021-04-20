package example

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig, Topology}
import org.apache.kafka.streams.kstream.{KeyValueMapper, Materialized, Produced, ValueMapper}
import org.apache.kafka.streams.state.KeyValueStore

import java.util
import java.util.{Locale, Properties}
import java.util.concurrent.CountDownLatch
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

object WordCount {

  def apply() = {
    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)

    val builder = new StreamsBuilder

    val dataStream = builder.stream[String, String]("streams-plaintext-input")
      .flatMapValues(value => java.util.Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
      .groupBy {
        (key, value) => value
      }.count(
      Materialized.as[String, Long, KeyValueStore[Bytes, Array[Byte]]]("counts-store")
    ).toStream.to("streams-wordcount-output", Produced.`with`(Serdes.String, Serdes.Long))

    val topology = builder.build
    val streams = new KafkaStreams(topology, props)
    streams
  }
}
