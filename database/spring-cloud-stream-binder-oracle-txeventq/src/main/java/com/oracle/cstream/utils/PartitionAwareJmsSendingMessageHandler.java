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

package com.oracle.cstream.utils;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.context.Lifecycle;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ErrorMessage;

import com.oracle.cstream.serialize.Serializer;

public class PartitionAwareJmsSendingMessageHandler
        extends AbstractMessageHandler
        implements Lifecycle {

    private final JmsTemplate jmsTemplate;

    private final Destination destination;

    private final JmsHeaderMapper headerMapper;

    private final MessageChannel errorChannel;

    private boolean mapHeaders = true;

    private String serializerClassName = null;

    private int dbversion = 23;

    private static final Logger logger = LoggerFactory.getLogger(PartitionAwareJmsSendingMessageHandler.class);

    public PartitionAwareJmsSendingMessageHandler(
            JmsTemplate jmsTemplate,
            Destination destination,
            JmsHeaderMapper headerMapper,
            MessageChannel errorChannel,
            boolean mapHeaders
    ) {
        this.jmsTemplate = jmsTemplate;
        this.destination = destination;
        this.headerMapper = headerMapper;
        this.errorChannel = errorChannel;
        this.mapHeaders = mapHeaders;
    }

    public void setSerializerClassName(String serializerClassName) {
        this.serializerClassName = serializerClassName;
    }

    public void setDBVersion(int dbversion) {
        this.dbversion = dbversion;
    }

    protected void handleMessageInternal(Message<?> message) {
        try {
            this.jmsHandleMessageInternal(message);
        } catch (Exception e) {
            logger.error("An error occurred while trying to send message: " + e);
            if (this.errorChannel != null) {
                this.errorChannel.send(new ErrorMessage(e, message));
            }
            throw e;
        }
    }

    protected void jmsHandleMessageInternal(Message<?> message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        Object objectToSend = message.getPayload();

        if (this.serializerClassName != null) {
            Class<?> serializer = null;
            try {
                serializer = Class.forName(this.serializerClassName);
            } catch (ClassNotFoundException ce) {
                logger.debug("The class name: " + serializerClassName + "is invalid.");
                throw new IllegalArgumentException(ce.getMessage());
            }

            boolean isInstanceOfSerializer = false;
            for (Class<?> inter_face : serializer.getInterfaces()) {
                if (inter_face.toString().equals(Serializer.class.toString())) {
                    isInstanceOfSerializer = true;
                    break;
                }
            }

            if (!isInstanceOfSerializer) {
                logger.debug("The configured serializer class is not an instance of 'com.oracle.cstream.serialize.Serializer'");
                throw new IllegalArgumentException("The configured serializer class is not an instance of 'com.oracle.cstream.serialize.Serializer'");
            }
            Serializer s = null;

            try {
                s = (Serializer) (serializer.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                logger.debug("Serializer object could not be initiated.");
                throw new IllegalArgumentException("Serializer object could not be initiated.");
            }

            objectToSend = s.serialize(objectToSend);
        }

        Integer partitionToSend = getPartition(message);
        HeaderMappingMessagePostProcessor messagePostProcessor = new HeaderMappingMessagePostProcessor(
                message,
                this.headerMapper,
                partitionToSend,
                mapHeaders
        );
        messagePostProcessor.setDBVersion(this.dbversion);

        this.jmsTemplate.convertAndSend(
                destination,
                objectToSend,
                messagePostProcessor
        );


    }

    private Integer getPartition(Message<?> message) {
        try {
            return (Integer) (message.getHeaders().get(BinderHeaders.PARTITION_HEADER));
        } catch (Exception e) {
            logger.info("Invalid Partition Index");
            throw new IllegalArgumentException("The partition index cannot be converted to an integer");
        }
    }

    private static final class HeaderMappingMessagePostProcessor
            implements MessagePostProcessor {

        private final Message<?> integrationMessage;
        private final JmsHeaderMapper headerMapper;
        private final Integer partition;
        private final boolean mapHeaders;
        private int dbversion = 23;

        private HeaderMappingMessagePostProcessor(
                Message<?> integrationMessage,
                JmsHeaderMapper headerMapper,
                Integer pNum,
                boolean mapHeaders
        ) {
            this.integrationMessage = integrationMessage;
            this.headerMapper = headerMapper;
            this.partition = pNum;
            this.mapHeaders = mapHeaders;
        }

        public void setDBVersion(int dbversion) {
            this.dbversion = dbversion;
        }

        public jakarta.jms.Message postProcessMessage(
                jakarta.jms.Message jmsMessage
        ) throws JMSException {
            if (this.mapHeaders) {
                this.headerMapper.fromHeaders(
                        this.integrationMessage.getHeaders(),
                        jmsMessage
                );
            }

            // set partition property if not null
            if (this.partition != null) {
                if (this.dbversion == 19)
                    jmsMessage.setJMSCorrelationID("" + this.partition);
                else
                    jmsMessage.setLongProperty("AQINTERNAL_PARTITION", this.partition * 2);
            } else {
                // choose 0 by default
                if (this.dbversion != 19)
                    jmsMessage.setLongProperty("AQINTERNAL_PARTITION", 0);
            }

            return jmsMessage;
        }
    }

    /*
      TODO: This has to be re factored, there is an open issue https://github.com/spring-cloud/spring-cloud-stream/issues/607
      that requires some love first
       */
    private boolean running;

    @Override
    public synchronized void start() {
        running = true;
    }

    @Override
    public synchronized void stop() {
        running = false;
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }
}
