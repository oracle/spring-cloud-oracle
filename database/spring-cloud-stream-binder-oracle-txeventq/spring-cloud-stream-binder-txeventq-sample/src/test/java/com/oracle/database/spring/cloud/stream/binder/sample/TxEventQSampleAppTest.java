// Copyright (c) 2024, 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.cloud.stream.binder.sample;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binding.BindingsLifecycleController;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
public class TxEventQSampleAppTest {
    @Container
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.26.1-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword("testpwd");

    @BeforeAll
    public static void setUp() throws Exception {
        oracleContainer.start();
        oracleContainer.copyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/tmp/init.sql");
        oracleContainer.execInContainer("sqlplus", "sys / as sysdba", "@/tmp/init.sql");
    }
    
    @TestConfiguration
    static class Config {
        @Bean
        public javax.sql.DataSource dataSource() throws java.sql.SQLException {
            oracle.ucp.jdbc.PoolDataSource pds = oracle.ucp.jdbc.PoolDataSourceFactory.getPoolDataSource();
            pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
            pds.setURL(oracleContainer.getJdbcUrl());
            pds.setUser(oracleContainer.getUsername());
            pds.setPassword(oracleContainer.getPassword());
            pds.setInitialPoolSize(1);
            pds.setMinPoolSize(1);
            pds.setMaxPoolSize(3);
            return pds;
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("JDBC_URL", oracleContainer::getJdbcUrl);
        registry.add("USERNAME", oracleContainer::getUsername);
        registry.add("PASSWORD", oracleContainer::getPassword);
    }

    @Autowired
    WordSupplier wordSupplier;

    @Autowired
    BindingsLifecycleController lifecycleController;
    
    /**
     * This ensures the JMS Poller stops BEFORE the UCP DataSource closes.
     */
    @AfterEach
    void stopBindings() {
        if (lifecycleController != null) {
            lifecycleController.queryStates().forEach(state -> {
                String bindingName = (String) state.get("bindingName");
                lifecycleController.stop(bindingName);
            });
        }
    }

    @Test
    void processStream() throws InterruptedException {
    	 await()
         .atMost(30, TimeUnit.SECONDS) 
         .pollInterval(Duration.ofSeconds(2))
         .until(() -> {
             // Check if the supplier is done OR if we've reached 
             // a target number of "Consumed" logs.
             return wordSupplier.done(); 
         });
    }
}
