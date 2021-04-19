# Kafka Overview

## About

Apache [Kafka](https://kafka.apache.org/) was originally created at LinkedIn and open-sourced in 2012 as
a high-throughput, low-latency horizontally scalable message bus that was optimized for writes.
The creators subsequently spun-off [Confluent](https://www.confluent.io/hub/) to provide enterprise support, associated products and consultancy services for Kafka.  

Technically speaking, Kafka itself is a distributed commit log, and its inner workings, design, and [speed](https://medium.com/@sunny_81705/what-makes-apache-kafka-so-fast-71b477dcbf0) have been written about
quite extensively. The top-response from [this Stack Overflow](https://stackoverflow.com/questions/32631064/why-kafka-so-fast) sites these principal design decisions:
 * [Zero Copy](https://en.wikipedia.org/wiki/Zero-copy): basically it calls the OS kernal direct rather than at the application layer to move data fast.
 * Batching/Chunking: Kafka is all about batching the data into chunks. This minimises cross-machine latency and associated buffering/copying
 * Avoids Random Disk Access - as Kafka is an immutable commit log it does not need to rewind the disk and do many random I/O operations and can just access the disk in a sequential manner.
 * Can Scale Horizontally - The ability to have thousands of partitions for a single topic spread among thousands of machines means Kafka can handle huge loads

It is written in Scala and Java (source [here](https://github.com/apache/kafka)), and its deployments require [Apache Zookeeper](https://zookeeper.apache.org/) to facilitate leadership election and store metadata, 
though a RAFT-based version is underway which removes that requirement.

# Topics/Partitions

See [here](topicPartitions.md)

## Client APIs

See [client APIs](clientApis.md)




