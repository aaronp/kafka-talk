package example

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig, Topology}

import java.util
import java.util.concurrent.CountDownLatch
import java.util.{Locale, Properties}
import scala.jdk.CollectionConverters._
import scala.sys.ShutdownHookThread
import scala.util.control.NonFatal

object WordCount {

  def apply(topology: Topology = topology(), props: Properties = localhost): KafkaStreams = new KafkaStreams(topology, props)

  def localhost = {
    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props
  }

  def topology(): Topology = {

    val builder = new StreamsBuilder

    val dataStream = builder.stream[String, String]("test-data")
      .flatMapValues { value =>
        println(value)
        value.toLowerCase(Locale.getDefault()).split("\\W+").toList.asJava
      }.groupBy { (key, value) => value }

    val counted = dataStream.count()

    val materialised = counted.toStream.to("streams-wordcount-output", Produced.`with`(Serdes.String, Serdes.Long))

    builder.build
  }
}
