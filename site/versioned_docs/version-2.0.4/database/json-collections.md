---
title: JSON Collections and Duality Views
sidebar_position: 4
---

# JSON Collections and Duality Views

The JSON starter provides dependencies and utilities for working with Oracle AI Database JSON data, including the JSON data type, JSON Relational Duality Views, and [Oracle's efficient serialized JSON format, OSON](https://medium.com/db-one/a-deep-dive-into-binary-json-formats-oson-e3190e5e9eb0).

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-starter-json-collections</artifactId>
</dependency>
```

## JSONB Conversion

The `JSONB` bean converts Java objects to and from OSON using `fromOSON` and `toOSON`. `InputStream`, `JsonParser`, `ByteBuffer`, and `byte[]` are supported as OSON input types. 

```java
@Autowired
JSONB jsonb;

// byte[], JsonParser, and ByteBuffer also supported as input types
Student student = jsonb.fromOSON(inputStream, Student.class);
byte[] bytes = jsonb.toOSON(student);
```

## Mapping OSON Rows

`JSONBRowMapper` converts OSON database columns to Java objects:

```java
RowMapper<Student> rowMapper = new JSONBRowMapper<>(this.jsonb, Student.class);

List<Student> students = jdbcTemplate.query(con -> {
    PreparedStatement ps = con.prepareStatement("""
        select * from students_dv v
        where v.data.first_name = ?
        and v.data.last_name = ?
        """);
    ps.setString(1, firstName);
    ps.setString(2, lastName);
    return ps;
}, rowMapper);
```

By default, the first column is selected as the JSON column. You may customize this when instantiating the `JSONBRowMapper`:

```java
RowMapper<Student> rowMapper = new JSONBRowMapper<>(
        this.jsonb, 
        Student.class,
        2 // column number of OSON data
);
```

## OSON Kafka Serializers

Spring Boot applications that mix OKafka APIs with OSON data may benefit from using OSON as the message serialization format. This is particularly useful when you're already using OSON as the storage format - messages sent as OSON may be inserted into a table using Oracle's JSON type without any further serialization.

The `OSONKafkaSerializationFactory` bean provides factory methods for kafka-java serializers and deserializers that allow you to send events in OSON format.

### Consumer Deserializer

```java
@Autowired
private OSONKafkaSerializationFactory osonKafkaSerializationFactory;

Deserializer<String> keyDeserializer = new StringDeserializer();
Deserializer<Sensor> valueDeserializer = osonKafkaSerializationFactory.createDeserializer(Sensor.class);
return new KafkaConsumer<>(props, keyDeserializer, valueDeserializer);
```

### Producer Serializer

```java
@Autowired
private OSONKafkaSerializationFactory osonKafkaSerializationFactory;


Serializer<String> keySerializer = new StringSerializer();
Serializer<Sensor> valueSerializer = osonKafkaSerializationFactory.createSerializer();
return new KafkaProducer<>(props, keySerializer, valueSerializer);
```

The Kafka/OSON classes will only be autowired if `kafka-clients` is on the classpath.

## Learn by example

- [Spring JDBC with JSON Relational Duality Views](https://github.com/oracle/spring-cloud-oracle/tree/main/database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-json-duality)
- [JSON Event Streaming](https://github.com/oracle/spring-cloud-oracle/tree/main/database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-json-events)