---
title: Get Started
sidebar_position: 2
---

In this section, we’ll walk through how to get started with the Spring Cloud Stream binder for Oracle AI Database TxEventQ.

A end-to-end sample for the binder can be found here: [TxEventQ Stream Binder Sample Application](https://github.com/oracle/spring-cloud-oracle/tree/main/database/spring-cloud-stream-binder-oracle-txeventq/spring-cloud-stream-binder-txeventq-sample)

## Dependency Coordinates

The binder JAR is available on Maven central, and may be added to your POM like so:

```xml
<dependency>
    <groupId>com.oracle.database.spring.cloud-stream-binder</groupId>
    <artifactId>spring-cloud-stream-binder-oracle-txeventq</artifactId>
</dependency>
```

## Database Permissions

The database user producing/consuming events with the stream binder for TxEventQ requires the following database permissions:

```sql
grant execute on dbms_aq to testuser;
grant execute on dbms_aqadm to testuser;
grant execute on dbms_aqin to testuser;
grant execute on dbms_aqjms_internal to testuser;
grant execute on DBMS_RESOURCE_MANAGER to testuser;
grant select on sys.aq$_queue_shards to testuser;
grant select on user_queue_partition_assignment_table to testuser;
```

## Database Connection

The stream binder uses a standard Spring datasource JDBC connection to stream events. With YAML properties, it should look something like this:

```yaml
spring:
  datasource:
    username: ${USERNAME}
    password: ${PASSWORD}
    url: ${JDBC_URL}
    driver-class-name: oracle.jdbc.OracleDriver
    type: oracle.ucp.jdbc.PoolDataSourceImpl
    oracleucp:
      initial-pool-size: 1
      min-pool-size: 1
      max-pool-size: 30
      connection-pool-name: TxEventQSample
      connection-factory-class-name: oracle.jdbc.pool.OracleDataSource
```

## Implementing Producers and Consumers

With Spring Cloud Stream, the producer/consumer code abstracts away references to TxEventQ, using pure Spring APIs to produce and consume messages.

Let’s start by implementing a producer that returns a phrase word-by-word, indicating when it has processed the whole phrase.

```java
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class WordSupplier implements Supplier<String> {
private final String[] words;
private final AtomicInteger idx = new AtomicInteger(0);
private final AtomicBoolean done = new AtomicBoolean(false);

    public WordSupplier(String phrase) {
        this.words = phrase.split(" ");
    }

    @Override
    public String get() {
        int i = idx.getAndAccumulate(words.length, (x, y) -> {
            if (x < words.length - 1) {
                return x + 1;
            }
            done.set(true);
            return 0;
        });
        return words[i];
    }

    public boolean done() {
        return done.get();
    }
}
```

Next, let’s add Spring beans for producers and consumers, including our WordSupplier and two more simple functional interfaces.

```java
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamConfiguration {
private @Value("${phrase}") String phrase;

    @Bean
    public Function<String, String> toUpperCase() {
        return String::toUpperCase;
    }

    @Bean
    public Consumer<String> stdoutConsumer() {
        return s -> System.out.println("Consumed: " + s);
    }

    @Bean
    public WordSupplier wordSupplier() {
        return new WordSupplier(phrase);
    }
}
```

## Wire up producers and consumer bindings with Spring Cloud Stream

Let’s configure the Spring Cloud Stream bindings.

In our binding configuration, the wordSupplier producer has toUpperCase as a destination, and the stdoutConsumer consumer reads from toUpperCase. The result of this acyclic producer-consumer configuration is that each word from the phrase is converted to uppercase and sent to stdout.

```yaml
phrase: "Spring Cloud Stream simplifies event-driven microservices with powerful messaging capabilities."

spring:
  cloud:
    stream:
      bindings:
        wordSupplier-out-0:
          destination: toUpperCase-in-0
          group: t1
          producer:
            required-groups:
              - t1
        stdoutConsumer-in-0:
          destination: toUpperCase-out-0
          group: t1
    function:
      definition: wordSupplier;toUpperCase;stdoutConsumer
```