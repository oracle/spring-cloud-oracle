/*
 ** TxEventQ Support for Spring Cloud Stream
 ** Copyright (c) 2023, 2025 Oracle and/or its affiliates.
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

import java.sql.Connection;
import java.util.function.Consumer;

import com.oracle.database.spring.cloud.stream.binder.serialize.Serializer;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import oracle.jakarta.jms.AQjmsSession;
import oracle.jakarta.jms.AQjmsTopicConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.context.Lifecycle;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ErrorMessage;

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

    private static final Logger sendLogger = LoggerFactory.getLogger(PartitionAwareJmsSendingMessageHandler.class);

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
            this.handleJMSMessageInternal(message);
        } catch (Exception e) {
            sendLogger.error("An error occurred while trying to send message:", e);
            if (this.errorChannel != null) {
                this.errorChannel.send(new ErrorMessage(e, message));
            }
            throw e;
        }
    }

    protected void handleJMSMessageInternal(Message<?> message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        Object objectToSend = this.serializeMessageIfRequired(message.getPayload());

        Integer partitionToSend = getPartition(message);
        HeaderMappingMessagePostProcessor messagePostProcessor = new HeaderMappingMessagePostProcessor(
                message,
                this.headerMapper,
                partitionToSend,
                mapHeaders
        );
        messagePostProcessor.setDBVersion(this.dbversion);

        // try to read ConnectionCallback - if set
        @SuppressWarnings("unchecked")
        Consumer<Connection> connCallback = (Consumer<Connection>) message.getHeaders().get(TxEventQBinderHeaderConstants.CONNECTION_CONSUMER);
        if (connCallback == null) {
            this.jmsTemplate.convertAndSend(
                    destination,
                    objectToSend,
                    messagePostProcessor
            );
            return;
        }

        Connection c = (Connection) message.getHeaders().get(TxEventQBinderHeaderConstants.MESSAGE_CONTEXT);
        if (c == null) {
            final Object actualPayload = objectToSend;
            jmsTemplate.send(destination, session -> {
                Connection sessionConnection = ((AQjmsSession) session).getDBConnection();
                connCallback.accept(sessionConnection);
                MessageConverter msgConverter = this.jmsTemplate.getMessageConverter();
                if (msgConverter == null) {
                    throw new IllegalStateException("No 'messageConverter' specified. Check configuration of JmsTemplate.");
                }
                jakarta.jms.Message msg = msgConverter.toMessage(actualPayload, session);
                return messagePostProcessor.postProcessMessage(msg);
            });
        } else {
            connCallback.accept(c);
            // create topic connection and session using c
            try (jakarta.jms.Connection conn = AQjmsTopicConnectionFactory.createTopicConnection(c);
                 Session s = conn.createSession(true, this.jmsTemplate.getSessionAcknowledgeMode());
                 MessageProducer p = s.createProducer(destination)) {
                MessageConverter msgConverter = this.jmsTemplate.getMessageConverter();
                if (msgConverter == null) {
                    throw new IllegalStateException("No 'messageConverter' specified. Check configuration of JmsTemplate.");
                }
                jakarta.jms.Message msg = msgConverter.toMessage(objectToSend, s);
                jakarta.jms.Message msgToSend = messagePostProcessor.postProcessMessage(msg);
                p.send(msgToSend);
                s.commit();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private Object serializeMessageIfRequired(Object objectToSend) {
        if (this.serializerClassName != null) {
            Class<?> serializer = null;
            try {
                serializer = Class.forName(this.serializerClassName);
            } catch (ClassNotFoundException ce) {
                sendLogger.debug("The class name: {} is invalid.", serializerClassName);
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
                sendLogger.debug("The configured serializer class is not an instance of 'com.oracle.cstream.serialize.Serializer'");
                throw new IllegalArgumentException("The configured serializer class is not an instance of 'com.oracle.cstream.serialize.Serializer'");
            }
            Serializer s = null;

            try {
                s = (Serializer) (serializer.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                sendLogger.debug("Serializer object could not be initiated.");
                throw new IllegalArgumentException("Serializer object could not be initiated.");
            }

            objectToSend = s.serialize(objectToSend);
        }
        return objectToSend;
    }

    private Integer getPartition(Message<?> message) {
        try {
            return (Integer) (message.getHeaders().get(BinderHeaders.PARTITION_HEADER));
        } catch (Exception e) {
            sendLogger.info("Invalid Partition Index");
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
                    jmsMessage.setLongProperty("AQINTERNAL_PARTITION", this.partition * 2L);
            } else {
                // choose 0 by default
                if (this.dbversion != 19)
                    jmsMessage.setLongProperty("AQINTERNAL_PARTITION", 0L);
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
