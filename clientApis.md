[back](README.md)
# Client APIs

There are myriad ways to get data into/out of kafka. Kafka has first-class JVM (Java) support, but communicates over TCP and has good client support/bindings for other languages.

From a JVM point-of-view, the main client APIs are:

 * Kafka Connect
 * Kafka Consumer/Producer API
 * Kafka Streams
 * KSQL

These are also well-covered elsewhere (e.g. [here](https://medium.com/@stephane.maarek/the-kafka-api-battle-producer-vs-consumer-vs-kafka-connect-vs-kafka-streams-vs-ksql-ef584274c1e))

The main thing to keep in mind is the data resiliance. I'll cover that more in the consumer/producer API. 

## Kafka Connect

Kafka Connect provides a Source API (data into Kafka) and Sink API (data out-of Kafka).

This is best used when the source or sink is another process you don't control (e.g. to/from a database or REST service).

There are a lot of existing connectors which you could use out-of-the-box.

## Kafka Consumer/Producer API

Best used when integrating into applications which themselves are sources or sinks of data. 

It's also (in my opinion) a nicer API to work with.

### Caveat: consumer offset 

When consuming data from Kafka, you have some choices. Do you start from the latest (most recent) data available? The earliest? A specific partition/offset?

And what happens when you stop - or fail? Where will you consume from on restart?

The answer is **consumer groups**. When connecting to Kafka, the consumer configuration specifies a consumer group ID.

All consumers using the same consumer group ID will have the effect of load-balancing the messages across the consumers in that group.

When a consumer connects and subscribes to a topic, each partition in that topic will be consumed by exactly one consumer in the consumer group, and Kafka 
ensures that each record is only ever read by a single consumer in the group.

Kafka also tracks (by data currently stored in zookeeper, though that is being phased out) the consumer groups, and the configuration directs Kafka what to do when it first sees a new consumer group (e.g. start from the earliest or latest record).

## Poll loop

As a user of the consumer API, you have the choice of either Kafka controlling/managing what offsets have been consumed by your consumer or for you, dear client of their API, manually committing the offsets you consume.

The pseudocode usually looks something like this:
```
kafkaClient := ...
records := kafkaClient.poll()
for each record in records
   stuffResult := doSomeStuff(record)
   persistIntoDurableStorage(stuffResult)

kafkaClient.commitOffsetsForBatch(records)
```

This way, if the _doSomeStufff_ or _persistIntoDurableStorage_ fails (raises an error), the offsets are not committed back to kafka. The error propagates and kills the process, which gets restarted, and kafka will send records starting from the last committed offset.

### Single-Threading

Given the above model, the JVM Kafka client (at least) will throw an exception if the kafkaClient is accesses from more than one thread.

That is an understandable design decision -- they don't want stupid/lazy users of their API to do things which could unwittingly cause data loss.

And, as a stupid/lazy developer, I immediately wanted to circumvent that annoyance :-)

For example, consider this simplified/reduced example:
```
record for offset 1
──────────────────────────┐
                          │
                          │
record for offset 2       │
──────────────┐           │
              │           │
              │           │   thread foo processes
              │           │     message one
              │           │       ...
◄─────────────┘           │
                          │
  thread bar              │
  processes               │
  message two             │
  (offset 2)              │
  and completes           │
                          │
                          │
                       !!BANG!!
```

In the above example, we tried being clever by introducing concurrency, but screwed up.
The second record was processed and finished first, and (assuming we're using a work-around whereby we can invoke things on the single Kafka consumer thread) we
then commit that offset. 

If our process then crashes, we have a data loss for record offset 1.

Luckily as a Scala developer I can use IO/effects libraries with strong guarantees for resource allocation/cleanup, which allows me to put on my big-boy pants
and perform concurrent processing for CPU-intensive tasks while ensuring offsets are only committed when safe to do so.

If you have to work with another language, let's say Kotlin, your work-around might involve using co-routines to ensure each process is bound to one co-routine per partition.
That way, failures will be limited to the single partition of the failure. 

## Rebalancing

When consumers do drop out (are scaled, restarted, etc), the Kafka broker serving that consumer will notice (via a heartbeat timeout driven by the frequency of poll calls)
that a consumer is no longer active and reassign/rebalance that consumer's data across the remaining consumers in the group.

This can cause real headaches in otherwise BAU/normal scenarios, such as throttled/controlled canary deployments.

It's useful to plan and test your release/upgrade processes.

## Other concerns

Kafka is designed to be horizontally scalable in both writes and reads, but it's not magic.

Aside from deployment rollouts, there are also hotspotting issues in cases where either partition strategy is poorly designed or the data usage no longer
fits what has been planned for. Typical hotspotting remediation patterns may be of some use - such as introducing a salt or randomness, depending on your use-case.

## Kafka Streams and KSQL

Kafka Streams provides a now typical reactive-stream pattern for performing projections, filters and transformations of data in ETL pipelines.

For these use-cases, users can use either a familiar SQL grammar or KStream API to transform one topic into another. 

The caveat is that it's still all Kafka -- you would need Kafka Connect or Consumer API to get the data out of Kafka and into your application.

Personally, this reminds me a little of the Hadoop/Apache Spark ecosystem, where Apache Spark made huge performance gains by avoiding all the disk IO introduced by map/reduce.

So, for me, if the transformed data makes sense to be exposed as a new (output) topic in Kafka in its own right (e.g. it's useful or potentially useful to a few different consumer use-cases), then this makes sense.

If, however, you just want a conveniet way to map (transform) or filter your data, that may best be done in e.g. the consumer API.
