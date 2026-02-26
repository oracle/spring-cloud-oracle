/*
 ** TxEventQ Support for Spring Cloud Stream
 ** Copyright (c) 2023, 2024, 2026 Oracle and/or its affiliates.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.integration.core.RecoveryCallback;
import org.springframework.integration.jms.inbound.ChannelPublishingJmsMessageListener;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import com.oracle.database.spring.cloud.stream.binder.config.JmsConsumerProperties;
import com.oracle.database.spring.cloud.stream.binder.serialize.CustomSerializationMessageConverter;

import jakarta.jms.BytesMessage;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import oracle.jakarta.jms.AQjmsSession;

public class JmsMessageDrivenChannelAdapterFactory
        implements ApplicationContextAware, BeanFactoryAware {

    private final ListenerContainerFactory listenerContainerFactory;

    private final MessageRecoverer messageRecoverer;

    private BeanFactory beanFactory;

    private ApplicationContext applicationContext;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public JmsMessageDrivenChannelAdapterFactory(
            ListenerContainerFactory listenerContainerFactory,
            MessageRecoverer messageRecoverer
    ) {
        this.listenerContainerFactory = listenerContainerFactory;
        this.messageRecoverer = messageRecoverer;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    public JmsMessageDrivenChannelAdapter build(
            Destination destination,
            String groupName,
            RetryTemplate retryTemplate,
            RecoveryCallback<Object> recoveryCallback,
            MessageChannel errorChannel,
            final ExtendedConsumerProperties<JmsConsumerProperties> properties,
            int dbversion
    ) {
        ChannelPublishingJmsMessageListener listener;
        if (!properties.isBatchMode()) {
        	RetryingChannelPublishingJmsMessageListener retryingListener = new RetryingChannelPublishingJmsMessageListener(
                    properties,
                    messageRecoverer,
                    properties.getExtension().getDlqName(),
                    retryTemplate,
                    recoveryCallback
            );
            if (properties.isUseNativeDecoding()) {
            	retryingListener.setDeSerializerClassName(properties.getExtension().getDeSerializer());
            }
            listener = retryingListener;
        } else {
        	 TEQBatchMessageListener batchListener  = new TEQBatchMessageListener();
        	 batchListener.setRetryTemplate(retryTemplate);
        	 batchListener.setRecoverer(recoveryCallback);
        	 batchListener.setDeSerializerClassName(properties.getExtension().getDeSerializer());
        	 listener = batchListener;
        }
        
        listener.setBeanFactory(this.beanFactory);

        if (dbversion == 19 && properties.getInstanceIndex() != -1) {
            logger.warn("Exact dequeue from a specific instanceIndex is not supported in Oracle Database version 19c. "
                    + "Please use Oracle DB version 23c if you want to perform exact dequeue from a partition.");
        }

        JmsMessageDrivenChannelAdapter adapter = new JmsMessageDrivenChannelAdapter(
                listenerContainerFactory.build(destination,
                        groupName,
                        properties.getInstanceIndex(),
                        properties.getConcurrency(),
                        properties.isBatchMode(),
                        properties.getExtension().getBatchSize(),
                        properties.getExtension().getTimeout()),
                listener
        );
        adapter.setApplicationContext(this.applicationContext);
        adapter.setBeanFactory(this.beanFactory);
        adapter.setErrorChannel(errorChannel);
        return adapter;
    }

    private static class RetryingChannelPublishingJmsMessageListener
            extends ChannelPublishingJmsMessageListener {

        private static final String RETRY_CONTEXT_MESSAGE_ATTRIBUTE = "message";

        private final ConsumerProperties properties;

        private final MessageRecoverer messageRecoverer;

        private final String deadLetterQueueName;

        private final RetryTemplate retryTemplate;

        private RecoveryCallback<Object> recoverer;

        private String deSerializerClassName = null;

        SpecCompliantJmsHeaderMapper headerMapper = new SpecCompliantJmsHeaderMapper();

        RetryingChannelPublishingJmsMessageListener(
                ConsumerProperties properties,
                MessageRecoverer messageRecoverer,
                String deadLetterQueueName,
                RetryTemplate retryTemplate,
                RecoveryCallback<Object> recoverer
        ) {
            this.properties = properties;
            this.messageRecoverer = messageRecoverer;
            this.deadLetterQueueName = deadLetterQueueName;
            this.retryTemplate = retryTemplate;
            this.recoverer = recoverer;
        }

        public void setDeSerializerClassName(String deSerializerClassName) {
            this.deSerializerClassName = deSerializerClassName;
        }

        @Override
        public void onMessage(final Message jmsMessage, final Session session)
                throws JMSException {
        	
        	// Maintain a reference to the converter being used.
            // If none is set, Spring Integration defaults to SimpleMessageConverter.
            final MessageConverter converter = (deSerializerClassName != null) ? new CustomSerializationMessageConverter() : new SimpleMessageConverter();
            
            if (deSerializerClassName != null) {
                ((CustomSerializationMessageConverter)converter).setDeserializer(deSerializerClassName);
                this.setMessageConverter(converter);
            }
            
        	try {
                // Spring 7 RetryTemplate.execute now takes a single Retryable lambda
                this.retryTemplate.execute(() -> {
                    // Oracle AQ Connection Setup
                    if (session instanceof AQjmsSession aqSession) {
                    	headerMapper.setConnection(aqSession.getDBConnection());
                    }

                    // Delegate to the superclass implementation (ChannelPublishingJmsMessageListener)
                    super.setHeaderMapper(headerMapper);
                    super.onMessage(jmsMessage, session);
                        
                    return null; 
                });
            } catch (Exception e) {
            	// Final failure: handle recovery
                resetMessageIfRequired(jmsMessage);
                
                // Exhausted Retries: This catch block replaces the old RecoveryCallback
                // The native RetryException is thrown when max attempts are reached.
                if (this.recoverer != null) {
                    try {
                    	 // Convert JMS payload to Object
                        Object payload = converter.fromMessage(jmsMessage);

                        // Map JMS headers to Spring MessageHeaders
                        MessageHeaders headers = new MessageHeaders(headerMapper.toHeaders(jmsMessage));

                        // Create the ACTUAL Spring Message that ErrorMessagePublisher expects
                        org.springframework.messaging.Message<?> springMessage = MessageBuilder.createMessage(payload, headers);
                        
                    	 // Use a simple anonymous implementation of AttributeAccessor
                        AttributeAccessor context = new AttributeAccessor() {
                            private final java.util.Map<String, Object> attrs = new java.util.HashMap<>();
                            @Override public void setAttribute(String n, Object v) { attrs.put(n, v); }
                            @Override public Object getAttribute(String n) { return attrs.get(n); }
                            @Override public Object removeAttribute(String n) { return attrs.remove(n); }
                            @Override public boolean hasAttribute(String n) { return attrs.containsKey(n); }
                            @Override public String[] attributeNames() { return attrs.keySet().toArray(new String[0]); }
                        };

                        // Put the messages in so the recoverer can find them
                        context.setAttribute(RETRY_CONTEXT_MESSAGE_ATTRIBUTE, springMessage);

                        // Pass the context and the actual exception (e) to the recoverer
                        this.recoverer.recover(context, e); 
                    } catch (Exception ex) {
                        logger.error(ex, "Recovery failed for TxEventQ message");
                        throw new JMSException("Recovery failed: " + ex.getMessage());
                    }
                } else {
                    logger.error("Retries exhausted and no recoverer configured for message: " + jmsMessage);
                    throw new JMSException("Retry attempts exhausted");
                }
            }
        }

        protected void resetMessageIfRequired(Message jmsMessage)
                throws JMSException {
            if (jmsMessage instanceof BytesMessage message) {
                message.reset();
            }
        }
    }
}
