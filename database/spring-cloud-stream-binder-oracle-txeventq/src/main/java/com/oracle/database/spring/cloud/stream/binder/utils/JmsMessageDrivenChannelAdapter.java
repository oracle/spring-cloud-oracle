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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.context.OrderlyShutdownCapable;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.jms.inbound.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.inbound.JmsMessageDrivenEndpoint;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.messaging.MessagingException;

public class JmsMessageDrivenChannelAdapter extends MessageProducerSupport implements
        OrderlyShutdownCapable {

    private JmsMessageDrivenEndpoint endpoint;

    private final ChannelPublishingJmsMessageListener listener;
    private final AbstractMessageListenerContainer listenerContainer;

    public JmsMessageDrivenChannelAdapter(AbstractMessageListenerContainer listenerContainer,
                                          ChannelPublishingJmsMessageListener listener) {
    	this.listenerContainer = listenerContainer;
        this.listener = listener;
    }

    @Override
    public void setOutputChannel(MessageChannel requestChannel) {
        super.setOutputChannel(requestChannel);
        this.listener.setRequestChannel(requestChannel);
    }

    @Override
    public void setOutputChannelName(String requestChannelName) {
        super.setOutputChannelName(requestChannelName);
        this.listener.setRequestChannelName(requestChannelName);
    }

    @Override
    public void setErrorChannel(MessageChannel errorChannel) {
        super.setErrorChannel(errorChannel);
        this.listener.setErrorChannel(errorChannel);
    }

    @Override
    public void setErrorChannelName(String errorChannelName) {
        this.listener.setErrorChannelName(errorChannelName);
    }

    @Override
    public void setSendTimeout(long requestTimeout) {
        this.listener.setRequestTimeout(requestTimeout);
    }

    @Override
    public void setShouldTrack(boolean shouldTrack) {
        this.listener.setShouldTrack(shouldTrack);
    }

    @Override
    public String getComponentType() {
        return "jms:message-driven-channel-adapter";
    }

    @Override
    public void setComponentName(String componentName) {
    	super.setComponentName(componentName);
        
        // Add this null check because endpoint doesn't exist yet during early setup
        if (this.endpoint != null) {
            this.endpoint.setComponentName(getComponentName() + ".endpoint");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	super.setApplicationContext(applicationContext);
    	
    	// The endpoint is created in onInit, so we only set the listener here
    	if (this.listener != null) {
    		this.listener.setBeanFactory(applicationContext);
    	}
    }

    @Override
    protected void onInit() {    	
    	 // Prepare infrastructure via parent
        super.onInit(); 

        ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) getBeanFactory();

        // Resolve the official TaskScheduler
        TaskScheduler scheduler = IntegrationContextUtils.getTaskScheduler(beanFactory);

        // Create and "Wire" the endpoint manually
        this.endpoint = new JmsMessageDrivenEndpoint(this.listenerContainer, this.listener);
        
        // This tells Spring to treat this object as a managed bean
        beanFactory.autowireBean(this.endpoint);
        this.endpoint.setBeanFactory(beanFactory);
        this.endpoint.setApplicationContext(getApplicationContext());
        this.endpoint.setTaskScheduler(scheduler);
        this.endpoint.setComponentName(getComponentName() + ".endpoint");

        // Initialize the endpoint
        try {
            this.endpoint.afterPropertiesSet();
        } catch (Exception e) {
            throw new MessagingException("Endpoint initialization failed", e);
        }
    }

    ChannelPublishingJmsMessageListener getListener() {
        return this.listener;
    }

    @Override
    protected void doStart() {
        this.endpoint.start();
    }

    @Override
    protected void doStop() {
        this.endpoint.stop();
    }

    @Override
    public void destroy() {
        this.endpoint.destroy();
    }

    @Override
    public int beforeShutdown() {
        return this.endpoint.beforeShutdown();
    }

    @Override
    public int afterShutdown() {
        return this.endpoint.afterShutdown();
    }

}
