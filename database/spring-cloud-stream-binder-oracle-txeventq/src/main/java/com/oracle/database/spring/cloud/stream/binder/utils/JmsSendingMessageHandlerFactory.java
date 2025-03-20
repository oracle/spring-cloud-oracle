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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageChannel;

import jakarta.jms.Destination;

public class JmsSendingMessageHandlerFactory implements ApplicationContextAware, BeanFactoryAware {

	private final JmsTemplate template;

	private ApplicationContext applicationContext;

	private BeanFactory beanFactory;

	private final JmsHeaderMapper headerMapper;

	public JmsSendingMessageHandlerFactory(JmsTemplate template,
										   JmsHeaderMapper headerMapper) {
		this.template = template;
		this.headerMapper = headerMapper;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public PartitionAwareJmsSendingMessageHandler build(Destination destination,
			MessageChannel errorChannel,
			boolean mapHeaders,
			String serializer,
			int dbversion) {
		template.setPubSubDomain(true);
		PartitionAwareJmsSendingMessageHandler handler = new PartitionAwareJmsSendingMessageHandler(
				this.template,
				destination,
				headerMapper,
				errorChannel,
				mapHeaders);
		handler.setSerializerClassName(serializer);
		handler.setApplicationContext(this.applicationContext);
		handler.setBeanFactory(this.beanFactory);
		handler.afterPropertiesSet();
		handler.setDBVersion(dbversion);
		return handler;
	}

}
