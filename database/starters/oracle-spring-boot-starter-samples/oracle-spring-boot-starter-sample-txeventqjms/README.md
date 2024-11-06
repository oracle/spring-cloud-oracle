# Oracle Transactional Event Queues (TxEventQ) Producer and Consumer Example

[Transactional Event Queues (TxEventQ)](https://docs.oracle.com/en/database/oracle/oracle-database/23/adque/aq-introduction.html) is a messaging platform built into Oracle Database that is used for application workflows, microservices, and event-triggered actions.

This sample demonstrates how to use [Transactional Event Queues (TxEventQ)](https://docs.oracle.com/en/database/oracle/oracle-database/23/adque/aq-introduction.html) and JMS using the Oracle Spring Boot Starter AQJMS. The sample has a Consumer and a Producer application. The Producer application puts messages on a TxEventQ and the Consumer application listens on the TxEventQ using a JMSListener.

## Install an Oracle Database 23ai

Install an Oracle Database 23ai. This sample is using a Docker Container but any Oracle Database 23ai will work. Install and start the database using the following command:

```shell
docker run --name free23ai -d -p 1521:1521 -e ORACLE_PWD=Welcome12345 \
container-registry.oracle.com/database/free:latest-lite
```

## Create user and the Transactional Event Queue (TxEventQ)

Log into the database as the `SYS` user and run the SQL script `setup.sql` located in the `sql` directory. The script creates a user called `TESTUSER` and a TxEventQ called `my_txeventq`.

## Start the Consumer application

Open a terminal window and go to the `consumer` directory and run `mvn spring-boot:run`. This will start the Consumer application that will listen to the TxEventQ `my_txeventq`.

## Start the Producer application

Open a terminal window and go to the `producer` directory and run `mvn spring-boot:run`. This will start the Producer application which will send messaged to the TxEventQ `my_txeventq`.

## Send a message

Open a terminal window and execute the following command to send a message via the Producer `/api/v1/message` endpoint:

```shell
curl -X POST http://localhost:8080/api/v1/message?message=MyMessage
```

The command returns the message `Message was sent successfully`.

In the Producer terminal window you will get a notification that looks like this:

```log
2024-11-01T14:07:40.549-05:00  INFO 94573 --- [producer] [nio-8080-exec-2] c.o.d.s.t.producer.service.Producer      : Sending message: MyMessage to topic my_txeventq
```

In the Consumer terminal window you will get a notification that looks like this:

```log
2024-11-01T14:07:40.548-05:00  INFO 94524 --- [consumer] [ntContainer#0-1] c.o.d.s.t.consumer.service.Consumer      : Received message: MyMessage
```

## Configure your project to use Oracle Spring Boot Starter for TxEventQ and JMS

To use Oracle Spring Boot Starter for TxEventQ and JMS for your Spring Boot application, add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-aqjms</artifactId>
</dependency>
```

or if you are using Gradle:

```text
implementation 'com.oracle.database.spring:oracle-spring-boot-starter-aqjms:24.3.0'
```
