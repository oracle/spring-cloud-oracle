// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.okafka;

import java.util.stream.Stream;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * The SampleProducer reads from a given Stream, and writes each message to a topic.
 * Once the stream is complete, the producer exits.
 *
 * @param <T> message type
 */
public class SampleProducer<T> implements Runnable, AutoCloseable {
    private final Producer<String, T> producer;
    private final String topic;
    private final Stream<T> inputs;

    public SampleProducer(Producer<String, T> producer, String topic, Stream<T> inputs) {
        this.producer = producer;
        this.topic = topic;
        this.inputs = inputs;
    }

    @Override
    public void run() {
        inputs.forEach(t -> {
            System.out.println("Produced record: " + t);
            producer.send(new ProducerRecord<>(topic, t));
        });
    }

    @Override
    public void close() throws Exception {
        if (this.producer != null) {
            producer.close();
        }
    }
}