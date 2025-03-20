// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.okafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import static com.oracle.database.spring.okafka.OKafkaConfiguration.TOPIC_NAME;

@Component
public class OKafkaComponent {
    private final AsyncTaskExecutor taskExecutor;
    private final Properties kafkaProperties;
    private final SampleProducer<String> sampleProducer;
    private final SampleConsumer<String> sampleConsumer;

    // The tasks list is used to track and wait for consumer/producer execution.
    private final List<Future<?>> tasks = new ArrayList<>();

    public OKafkaComponent(@Qualifier("applicationTaskExecutor") AsyncTaskExecutor taskExecutor,
                           @Qualifier("kafkaProperties") Properties kafkaProperties,
                           @Qualifier("sampleProducer") SampleProducer<String> sampleProducer,
                           @Qualifier("sampleConsumer") SampleConsumer<String> sampleConsumer) {
        this.taskExecutor = taskExecutor;
        this.kafkaProperties = kafkaProperties;
        this.sampleProducer = sampleProducer;
        this.sampleConsumer = sampleConsumer;
    }

    @PostConstruct
    public void init() {
        // Create a new TxEventQ topic
        NewTopic topic = new NewTopic(TOPIC_NAME, 1, (short) 1);
        OKafkaUtil.createTopicIfNotExists(kafkaProperties, topic);

        // Start the producer and consumer
        tasks.add(taskExecutor.submit(sampleProducer));
        tasks.add(taskExecutor.submit(sampleConsumer));
    }

    public void await() throws ExecutionException, InterruptedException {
        for (Future<?> task : tasks) {
            task.get();
        }
    }
}
