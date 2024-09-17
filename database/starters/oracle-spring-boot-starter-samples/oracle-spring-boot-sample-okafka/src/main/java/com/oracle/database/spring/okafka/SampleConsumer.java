// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.okafka;

import java.time.Duration;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

/**
 * The SampleConsumer polls from a given topic, until it reads at least expectedMessages from that topic.
 * After the expected number of messages have been read, the consumer exits.
 *
 * @param <T> message type
 */
public class SampleConsumer<T> implements Runnable, AutoCloseable {
    private final Consumer<String, T> consumer;
    private final String topic;
    private final int expectedMessages;

    public SampleConsumer(Consumer<String, T> consumer, String topic, int expectedMessages) {
        this.consumer = consumer;
        this.topic = topic;
        this.expectedMessages = expectedMessages;
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(topic));
        int consumedRecords = 0;
        while (true) {
            ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(100));
            System.out.println("Consumed records: " + records.count());
            consumedRecords += records.count();
            if (consumedRecords >= expectedMessages) {
                return;
            }
            processRecords(records);
            // Commit records when done processing.
            consumer.commitAsync();
        }
    }

    private void processRecords(ConsumerRecords<String, T> records) {
        // Application implementation of record processing.
    }

    @Override
    public void close() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }
}