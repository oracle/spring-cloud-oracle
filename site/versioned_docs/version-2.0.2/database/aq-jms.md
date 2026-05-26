---
title: AQ/JMS
sidebar_position: 3
---

# Spring JMS with TxEventQ

The AQ/JMS starter provides Spring Boot support for Oracle Transactional Event Queues (TxEventQ) as a [Spring JMS provider](https://spring.io/guides/gs/messaging-jms).

## Dependency Coordinates

To use Oracle AI Database for JMS, include the oracle-spring-boot-starter-aqjms and spring-boot-starter-jdbc dependencies in your project:

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-starter-aqjms</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

## Database Permissions

The database JMS user should have at least the following permissions to produce and consume messages with Oracle AI Database:

```sql
grant aq_user_role to testuser;
grant execute on dbms_aq to testuser;
grant execute on dbms_aqadm to testuser;
grant execute ON dbms_aqin TO testuser;
grant execute ON dbms_aqjms TO testuser;
```

## Create a JMS Queue or Topic

JMS applications must have an existing queue or topic. You may create this from your application code, or do so with a PL/SQL statement. The following snippet creates a JMS queue named `MY_JMS_QUEUE` in the `TESTUSER` schema:

```sql
begin
    dbms_aqadm.create_transactional_event_queue(
            queue_name         => 'TESTUSER.MY_JMS_QUEUE',
            -- False -> JMS Queue. True -> JMS Topic
            multiple_consumers => false
    );

    -- start the TEQ
    dbms_aqadm.start_queue(
            queue_name         => 'TESTUSER.MY_JMS_QUEUE'
    );
end;
/
```

## Database Connection

JMS uses a standard Spring Boot datasource JDBC connection. Oracle AI Database JMS producers and consumers will use autowired Spring JDBC datasource:

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

## Produce and consume messages

You can use Spring JMS abstractions like `JMSTemplate`, `JMSListener`, and `JMSClient` in your Spring Boot applications with Oracle AI Database.

### Producer Example with `JMSTemplate`

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.jms.core.JmsTemplate;

@Service
public class Producer {

  private static final Logger log = LoggerFactory.getLogger(Producer.class);

  JmsTemplate jmsTemplate;

  @Value("${txeventq.topic.name}")
  private String topic;

  public Producer(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  public void sendMessageToTopic(String message)
  {
    jmsTemplate.convertAndSend(topic,message);
    log.info("Sending message: {} to topic {}", message, topic);
  }
}
```

### Consumer Example with `@JMSListener`

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

  private static final Logger log = LoggerFactory.getLogger(Consumer.class);

  @JmsListener(destination = "${txeventq.topic.name}")
  public void receiveMessage(String message) {
    log.info("Received message: {}", message);
  }
}
```