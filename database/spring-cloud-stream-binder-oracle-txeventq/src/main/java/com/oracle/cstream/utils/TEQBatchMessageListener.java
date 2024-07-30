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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.DefaultJmsHeaderMapper;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.integration.support.utils.IntegrationUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.support.RetryTemplate;

import com.oracle.cstream.serialize.CustomSerializationMessageConverter;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Session;

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

    private final String RETRY_CONTEXT_MESSAGE_ATTRIBUTE = "message";

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
        this.retryTemplate
                .execute(retryContext -> {
                            try {

                                if (deSerializerClassName != null) {
                                    // get class name and parameterized type name
                                    CustomSerializationMessageConverter customConverter = new CustomSerializationMessageConverter();
                                    customConverter.setDeserializer(deSerializerClassName);
                                    TEQBatchMessageListener.this.setMessageConverter(
                                            customConverter
                                    );
                                }

                                retryContext.setAttribute(
                                        RETRY_CONTEXT_MESSAGE_ATTRIBUTE,
                                        jmsMessages
                                );

                                TEQBatchMessageListener.this.onMessageHelper(
                                        jmsMessages,
                                        session
                                );
                            } catch (JMSException e) {
                                logger.error(e, "Failed to send message");
                                for (jakarta.jms.Message jmsMessage : jmsMessages)
                                    TEQBatchMessageListener.this.resetMessageIfRequired(jmsMessage);
                                throw new RuntimeException(e);
                            } catch (Exception e) {
                                for (jakarta.jms.Message jmsMessage : jmsMessages)
                                    TEQBatchMessageListener.this.resetMessageIfRequired(jmsMessage);
                                throw e;
                            }
                            return null;
                        },
                        this.recoverer
                );
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
            List<Map<String, Object>> individual_headers = new ArrayList<>();
            if (this.extractRequestPayload) {
                for (jakarta.jms.Message jmsMessage : jmsMessages) {
                    Object payload = this.messageConverter.fromMessage(jmsMessage);
                    result.add(payload);
                    Map<String, Object> headers = this.headerMapper.toHeaders(jmsMessage);
                    individual_headers.add(headers);
                    this.logger.debug(() -> "converted JMS Message [" + jmsMessage + "] to integration Message payload ["
                            + payload + "]");
                }
            } else {
                for (jakarta.jms.Message jmsMessage : jmsMessages)
                    result.add(jmsMessage);
            }

            requestMessage = this.messageBuilderFactory
                    .withPayload(result)
                    .setHeader(TEQ_BATCHED_HEADERS, individual_headers)
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

    protected void start() {
        this.gatewayDelegate.start();
    }

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