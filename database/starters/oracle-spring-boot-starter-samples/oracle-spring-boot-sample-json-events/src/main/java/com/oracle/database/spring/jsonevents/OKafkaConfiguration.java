// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.util.Properties;

import com.oracle.database.spring.jsonevents.model.Sensor;
import com.oracle.spring.json.kafka.OSONKafkaSerializationFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.oracle.okafka.clients.consumer.KafkaConsumer;
import org.oracle.okafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OKafkaConfiguration configures the OKafka properties, producer, and consumer beans.
 */
@Configuration
public class OKafkaConfiguration {

    @Value("${app.ojdbcPath}")
    private String ojdbcPath;

    @Value("${app.bootstrapServers}")
    private String bootstrapServers;

    // We use the default 23ai Free service name
    @Value("${app.serviceName:freepdb1}")
    private String serviceName;

    // We use plaintext for a containerized, local database.
    // Use SSL for wallet connections, like Autonomous Database.
    @Value("${app.securityProtocol:PLAINTEXT}")
    private String securityProtocol;

    @Value("${app.consumerGroup:SensorEvents}")
    private String consumerGroup;

    @Bean
    @Qualifier("okafkaProperties")
    public Properties okafkaProperties() {
        Properties props = new Properties();
        props.put("oracle.service.name", serviceName);
        props.put("security.protocol", securityProtocol);
        props.put("bootstrap.servers", bootstrapServers);
        // If using Oracle Database wallet, pass wallet directory
        props.put("oracle.net.tns_admin", ojdbcPath);
        return props;
    }

    @Bean
    @Qualifier("okafkaConsumer")
    public Consumer<String, Sensor> okafkaConsumer(OSONKafkaSerializationFactory osonKafkaSerializationFactory) {
        Properties props = okafkaProperties();
        props.put("group.id", consumerGroup);
        props.put("enable.auto.commit","false");
        props.put("max.poll.records", 2000);
        props.put("auto.offset.reset", "earliest");

        Deserializer<String> keyDeserializer = new StringDeserializer();
        Deserializer<Sensor> valueDeserializer = osonKafkaSerializationFactory.createDeserializer(Sensor.class);
        return new KafkaConsumer<>(props, keyDeserializer, valueDeserializer);
    }

    @Bean
    @Qualifier("okafkaProducer")
    public Producer<String, Sensor> okafkaProducer(OSONKafkaSerializationFactory osonKafkaSerializationFactory) {
        Properties props = okafkaProperties();
        props.put("enable.idempotence", "true");

        Serializer<String> keySerializer = new StringSerializer();
        Serializer<Sensor> valueSerializer = osonKafkaSerializationFactory.createSerializer();
        return new KafkaProducer<>(props, keySerializer, valueSerializer);
    }
}
