// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp;

import javax.sql.DataSource;

import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UCPAutoConfiguration.class)
@Import(Config.class)
public class UCPAutoConfigurationTest {
    @Autowired
    DataSource dataSource;

    @Test
    void dataSourceConfigured() {
        assertThat(dataSource).isNotNull();
        if (dataSource instanceof PoolDataSourceImpl ds) {
            assertThat(ds.getInitialPoolSize()).isEqualTo(15);
            assertThat(ds.getConnectionFactoryClassName()).isEqualTo("oracle.jdbc.pool.OracleDataSource");
            assertThat(ds.getConnectionPoolName()).isEqualTo("SpringConnectionPool");
        } else {
            Assertions.fail("Datasource is not a PoolDataSourceImpl: " + dataSource.getClass().getName());
        }
    }
}
