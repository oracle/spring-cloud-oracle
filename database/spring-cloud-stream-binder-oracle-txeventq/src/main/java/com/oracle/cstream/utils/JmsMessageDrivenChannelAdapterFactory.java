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

import com.oracle.cstream.config.JmsConsumerProperties;
import com.oracle.cstream.serialize.CustomSerializationMessageConverter;

import jakarta.jms.BytesMessage;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

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
	  ChannelPublishingJmsMessageListener listener = null;
	  if(!properties.isBatchMode()) {
		  	listener = new RetryingChannelPublishingJmsMessageListener(
		  			properties,
		  			messageRecoverer,
		  			properties.getExtension().getDlqName(),
		  			retryTemplate,
		  			recoveryCallback
		  			);
		  	if(properties.isUseNativeDecoding()) {
		  		((RetryingChannelPublishingJmsMessageListener) listener)
		  			.setDeSerializerClassName(properties.getExtension().getDeSerializer());
		  	}
	  } else {
		  listener = new TEQBatchMessageListener();
		  ((TEQBatchMessageListener)listener).setRetryTemplate(retryTemplate);
		  ((TEQBatchMessageListener)listener).setRecoverer(recoveryCallback);
		  ((TEQBatchMessageListener)listener).setDeSerializerClassName(properties.getExtension().getDeSerializer());
	  }
    listener.setBeanFactory(this.beanFactory);
    
    if(dbversion == 19 && properties.getInstanceIndex() != -1) {
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
      this.retryTemplate
        .execute(
          new RetryCallback<Object, JMSException>() {
            @Override
            public Object doWithRetry(RetryContext retryContext)
              throws JMSException {
              try {
            	// convert data if de-serialization is enabled
            	if(deSerializerClassName != null) {
            		// get class name and parameterized type name
            		CustomSerializationMessageConverter customConverter = new CustomSerializationMessageConverter();
            		customConverter.setDeserializer(deSerializerClassName);
            		RetryingChannelPublishingJmsMessageListener.this.setMessageConverter(
            				customConverter
            				);
            	}
            	
                retryContext.setAttribute(
                  RETRY_CONTEXT_MESSAGE_ATTRIBUTE,
                  jmsMessage
                );
                RetryingChannelPublishingJmsMessageListener.super.onMessage(
                  jmsMessage,
                  session
                );
              } catch (JMSException e) {
                logger.error(e, "Failed to send message");
                resetMessageIfRequired(jmsMessage);
                throw e;
              } catch (Exception e) {
                resetMessageIfRequired(jmsMessage);
                throw e;
              }
              return null;
            }
          },
          this.recoverer
        );
    }

    protected void resetMessageIfRequired(Message jmsMessage)
      throws JMSException {
      if (jmsMessage instanceof BytesMessage message) {
        message.reset();
      }
    }
  }
}
