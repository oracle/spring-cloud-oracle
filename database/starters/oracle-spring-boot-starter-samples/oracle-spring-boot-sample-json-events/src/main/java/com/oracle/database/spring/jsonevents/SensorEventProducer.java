// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import com.oracle.database.spring.jsonevents.model.Sensor;
import com.oracle.database.spring.jsonevents.model.SensorEvent;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The SensorEventProducer
 */
@Service
public class SensorEventProducer implements AutoCloseable {
    private final Producer<String, Sensor> producer;
    private final String topic;
    private final SensorEventParser sensorEventParser;

    public SensorEventProducer(@Qualifier("okafkaProducer") Producer<String, Sensor> producer,
                               @Value("${app.topic}") String topic,
                               SensorEventParser sensorEventParser) {
        this.producer = producer;
        this.topic = topic;
        this.sensorEventParser = sensorEventParser;
    }

    public void send(SensorEvent event) {
        for (Sensor sensor : sensorEventParser.parse(event)) {
            ProducerRecord<String, Sensor> record = new ProducerRecord<>(topic, sensor);
            producer.send(record);
        }
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        if (producer != null) {
            producer.close();
        }
    }
}
