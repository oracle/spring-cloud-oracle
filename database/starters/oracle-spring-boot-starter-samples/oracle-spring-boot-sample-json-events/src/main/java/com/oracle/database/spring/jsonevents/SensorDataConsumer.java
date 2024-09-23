// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.time.Duration;
import java.util.List;

import com.oracle.database.spring.jsonevents.model.SensorData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.oracle.okafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SensorDataConsumer implements Runnable, AutoCloseable {
    private final KafkaConsumer<String, SensorData> consumer;
    private final String topic;
    private final SensorDataEnricher sensorDataEnricher;
    private final SensorDataService sensorDataService;

    public SensorDataConsumer(@Qualifier("sensorDataConsumer") KafkaConsumer<String, SensorData> consumer,
                              @Value("${app.topic}") String topic,
                              SensorDataEnricher sensorDataEnricher,
                              SensorDataService sensorDataService) {
        this.consumer = consumer;
        this.topic = topic;
        this.sensorDataEnricher = sensorDataEnricher;
        this.sensorDataService = sensorDataService;
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(topic));
        while (true) {
            ConsumerRecords<String, SensorData> records = consumer.poll(Duration.ofMillis(100));
            processRecords(records);
            // Commit records when done processing.
            consumer.commitAsync();
        }
    }

    private void processRecords(ConsumerRecords<String, SensorData> records) {
        for (ConsumerRecord<String, SensorData> record : records) {
            SensorData sensorData = record.value();
            // Add weather station data to the event
            SensorData enriched = sensorDataEnricher.enrich(sensorData);
            // Persist the event
            sensorDataService.save(enriched);
        }
    }

    @Override
    public void close() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }
}
