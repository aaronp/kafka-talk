[back](README.md)
# Schema Registry

In practice, the [Apache Avro](https://avro.apache.org/) project is a good bedfellow to represent the data squirted through kafka.

Avro is a language-agnostic protocol for the encoding of messages which can be thought of as json without the labels. 

e.g., instead of this:
```
{
  "name" : "Dave",
  "age" : 30
}
```

An avro record would split out the schema (a string 'name' field and integer 'age' field) and the values, which 
has the effect of:

 1) efficient binary (or json) encoding of messages
 2) an enforced/agreed schema

The idea is that you exchange the schema ... _somehow_ (perhaps a published artefact, a shared hard-coded file, via email ...)
and then use that schema to encode/decode messages.

In the case of the **schema registry**, that _somehow_ is Kafka itself.
Put another way, you can think about the **schema registry** as a database which provides storage/lookup of schemas by an ID, but you have to squint your eyes a bit, because the database is also actually Kafka itself.

### Example

If we think about a serialiser as the moral equivalent of this type signature:
```
# here Serialiser is a type alias for a function of any type 'A' into an array of bytes
type Serialiser[A] = A => Array[Byte]
```

Then the Kafka schema registry provides an implementation whereby the type `A` is an Avro record, and the (dirty, naughty, lying side-effecting function) does this:

```
class FakeExamplePseudoCodeSchemaRegistrySerialiser[A](schema : Schema, schemaReg : SchemaRegistry) extends Serialiser[A] {
  def apply(record : A) : Array[Byte] = {
     val byteArray = schema.toBytes(record)
     val schemaId = schemaReg.upsert(schema) // <-- this is locally cached/fast on subsequent calls
     concatBytes(schemaId, byteArray) // <-- make the first 4 bytes the ID of the schema
  }
}
```

As you can see, our schema-registry Serde prepends the byte array with the ID of our schema.

On the receiving end (the 'de' or 'derserialiser' of the 'serde'), a similar implementation (the dual of the above function) will:
 1) pop off the first 5 bytes
 2) interpret them as the schema ID
 3) ask for the schema by that ID from the schema registry (which will be cached)
 4) use that schema to decode the message and crack on

You can read more about the actual wire format [here](https://docs.confluent.io/platform/current/schema-registry/serdes-develop/index.html#wire-format).

The upshot of all this is that:
 1) you get schema exchange nearly for-free just by using the schema registry Serde
 2) you also get some schema compatibility checks

## Compatibility

You can read about compatibility modes [here](https://docs.confluent.io/platform/current/schema-registry/avro.html), but the long-and-short of it is that the Serde used to communicate schemas
can also verify their compatibility, effectively:
 1) throwing an error which prohibits producers from publishing incompatible records
 2) allowing consumers to deserialise compatible messages

### Notes / Warning / Caveat / Best Practice

The schema registry is intended as a transparent mechanism for communicating the schemas and providing (hopefully) seemless
compatbility.

That **doesn't** mean it should be used as a tool used by humans to negotiate/communciate avro schemas/contracts.

Ideally people/teams/companies will employ a small git repo of the agreed schema. The disparate teams representing the producers/consumers
of that data will then refer to a published artefact from that repo and generate stubs in their language of choice (e.g. golang, .net, java, etc bindings).

Those people/teams will then discuss, PR, semver, etc changes to that avro schema, write their software against those artefacts, and then trust and enjoy that Kafka
knows how to send/receive messages between applications which use those schemas.


### Exception to that rule

The exception is for pass-through applications. In Apache Avro, there is a [Specific Record](https://avro.apache.org/docs/current/api/java/org/apache/avro/specific/SpecificRecord.html) (e.g. a case class/data class/struct) 
and a [Generic Record](https://avro.apache.org/docs/current/api/java/index.html?org/apache/avro/generic/GenericRecord.html) (e.g. akin to Json or map/dictionary of values).

Some components don't or shouldn't need to care about the record. They might just need to turn it into json, or another blob of some sort, or nest it within another structure.

In those cases, it's a perfectly cromulent and valid situation to NOT have to release a new version of that component just because the schema has changed, 
as it can just deserialise the avro into a Generic Record.

