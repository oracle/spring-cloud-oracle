// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import javax.sql.DataSource;

import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class OracleSpatialConditionalAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OracleSpatialAutoConfiguration.class));

    @Test
    void backsOffWhenNoDataSourceBeanIsPresent() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(OracleSpatialJdbcOperations.class);
        });
    }

    @Test
    void backsOffWhenOracleJdbcClassesAreMissing() {
        contextRunner
                .withUserConfiguration(TestDataSourceConfiguration.class)
                .withClassLoader(new FilteredClassLoader(oracle.jdbc.OracleConnection.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(DataSource.class);
                    assertThat(context).doesNotHaveBean(OracleSpatialJdbcOperations.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestDataSourceConfiguration {
        @Bean
        DataSource dataSource() {
            return PoolDataSourceFactory.getPoolDataSource();
        }
    }
}
