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

package com.oracle.cstream.config;

import com.oracle.cstream.JMSMessageChannelBinder;
import com.oracle.cstream.utils.*;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class JmsBinderGlobalConfiguration {

  private ConnectionFactory connectionFactory;
  
  public JmsBinderGlobalConfiguration(ConnectionFactory connectionFactory) {
	  this.connectionFactory = connectionFactory;
  }

  @Bean
  public DestinationNameResolver queueNameResolver() {
    return new DestinationNameResolver(
      new Base64UrlNamingStrategy("anonymous_")
    );
  }

  @Bean
  @ConditionalOnMissingBean(MessageRecoverer.class)
  MessageRecoverer defaultMessageRecoverer() {
    return new RepublishMessageRecoverer(
      jmsTemplate(),
      new SpecCompliantJmsHeaderMapper()
    );
  }

  @Bean
  ListenerContainerFactory listenerContainerFactory() {
    return new ListenerContainerFactory(connectionFactory);
  }

  @Bean
  public JmsMessageDrivenChannelAdapterFactory jmsMessageDrivenChannelAdapterFactory(
    MessageRecoverer messageRecoverer,
    ListenerContainerFactory listenerContainerFactory
  ) {
    return new JmsMessageDrivenChannelAdapterFactory(
      listenerContainerFactory,
      messageRecoverer
    );
  }

  @Bean
  @ConditionalOnMissingBean(JmsSendingMessageHandlerFactory.class)
  public JmsSendingMessageHandlerFactory jmsSendingMessageHandlerFactory()
  {
    return new JmsSendingMessageHandlerFactory(
      jmsTemplate(),
      new SpecCompliantJmsHeaderMapper()
    );
  }

  @Bean
  @ConditionalOnMissingBean(JmsTemplate.class)
  public JmsTemplate jmsTemplate() {
    return new JmsTemplate(connectionFactory);
  }

  @Configuration
  @EnableConfigurationProperties(JmsExtendedBindingProperties.class)
  public static class JmsBinderConfiguration {

    @Bean
    JMSMessageChannelBinder jmsMessageChannelBinder(
      JmsMessageDrivenChannelAdapterFactory jmsMessageDrivenChannelAdapterFactory,
      JmsSendingMessageHandlerFactory jmsSendingMessageHandlerFactory,
      JmsTemplate jmsTemplate,
      ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> provisioningProvider,
      ConnectionFactory connectionFactory,
      JmsExtendedBindingProperties jmsExtendedBindingProperties,
      DestinationNameResolver destinationNameResolver
    ) {
      JMSMessageChannelBinder jmsMessageChannelBinder = new JMSMessageChannelBinder(
        provisioningProvider,
        jmsSendingMessageHandlerFactory,
        jmsMessageDrivenChannelAdapterFactory,
        jmsTemplate,
        connectionFactory
      );
      
      jmsMessageChannelBinder.setExtendedBindingProperties(
        jmsExtendedBindingProperties
      );
      
      jmsMessageChannelBinder.setDestinationNameResolver(destinationNameResolver);
      
      return jmsMessageChannelBinder;
    }
  }
}
