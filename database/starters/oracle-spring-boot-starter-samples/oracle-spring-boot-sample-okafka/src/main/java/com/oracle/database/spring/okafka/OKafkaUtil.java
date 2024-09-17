// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.okafka;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.oracle.okafka.clients.admin.AdminClient;
public class OKafkaUtil {
    public static Properties getConnectionProperties(String ojdbcPath,
                                                     String bootstrapServers,
                                                     String securityProtocol,
                                                     String serviceName)  {
        Properties props = new Properties();
        props.put("oracle.service.name", serviceName);
        props.put("security.protocol", securityProtocol);
        props.put("bootstrap.servers", bootstrapServers);
        // If using Oracle Database wallet, pass wallet directory
        props.put("oracle.net.tns_admin", ojdbcPath);
        return props;
    }

    public static void createTopicIfNotExists(Properties okafkaProperties,
                                              NewTopic newTopic) {
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
    }
}
