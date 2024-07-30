/*
** TxEventQ Support for Spring Cloud Stream
** Copyright (c) 2023, 2024 Oracle and/or its affiliates.
** 
** This file has been modified by Oracle Corporation.
** 
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

package com.oracle.cstream;

import com.oracle.cstream.config.*;
import com.oracle.cstream.provisioning.JmsConsumerDestination;
import com.oracle.cstream.provisioning.JmsProducerDestination;
import com.oracle.cstream.utils.*;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binder.HeaderMode;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.support.RetryTemplate;


public class JMSMessageChannelBinder
  extends AbstractMessageChannelBinder<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>, ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>>>
  implements
    ExtendedPropertiesBinder<MessageChannel, JmsConsumerProperties, JmsProducerProperties> {

  private JmsExtendedBindingProperties extendedBindingProperties = new JmsExtendedBindingProperties();

  private final JmsSendingMessageHandlerFactory jmsSendingMessageHandlerFactory;
  private final JmsMessageDrivenChannelAdapterFactory jmsMessageDrivenChannelAdapterFactory;
  private final ConnectionFactory connectionFactory;

  private final DestinationResolver destinationResolver;

  private DestinationNameResolver destinationNameResolver;
  
  public JMSMessageChannelBinder(
    ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> provisioningProvider,
    JmsSendingMessageHandlerFactory jmsSendingMessageHandlerFactory,
    JmsMessageDrivenChannelAdapterFactory jmsMessageDrivenChannelAdapterFactory,
    JmsTemplate jmsTemplate,
    ConnectionFactory connectionFactory
  ) {
    super(null, provisioningProvider);
    this.jmsSendingMessageHandlerFactory = jmsSendingMessageHandlerFactory;
    this.jmsMessageDrivenChannelAdapterFactory =
      jmsMessageDrivenChannelAdapterFactory;
    this.connectionFactory = connectionFactory;
    this.destinationResolver = jmsTemplate.getDestinationResolver();
  }

  public void setExtendedBindingProperties(
    JmsExtendedBindingProperties extendedBindingProperties
  ) {
    this.extendedBindingProperties = extendedBindingProperties;
  }
  
  public void setDestinationNameResolver(DestinationNameResolver destinationNameResolver) {
	  this.destinationNameResolver = destinationNameResolver;
  }

  @Override
  protected MessageHandler createProducerMessageHandler(
    ProducerDestination producerDestination,
    ExtendedProducerProperties<JmsProducerProperties> producerProperties,
    MessageChannel errorChannel
  ) throws Exception {
	 Topic topic = null;
	 try(Connection conn = connectionFactory.createConnection()) {
		 Session session = conn.createSession(true, 1);

		 String destination = producerDestination.getName();
		 topic = (Topic) destinationResolver.resolveDestinationName(
				 			session,
				 			destination,
				 			true);
	 }
    
     if(producerProperties.isUseNativeEncoding()) {
    	return this.jmsSendingMessageHandlerFactory
        		.build(topic, errorChannel, 
        				producerProperties.getHeaderMode() == null 
        					|| producerProperties.getHeaderMode().equals(HeaderMode.headers),
        		producerProperties.getExtension().getSerializer(),
        		((JmsProducerDestination)producerDestination).getDBVersion());
    }
    
    return this.jmsSendingMessageHandlerFactory
    		.build(topic, errorChannel, 
    				producerProperties.getHeaderMode() == null || 
    				producerProperties.getHeaderMode().equals(HeaderMode.headers),
    				null,
    				((JmsProducerDestination)producerDestination).getDBVersion());
  }

  @Override
  protected org.springframework.integration.core.MessageProducer createConsumerEndpoint(
    ConsumerDestination consumerDestination,
    String group,
    ExtendedConsumerProperties<JmsConsumerProperties> properties
  ) throws Exception {
	  group = this.destinationNameResolver.resolveGroupName(group);
	  Topic topic = null;
	  try(Connection conn = connectionFactory.createConnection()) {
			 Session session = conn.createSession(true, 1);

			 topic = (Topic) destinationResolver.resolveDestinationName(
					 			session,
					 			consumerDestination.getName(),
					 			true);
	  }
	  
    
	  RetryTemplate retryTemplate = buildRetryTemplate(properties);
	  ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(consumerDestination, group, properties);
	  RecoveryCallback<Object> recoveryCallback = errorInfrastructure
    					.getRecoverer();
    
	  return jmsMessageDrivenChannelAdapterFactory.build(
			  topic,
			  group,
			  retryTemplate,
			  recoveryCallback,
			  errorInfrastructure.getErrorChannel(),
			  properties,
			  ((JmsConsumerDestination)consumerDestination).getDBVersion()
	  );
  }

  @Override
  public JmsConsumerProperties getExtendedConsumerProperties(
    String channelName
  ) {
    return this.extendedBindingProperties.getExtendedConsumerProperties(
        channelName
      );
  }

  @Override
  public JmsProducerProperties getExtendedProducerProperties(
    String channelName
  ) {
    return this.extendedBindingProperties.getExtendedProducerProperties(
        channelName
      );
  }

  @Override
  public String getDefaultsPrefix() {
    return this.extendedBindingProperties.getDefaultsPrefix();
  }

  @Override
  public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
    return this.extendedBindingProperties.getExtendedPropertiesEntryClass();
  }
}
