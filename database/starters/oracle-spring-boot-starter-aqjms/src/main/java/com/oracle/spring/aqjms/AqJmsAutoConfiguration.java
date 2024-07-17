// Copyright (c) 2023, 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.aqjms;

import javax.sql.DataSource;

import java.sql.SQLException;

import com.oracle.spring.ucp.UCPAutoConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.jms.ConnectionFactory;

import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import oracle.jakarta.jms.AQjmsFactory;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.util.ClassUtils;

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
