// Copyright (c) 2023, 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.aqjms;

import javax.sql.DataSource;

import java.sql.SQLException;

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
@ConditionalOnClass({OracleDataSource.class})
public class AqJmsAutoConfiguration {
	private final DataSource dataSource;

	public AqJmsAutoConfiguration(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@PostConstruct
	public void init() {
		if (dataSource instanceof PoolDataSourceImpl ds && !isUCPLoaded()) {
			try {
				ds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
				setIfNull(ds.getConnectionPoolName(), () -> ds.setConnectionPoolName("SpringConnectionPool"));
				setIfNull(ds.getInitialPoolSize(), () -> ds.setInitialPoolSize(15));
				setIfNull(ds.getMinPoolSize(), () -> ds.setMinPoolSize(5));
				setIfNull(ds.getMaxPoolSize(), () -> ds.setMaxPoolSize(30));
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private <T> void setIfNull(T value, Setter setter) throws SQLException {
		if (value != null) {
			setter.set();
		}
	}

	private interface Setter {
		void set() throws SQLException;
	}

	@Bean
	@ConditionalOnMissingBean
	public ConnectionFactory aqJmsConnectionFactory(DataSource ds) {
		ConnectionFactory connectionFactory = null;
		try {
			connectionFactory = AQjmsFactory.getConnectionFactory(ds);
		} catch (Exception ignore) {}
		return connectionFactory;
	}

	private boolean isUCPLoaded() {
		return ClassUtils.isPresent("com.oracle.cloud.spring.ucp.UCPAutoConfiguration", AqJmsAutoConfiguration.class.getClassLoader());
	}

}
