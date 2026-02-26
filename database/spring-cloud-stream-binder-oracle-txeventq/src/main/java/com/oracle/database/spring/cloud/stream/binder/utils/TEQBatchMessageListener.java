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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oracle.database.spring.cloud.stream.binder.serialize.CustomSerializationMessageConverter;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.jms.inbound.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.DefaultJmsHeaderMapper;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.integration.support.utils.IntegrationUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.integration.core.RecoveryCallback;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.AttributeAccessor;

public class TEQBatchMessageListener extends ChannelPublishingJmsMessageListener {
    private final GatewayDelegate gatewayDelegate = new GatewayDelegate();

    private boolean expectReply;

    private MessageConverter messageConverter = new SimpleMessageConverter();

    private boolean extractRequestPayload = true;

    private JmsHeaderMapper headerMapper = new DefaultJmsHeaderMapper();

    private BeanFactory beanFactory;

    private MessageBuilderFactory messageBuilderFactory = new DefaultMessageBuilderFactory();

    private static final String TEQ_BATCHED_HEADERS = "teq_batched_headers";

    private RetryTemplate retryTemplate;

    private RecoveryCallback<Object> recoverer;

    private static final String RETRY_CONTEXT_MESSAGE_ATTRIBUTE = "message";

    private String deSerializerClassName = null;

    public String getDeSerializerClassName() {
        return deSerializerClassName;
    }

    public void setDeSerializerClassName(String deSerializerClassName) {
        this.deSerializerClassName = deSerializerClassName;
    }

    @Override
    public void setExpectReply(boolean expectReply) {
        this.expectReply = expectReply;
    }

    @Override
    public void setComponentName(String componentName) {
        this.gatewayDelegate.setComponentName(componentName);
    }

    @Override
    public void setRequestChannel(MessageChannel requestChannel) {
        this.gatewayDelegate.setRequestChannel(requestChannel);
    }

    @Override
    public void setRequestChannelName(String requestChannelName) {
        this.gatewayDelegate.setRequestChannelName(requestChannelName);
    }

    @Override
    public void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    public void setRecoverer(RecoveryCallback<Object> recoverer) {
        this.recoverer = recoverer;
    }

    @Override
    public void setReplyChannel(MessageChannel replyChannel) {
        this.gatewayDelegate.setReplyChannel(replyChannel);
    }

    @Override
    public void setReplyChannelName(String replyChannelName) {
        this.gatewayDelegate.setReplyChannelName(replyChannelName);
    }

    @Override
    public void setErrorChannel(MessageChannel errorChannel) {
        this.gatewayDelegate.setErrorChannel(errorChannel);
    }

    @Override
    public void setErrorChannelName(String errorChannelName) {
        this.gatewayDelegate.setErrorChannelName(errorChannelName);
    }

    @Override
    public void setRequestTimeout(long requestTimeout) {
        this.gatewayDelegate.setRequestTimeout(requestTimeout);
    }

    @Override
    public void setReplyTimeout(long replyTimeout) {
        this.gatewayDelegate.setReplyTimeout(replyTimeout);
    }

    @Override
    public void setErrorOnTimeout(boolean errorOnTimeout) {
        this.gatewayDelegate.setErrorOnTimeout(errorOnTimeout);
    }

    @Override
    public void setShouldTrack(boolean shouldTrack) {
        this.gatewayDelegate.setShouldTrack(shouldTrack);
    }

    @Override
    public String getComponentName() {
        return this.gatewayDelegate.getComponentName();
    }

    @Override
    public String getComponentType() {
        return this.gatewayDelegate.getComponentType();
    }

    @Override
    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public void setHeaderMapper(JmsHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }

