// Copyright (c) 2023, 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.aqjms;

import javax.sql.DataSource;

import com.oracle.spring.ucp.UCPAutoConfiguration;
import jakarta.jms.ConnectionFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import oracle.jakarta.jms.AQjmsFactory;

/**
 * This class autowires the configuration and injects both a JDBC DataSource
 * and a JMSConnectionFactory into your application.
 */
@AutoConfiguration
@ConditionalOnClass({UCPAutoConfiguration.class})
public class AqJmsAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean
	public ConnectionFactory aqJmsConnectionFactory(DataSource ds) {
		ConnectionFactory connectionFactory = null;
		try {
			connectionFactory = AQjmsFactory.getConnectionFactory(ds);
		} catch (Exception ignore) {}
		return connectionFactory;
	}
}
