// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.time.Duration;
import java.util.List;

import com.oracle.database.spring.jsonevents.model.Sensor;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SensorConsumer implements Runnable, AutoCloseable {
    private final Consumer<String, Sensor> consumer;
    private final String topic;
    private final SensorEnricher sensorEnricher;
    private final SensorService sensorService;
    // Used to end the consumer for example/testing purposes
    private final int limit;

    public SensorConsumer(@Qualifier("okafkaConsumer") Consumer<String, Sensor> consumer,
                          @Value("${app.topic:weathersensor}") String topic,
                          @Value("${app.consumer.limit:15}") int limit,
                          SensorEnricher sensorEnricher,
                          SensorService sensorService) {
        this.consumer = consumer;
        this.topic = topic;
        this.limit = limit;
        this.sensorEnricher = sensorEnricher;
        this.sensorService = sensorService;

    }

    @Override
    public void run() {
        consumer.subscribe(List.of(topic));
        int consumedRecords = 0;
        while (true) {
            ConsumerRecords<String, Sensor> records = consumer.poll(Duration.ofMillis(100));
            processRecords(records);
            // Commit records when done processing.
            consumer.commitSync();
            // End the consumed once we have consumed all records.
            consumedRecords += records.count();
            if (consumedRecords >= limit) {
                return;
            }
        }
    }

    private void processRecords(ConsumerRecords<String, Sensor> records) {
        for (ConsumerRecord<String, Sensor> record : records) {
            Sensor sensor = record.value();
            // Add weather station data to the event
            Sensor enriched = sensorEnricher.enrich(sensor);
            // Persist the event
            sensorService.save(enriched);
        }
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }
}
