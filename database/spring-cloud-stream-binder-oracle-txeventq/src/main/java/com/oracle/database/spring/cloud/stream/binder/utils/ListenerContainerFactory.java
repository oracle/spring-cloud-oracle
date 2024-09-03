/*
 ** TxEventQ Support for Spring Cloud Stream
 ** Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 **
 ** This file has been modified by Oracle Corporation.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oracle.database.spring.cloud.stream.binder.utils;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class ListenerContainerFactory {
    private final ConnectionFactory factory;

    private static final Logger logger = LoggerFactory.getLogger(ListenerContainerFactory.class);

    public ListenerContainerFactory(ConnectionFactory factory) {
        this.factory = factory;
    }

    public AbstractMessageListenerContainer build(
            Destination topic,
            String group,
            int partition,
            int concurrency,
            boolean isBatched,
            int batchSize,
            int timeout
    ) {
        DefaultMessageListenerContainer listenerContainer = null;
        if (!isBatched) {
            listenerContainer = new TEQMessageListenerContainer();
            ((TEQMessageListenerContainer) listenerContainer).setPartition(partition);
        } else {
            listenerContainer = new TEQBatchMessageListenerContainer();
            ((TEQBatchMessageListenerContainer) listenerContainer).setPartition(partition);
            ((TEQBatchMessageListenerContainer) listenerContainer).setBatchSize(batchSize);
        }
        logger.info("Consuming from destination: {}, Group: {}", topic, group);
        listenerContainer.setDestination(topic);
        listenerContainer.setPubSubDomain(true);
        listenerContainer.setSubscriptionName(group);
        listenerContainer.setReceiveTimeout(timeout);

        listenerContainer.setConcurrentConsumers(concurrency);
        if (!group.contains("anonymous")) {
            listenerContainer.setSubscriptionDurable(true);
        }
        listenerContainer.setConnectionFactory(factory);
        listenerContainer.setSessionTransacted(true);
        return listenerContainer;
    }
}
