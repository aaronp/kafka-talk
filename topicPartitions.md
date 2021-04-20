[back](README.md)
# Topic Partitions

At its core, data in Kafka is arranged into key/value pairs under a particular Kafka topic (which is just a name) and stored in [batches](https://kafka.apache.org/documentation/#messageformat).

Kafka topics are created with a number of replicas (redundant copies) and partitions (shards).

When data is published to Kafka, if a specific partition is not specified, then a hashing mechanism is used based on the key.

The upshot of all of this is that there is *not* global ordering for a topic, but only for a topic-partition.

There is far more detail to cover in topic-partition leaders, write ack options, etc, which is out-of-scope for this talk. 