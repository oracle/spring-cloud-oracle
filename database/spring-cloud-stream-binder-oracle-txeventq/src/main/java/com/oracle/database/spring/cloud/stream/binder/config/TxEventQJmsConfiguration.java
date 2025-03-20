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

package com.oracle.database.spring.cloud.stream.binder.config;

import com.oracle.database.spring.cloud.stream.binder.TxEventQQueueProvisioner;
import com.oracle.database.spring.cloud.stream.binder.plsql.OracleDBUtils;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import oracle.jakarta.jms.AQjmsConnectionFactory;
import oracle.jakarta.jms.AQjmsFactory;
import oracle.ucp.jdbc.PoolDataSource;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
//It is important to include the root JMS configuration class.
@Import(JmsBinderAutoConfiguration.class)
@AutoConfigureAfter({ JndiConnectionFactoryAutoConfiguration.class })
@ConditionalOnClass({ ConnectionFactory.class, AQjmsConnectionFactory.class })
public class TxEventQJmsConfiguration {
  
  private final Logger logger = LoggerFactory.getLogger(TxEventQJmsConfiguration.class);

  @Bean
  @ConditionalOnMissingBean(ConnectionFactory.class)
  public ConnectionFactory aqJmsConnectionFactory(PoolDataSource ds) {
    ConnectionFactory connectionFactory = null;
    try {
      connectionFactory = AQjmsFactory.getConnectionFactory(ds);
    } catch (JMSException ignore) {
    	logger.error("Error creating connection factory bean.", ignore);
    	throw new IllegalArgumentException("Error while trying to obtain connectionFactory.");
    }
    return connectionFactory;
  }
  
  @Bean
  public OracleDBUtils getOracleDBUtils(PoolDataSource pds) {
	  try(java.sql.Connection conn = pds.getConnection()) {
		  return new OracleDBUtils(pds, conn.getMetaData().getDatabaseMajorVersion());
	} catch (SQLException e) {
		logger.error("Error creating OracleDBUtils Bean.");
		throw new IllegalArgumentException("Cannot initialize OracleDBUtils", e);
	}
  }

  @Bean
  ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>,ExtendedProducerProperties<JmsProducerProperties>> txeventQQueueProvisioner(
    ConnectionFactory connectionFactory,
    OracleDBUtils dbutils
  ) {
    return new TxEventQQueueProvisioner(
      connectionFactory,
      dbutils
    );
  }
}
