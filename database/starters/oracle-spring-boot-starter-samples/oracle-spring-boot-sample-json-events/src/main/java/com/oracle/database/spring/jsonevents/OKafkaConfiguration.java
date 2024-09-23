// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.util.Properties;

import com.oracle.database.spring.jsonevents.model.SensorData;
import jakarta.annotation.PostConstruct;
import org.oracle.okafka.clients.consumer.KafkaConsumer;
import org.oracle.okafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @Qualifier("sensorDataConsumer")
    public KafkaConsumer<String, SensorData> sensorDataConsumer() {
        Properties props = okafkaProperties();

        return new KafkaConsumer<>(props);
    }

    @Bean
    @Qualifier("sensorDataProducer")
    public KafkaProducer<String, SensorData> sensorDataProducer() {
        Properties props = okafkaProperties();

        return new KafkaProducer<>(props);
    }

    @PostConstruct
    void initOKafka() {

    }
}
