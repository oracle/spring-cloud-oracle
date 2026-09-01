// Copyright (c) 2024, 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.cloud.stream.binder;

import java.sql.SQLException;

import com.oracle.database.spring.cloud.stream.binder.config.TxEventQJmsConfiguration;
import com.oracle.database.spring.testcontainers.OracleContainer;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = TxEventQJmsConfiguration.class)
@Import(TEQSpringBootIT.Config.class)
public class TEQSpringBootIT {
    @Container
    private static final OracleContainer oracleContainer = Util.oracleContainer();

    @Configuration
    public static class Config {
        @Bean
        public PoolDataSource poolDataSource() throws SQLException {
            PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
            Util.configurePoolDataSource(poolDataSource, oracleContainer);
            return poolDataSource;
        }
    }

    @BeforeAll
    public static void setUp() throws Exception {
        Util.startOracleContainer(oracleContainer);
    }

    @Autowired
    JMSMessageChannelBinder jmsMessageChannelBinder;

    @Test
    void contextLoads() {}
}
