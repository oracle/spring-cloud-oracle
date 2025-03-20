// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.okafka;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.Consumer;
import org.oracle.okafka.clients.consumer.KafkaConsumer;
import org.oracle.okafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OKafkaConfiguration {
    public static final String TOPIC_NAME = "OKAFKA_SAMPLE";

    @Value("${ojdbc.path}")
    private String ojdbcPath;

    @Value("${bootstrap.servers}")
    private String bootstrapServers;

    // We use the default 23ai Free service name
    @Value("${service.name:freepdb1}")
    private String serviceName;

    // We use plaintext for a containerized, local database.
    // Use SSL for wallet connections, like Autonomous Database.
    @Value("${security.protocol:PLAINTEXT}")
    private String securityProtocol;

    @Value("${producer.stream.file}")
    private String producerFile;

    @Value("${expected.messages:50}")
    private int expectedMessages;


    @Bean
    @Qualifier("okafkaProperties")
    public Properties kafkaProperties() {
        return OKafkaUtil.getConnectionProperties(ojdbcPath,
                bootstrapServers,
                securityProtocol,
                serviceName);
    }

    @Bean
    @Qualifier("sampleProducer")
    public SampleProducer<String> sampleProducer() throws IOException {
        // Create the OKafka Producer.
        Properties props = kafkaProperties();
        props.put("enable.idempotence", "true");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // Note the use of the org.oracle.okafka.clients.producer.KafkaProducer class, for Oracle TxEventQ.
        KafkaProducer<String, String> okafkaProducer = new KafkaProducer<>(props);
        // We create a stream of a data from a file to give the producer input messages.
        Stream<String> producerData = Files.lines(new File(producerFile).toPath());
        return new SampleProducer<>(okafkaProducer, TOPIC_NAME, producerData);
    }

    @Bean
    @Qualifier("sampleConsumer")
    public SampleConsumer<String> sampleConsumer() {
        // Create the OKafka Consumer.
        Properties props = kafkaProperties();
        props.put("group.id" , "MY_CONSUMER_GROUP");
        props.put("enable.auto.commit","false");
        props.put("max.poll.records", 2000);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        // Note the use of the org.oracle.okafka.clients.producer.KafkaConsumer class, for Oracle TxEventQ.
        Consumer<String, String> okafkaConsumer = new KafkaConsumer<>(props);
        return new SampleConsumer<>(okafkaConsumer, TOPIC_NAME, expectedMessages);
    }
}
