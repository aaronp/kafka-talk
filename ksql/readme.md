#KSQL

This is just some example KSQL code taken from the min/max example [here](https://kafka-tutorials.confluent.io/create-stateful-aggregation-minmax/ksql.html) to play around w/ some [joins](https://docs.ksqldb.io/en/latest/developer-guide/joins/join-streams-and-tables/)

## Running:

Start up kafka, zookeeper, schema reg, etc:
```
docker-compose up -d
```


You can then open a ksql shell:
```
docker exec -it ksqldb-cli ksql http://ksqldb-server:8088
```

And execute some SQL to demonstrate keeping only the latest version of a series of records:
```
SET 'auto.offset.reset' = 'earliest';
SET 'ksql.streams.cache.max.bytes.buffering' = '10000000';

CREATE STREAM CUSTOMERS (id INT KEY, name VARCHAR, version INT)
    WITH (KAFKA_TOPIC='customers',
          PARTITIONS=1,
          VALUE_FORMAT='protobuf');

CREATE TABLE CUSTOMER_VERSIONS AS
	SELECT id, 
	       MIN(version) AS MIN_VERSION,
	       MAX(version) AS MAX_VERSION
	FROM CUSTOMERS
	GROUP BY id
	EMIT CHANGES;

CREATE TABLE CUSTOMER_LATEST AS
    SELECT c.id,
           LATEST_BY_OFFSET(c.name) AS name,
           LATEST_BY_OFFSET(c.version) AS version,
           LATEST_BY_OFFSET(v.MAX_VERSION) AS maxversion
    FROM CUSTOMERS AS c
	LEFT JOIN CUSTOMER_VERSIONS v ON c.id = v.id 
	WHERE c.version >= v.MAX_VERSION
    GROUP BY c.id
    EMIT CHANGES;


INSERT INTO CUSTOMERS (id, name, version) VALUES (1, 'Arn', 1);
INSERT INTO CUSTOMERS (id, name, version) VALUES (1, 'Foo', 3);
INSERT INTO CUSTOMERS (id, name, version) VALUES (1, 'Out of Order', 2);
INSERT INTO CUSTOMERS (id, name, version) VALUES (1, 'Aaron', 4);

INSERT INTO CUSTOMERS (id, name, version) VALUES (2, 'George', 2);
INSERT INTO CUSTOMERS (id, name, version) VALUES (2, 'G', 1);

INSERT INTO CUSTOMERS (id, name, version) VALUES (3, 'Eleanor', 100);
INSERT INTO CUSTOMERS (id, name, version) VALUES (3, 'Eli', 20);
INSERT INTO CUSTOMERS (id, name, version) VALUES (3, 'Nora', 30);
INSERT INTO CUSTOMERS (id, name, version) VALUES (3, 'mistake', 31);

INSERT INTO CUSTOMERS (id, name, version) VALUES (4, 'one', 100);
INSERT INTO CUSTOMERS (id, name, version) VALUES (4, 'two', 200);
INSERT INTO CUSTOMERS (id, name, version) VALUES (4, 'three', 300);

INSERT INTO CUSTOMERS (id, name, version) VALUES (5, 'lonely', 1);

INSERT INTO CUSTOMERS (id, name, version) VALUES (6, 'too', 100);
INSERT INTO CUSTOMERS (id, name, version) VALUES (6, 'two', 101);

INSERT INTO CUSTOMERS (id, name, version) VALUES (6, 'two', 101);



PRINT CUSTOMER_VERSIONS FROM BEGINNING LIMIT 6;
PRINT CUSTOMER_LATEST FROM BEGINNING LIMIT 6;

```


To clean-up/delete things:

```
show queries;  <-- followed by terminate <query id>
```

then check :
```
drop table CUSTOMER_LATEST;
drop table CUSTOMER_VERSIONS;
drop stream CUSTOMERS;
show tables;
show stream;
```


# Running tests

I've not actually managed to get this to work, but you should be able to also run the above as automated tests using the statements defined under `src`:

```
docker exec ksqldb-cli ksql-test-runner -s /opt/app/src/customers.sql  -i /opt/app/test/input.json -o /opt/app/test/
```
