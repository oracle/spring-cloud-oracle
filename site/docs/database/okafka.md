---
title: OKafka
sidebar_position: 5
---

# OKafka

The OKafka starter enables Spring Boot applications to use Kafka-style producer, consumer, and administration APIs with Oracle AI Database Transactional Event Queues.

This starter brings in the `com.oracle.database.messaging:okafka` dependency so applications can:

- create Kafka-style producers and consumers backed by Oracle TxEventQ
- manage TxEventQ topics with the OKafka admin client
- use Oracle AI Database as the event log while keeping a Kafka client programming model

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-starter-okafka</artifactId>
</dependency>
```

## Connection Properties

OKafka uses standard Kafka-style properties together with Oracle-specific settings for the target database service and wallet or TNS directory:

```java
Properties props = new Properties();
props.put("oracle.service.name", serviceName);
props.put("security.protocol", securityProtocol);
props.put("bootstrap.servers", bootstrapServers);
props.put("oracle.net.tns_admin", ojdbcPath);
```

- `oracle.service.name` selects the Oracle AI Database service, for example `freepdb1`
- `bootstrap.servers` identifies the database listener host and port
- `oracle.net.tns_admin` points to a wallet or connection-properties directory when needed
- `security.protocol` can be `PLAINTEXT` for local development or `SSL` for wallet-backed connections

## Producer and Consumer Setup

Applications use the Oracle OKafka producer and consumer implementations directly:

```java
props.put("enable.idempotence", "true");
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

KafkaProducer<String, String> okafkaProducer = new KafkaProducer<>(props);
```

```java
props.put("group.id", "MY_CONSUMER_GROUP");
props.put("enable.auto.commit", "false");
props.put("max.poll.records", 2000);
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("auto.offset.reset", "earliest");

Consumer<String, String> okafkaConsumer = new KafkaConsumer<>(props);
```

Use the Oracle OKafka client classes rather than the default Apache Kafka implementations:

- `org.oracle.okafka.clients.producer.KafkaProducer`
- `org.oracle.okafka.clients.consumer.KafkaConsumer`
- `org.oracle.okafka.clients.admin.AdminClient`

## Topic Administration

You can create a TxEventQ topic through the OKafka admin client:

```java
NewTopic topic = new NewTopic("OKAFKA_SAMPLE", 1, (short) 1);
OKafkaUtil.createTopicIfNotExists(kafkaProperties, topic);
```

In production code, it is common to handle `TopicExistsException` so repeated startup does not fail when the topic already exists.

## Database Permissions

The database user needs the privileges required for TxEventQ and OKafka access. A representative setup looks like this:

```sql
grant aq_user_role to TESTUSER;
grant execute on dbms_aq to TESTUSER;
grant execute on dbms_aqadm to TESTUSER;
grant select on gv_$session to TESTUSER;
grant select on v_$session to TESTUSER;
grant select on gv_$instance to TESTUSER;
grant select on gv_$listener_network to TESTUSER;
grant select on SYS.DBA_RSRC_PLAN_DIRECTIVES to TESTUSER;
grant select on gv_$pdbs to TESTUSER;
grant select on user_queue_partition_assignment_table to TESTUSER;
exec dbms_aqadm.GRANT_PRIV_FOR_RM_PLAN('TESTUSER');
```

## When To Use OKafka

Use the OKafka starter when your application already follows Kafka client concepts such as producers, consumers, groups, serializers, and topic administration, but you want Oracle AI Database TxEventQ as the messaging backend.

Choose this starter when you want Kafka-style APIs directly. If you want higher-level Spring messaging abstractions instead, consider the AQ/JMS starter or the Spring Cloud Stream binder for TxEventQ.

## Learn by Example

See the sample application:

- [oracle-spring-boot-sample-okafka](https://github.com/oracle/spring-cloud-oracle/tree/main/database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-okafka)