    @Override
    public void setExtractRequestPayload(boolean extractRequestPayload) {
        this.extractRequestPayload = extractRequestPayload;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void onMessage(jakarta.jms.Message jmsMessage, Session session) throws JMSException {
        throw new IllegalArgumentException("Single consumer message listener should not be called for batched listener!!");
    }

    public void onMessage(List<jakarta.jms.Message> jmsMessages, Session session) throws JMSException {
    	 // Setup the converter at the start
        final MessageConverter converter = (deSerializerClassName != null) 
                ? new CustomSerializationMessageConverter() 
                : new SimpleMessageConverter();

        if (deSerializerClassName != null) {
            ((CustomSerializationMessageConverter) converter).setDeserializer(deSerializerClassName);
            this.setMessageConverter(converter);
        }
        
    	try {
            this.retryTemplate.execute(() -> {

                // Standard helper call
                this.onMessageHelper(jmsMessages, session);
                return null;
                
            });
        } catch (Exception e) { 
            // Catching Exception handles RetryException and the wrapped RuntimeException
            logger.error(e, "Batch processing failed after all retry attempts");

            // Perform cleanup ONLY on final failure
            for (jakarta.jms.Message jmsMessage : jmsMessages) {
                this.resetMessageIfRequired(jmsMessage);
            }

            if (this.recoverer != null) {
                try {
                	  //  Convert the entire batch into a List of payloads
                    List<Object> batchPayload = new ArrayList<>();
                    for (jakarta.jms.Message jmsMsg : jmsMessages) {
                        batchPayload.add(converter.fromMessage(jmsMsg));
                    }

                    // Map headers from the first message in the batch for context
                    // This is standard for batch error handling
                    Map<String, Object> headers = this.headerMapper.toHeaders(jmsMessages.get(0));

                    // Wrap the List in a Spring Message
                    // This prevents the ClassCastException in ErrorMessageSendingRecoverer
                    final Message<?> springBatchMessage = MessageBuilder.createMessage(batchPayload, new MessageHeaders(headers));

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
                    context.setAttribute(RETRY_CONTEXT_MESSAGE_ATTRIBUTE, springBatchMessage);

                    // Pass the context and the actual exception (e) to the recoverer
                    this.recoverer.recover(context, e); 
                } catch (Exception ex) {
                	logger.error(ex, "Recovery logic failed for batch");
                    throw new JMSException("Batch recovery failed: " + ex.getMessage());
                }
            } else {
                // Rethrow the cause if it's a JMSException to maintain the contract
                Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                if (cause instanceof JMSException jmsEx) throw jmsEx;
                throw new JMSException("Batch retry exhausted: " + cause.getMessage());
            }
        }
    }

    protected void resetMessageIfRequired(jakarta.jms.Message jmsMessage)
            throws JMSException {
        if (jmsMessage instanceof BytesMessage message) {
            message.reset();
        }
    }

    public void onMessageHelper(List<jakarta.jms.Message> jmsMessages, Session session) throws JMSException {
        Message<?> requestMessage = null;
        try {
            // result will store the list of appropriately converted message
            final List<Object> result = new ArrayList<>();
            List<Map<String, Object>> individualHeaders = new ArrayList<>();
            if (this.extractRequestPayload) {
                for (jakarta.jms.Message jmsMessage : jmsMessages) {
                    Object payload = this.messageConverter.fromMessage(jmsMessage);
                    result.add(payload);
                    Map<String, Object> headers = this.headerMapper.toHeaders(jmsMessage);
                    individualHeaders.add(headers);
                    this.logger.debug(() -> "converted JMS Message [" + jmsMessage + "] to integration Message payload ["
                            + payload + "]");
                }
            } else {
                for (jakarta.jms.Message jmsMessage : jmsMessages)
                    result.add((Object) jmsMessage);
            }

            requestMessage = this.messageBuilderFactory
                    .withPayload(result)
                    .setHeader(TEQ_BATCHED_HEADERS, individualHeaders)
                    .build();

        } catch (RuntimeException e) {
            MessageChannel errorChannel = this.gatewayDelegate.getErrorChannel();
            if (errorChannel == null) {
                throw e;
            }
            this.gatewayDelegate.getMessagingTemplate()
                    .send(errorChannel,
                            this.gatewayDelegate.buildErrorMessage(
                                    new MessagingException("Inbound conversion failed for: " + jmsMessages, e)));
            return;
        }

        this.gatewayDelegate.send(requestMessage);
    }

    @Override
    public void afterPropertiesSet() {
        if (this.beanFactory != null) {
            this.gatewayDelegate.setBeanFactory(this.beanFactory);
        }
        this.gatewayDelegate.afterPropertiesSet();
        this.messageBuilderFactory = IntegrationUtils.getMessageBuilderFactory(this.beanFactory);
    }

    @Override
    protected void start() {
        this.gatewayDelegate.start();
    }

    @Override
    protected void stop() {
        this.gatewayDelegate.stop();
    }

    private class GatewayDelegate extends MessagingGatewaySupport {

        GatewayDelegate() {
        }

        @Override
        protected void send(Object request) { // NOSONAR - not useless, increases visibility
            super.send(request);
        }

        @Override
        protected Message<?> sendAndReceiveMessage(Object request) { // NOSONAR - not useless, increases visibility
            return super.sendAndReceiveMessage(request);
        }

        protected ErrorMessage buildErrorMessage(Throwable throwable) {
            return super.buildErrorMessage(null, throwable);
        }

        protected MessagingTemplate getMessagingTemplate() {
            return this.messagingTemplate;
        }

        @Override
        public String getComponentType() {
            if (TEQBatchMessageListener.this.expectReply) {
                return "jms:inbound-gateway";
            } else {
                return "jms:message-driven-channel-adapter";
            }
        }

    }
}