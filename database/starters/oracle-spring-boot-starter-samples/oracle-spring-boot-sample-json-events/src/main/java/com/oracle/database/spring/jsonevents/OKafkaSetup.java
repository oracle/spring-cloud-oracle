// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.oracle.okafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * OKafkaSetup creates the app's OKafka topic, and starts the consumer thread.
 */
@Configuration
public class OKafkaSetup {
    private final AsyncTaskExecutor asyncTaskExecutor;
    private final SensorConsumer sensorConsumer;
    private final Properties okafkaProperties;

    @Value("${app.topic}")
    private String topic;

    public OKafkaSetup(@Qualifier("applicationTaskExecutor") AsyncTaskExecutor asyncTaskExecutor,
                       SensorConsumer sensorConsumer,
                       @Qualifier("okafkaProperties") Properties okafkaProperties) {
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.sensorConsumer = sensorConsumer;
        this.okafkaProperties = okafkaProperties;
    }

    @PostConstruct
    void init() {
        NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
        try (Admin admin = AdminClient.create(okafkaProperties)) {
            admin.createTopics(Collections.singletonList(newTopic))
                    .all()
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof TopicExistsException) {
                System.out.println("Topic already exists, skipping creation");
            } else {
                throw new RuntimeException(e);
            }
        }
        asyncTaskExecutor.submit(sensorConsumer);
    }
}
