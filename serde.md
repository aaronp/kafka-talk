[back](README.md)
# Serde (Serialiation/Deserialisation)

Kafka itself is (on one level) just a dumb key/value store of byte arrays.

When using the various APIs to produce/consume messages from Kafka, you specify a 'serde' (serialiser/deserialiser) for both the keys and the values.

The 'serde' is just a simple function of a byte array to some type (int, long, string, avro, protobuf, etc).

You can of course perform 'vanilla' avro serialisation, turning the records into bytes via your language's favourite avro library.

Kafka also offers another little 'trick' though in the [schema registry](schemaRegistry.md).