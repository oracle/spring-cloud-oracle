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

import org.springframework.jms.listener.DefaultMessageListenerContainer;
import oracle.jakarta.jms.AQjmsConsumer;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

public class TEQMessageListenerContainer extends DefaultMessageListenerContainer {

    /**
     * Instance variable to consume from a specific partition
     */
    private int partition = -1;

    /**
     * Getters and setters for partition
     */
    public int getPartition() {
        return this.partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    /**
     * Create a JMS MessageConsumer for the given Session and Destination.
     * <p>This implementation uses JMS 1.1 API.
     * Also sets the corresponding partition on AQjmsConsumer
     *
     * @param session     the JMS Session to create a MessageConsumer for
     * @param destination the JMS Destination to create a MessageConsumer for
     * @return the new JMS MessageConsumer
     * @throws jakarta.jms.JMSException if thrown by JMS API methods
     */
    @Override
    protected MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
        MessageConsumer consumer = super.createConsumer(session, destination);
        if (this.partition != -1)
            ((AQjmsConsumer) consumer).setPartition(this.partition);
        return consumer;
    }
}
