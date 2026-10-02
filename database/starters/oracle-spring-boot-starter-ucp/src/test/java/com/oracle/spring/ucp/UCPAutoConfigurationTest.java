// Copyright (c) 2024, 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.Properties;

import oracle.jdbc.OracleConnection;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

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
            assertThat(ds.getConnectionProperties().getProperty(
                    OracleConnection.CONNECTION_PROPERTY_THIN_VSESSION_PROGRAM)).isEqualTo("SPRING_CLOUD_ORACLE");
        } else {
            Assertions.fail("Datasource is not a PoolDataSourceImpl: " + dataSource.getClass().getName());
        }
    }

    @Test
    void preservesConfiguredSessionProgramAndConnectionProperties() throws SQLException {
        PoolDataSourceImpl ds = new PoolDataSourceImpl();
        Properties properties = new Properties();
        properties.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_VSESSION_PROGRAM, "CUSTOM_APPLICATION");
        properties.setProperty("oracle.net.CONNECT_TIMEOUT", "5000");
        ds.setConnectionProperties(properties);

        new UCPAutoConfiguration(ds).init();

        assertThat(ds.getConnectionProperties()).containsAllEntriesOf(properties);
    }

    @Test
    void defaultsSessionProgramWithoutReplacingOtherConnectionProperties() throws SQLException {
        PoolDataSourceImpl ds = new PoolDataSourceImpl();
        Properties properties = new Properties();
        properties.setProperty("oracle.net.CONNECT_TIMEOUT", "5000");
        ds.setConnectionProperties(properties);

        new UCPAutoConfiguration(ds).init();

        assertThat(ds.getConnectionProperties().getProperty(
                OracleConnection.CONNECTION_PROPERTY_THIN_VSESSION_PROGRAM)).isEqualTo("SPRING_CLOUD_ORACLE");
        assertThat(ds.getConnectionProperties().getProperty("oracle.net.CONNECT_TIMEOUT")).isEqualTo("5000");
    }
}
