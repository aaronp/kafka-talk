CREATE STREAM CUSTOMERS (id INT KEY, name VARCHAR, version INT) WITH (KAFKA_TOPIC='CUSTOMERS', PARTITIONS=1, VALUE_FORMAT='avro');

CREATE TABLE CUSTOMER_VERSIONS WITH (PARTITIONS=1, VALUE_FORMAT='avro') AS
	SELECT id, MIN(version) AS MIN_VERSION, MAX(version) AS MAX_VERSION
	FROM CUSTOMERS GROUP BY id EMIT CHANGES;

CREATE TABLE CUSTOMER_LATEST WITH (KAFKA_TOPIC='customer_latest',
          PARTITIONS=1,
          VALUE_FORMAT='avro') AS
    SELECT c.id,
           LATEST_BY_OFFSET(c.name) AS name,
           LATEST_BY_OFFSET(c.version) AS version,
           LATEST_BY_OFFSET(v.MAX_VERSION) AS maxversion
    FROM CUSTOMERS AS c
	LEFT JOIN CUSTOMER_VERSIONS v ON c.id = v.id 
	WHERE c.version >= v.MAX_VERSION
    GROUP BY c.id
    EMIT CHANGES;