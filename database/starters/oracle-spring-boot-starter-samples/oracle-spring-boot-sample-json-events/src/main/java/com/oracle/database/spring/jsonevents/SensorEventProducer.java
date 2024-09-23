// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import com.oracle.database.spring.jsonevents.model.SensorData;
import com.oracle.database.spring.jsonevents.model.SensorEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.oracle.okafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The SensorEventProducer
 */
@Service
public class SensorEventProducer {
    private final KafkaProducer<String, SensorData> producer;
    private final String topic;
    private final SensorEventParser sensorEventParser;

    public SensorEventProducer(@Qualifier("sensorDataProducer") KafkaProducer<String, SensorData> producer,
                               @Value("${app.topic}") String topic,
                               SensorEventParser sensorEventParser) {
        this.producer = producer;
        this.topic = topic;
        this.sensorEventParser = sensorEventParser;
    }

    public void send(SensorEvent event) {
        for (SensorData sensorData : sensorEventParser.parse(event)) {
            ProducerRecord<String, SensorData> record = new ProducerRecord<>(topic, sensorData);
            producer.send(record);
        }
    }


}
