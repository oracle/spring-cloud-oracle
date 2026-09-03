// Copyright (c) 2024, 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp;

import javax.sql.DataSource;

import java.sql.SQLException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@AutoConfiguration
@ConditionalOnClass({OracleDataSource.class})
@ConditionalOnBean(DataSource.class)
@SuppressFBWarnings(value = "EI2",
        justification = "The DataSource is an application-managed Spring dependency configured by this auto-configuration.")
public class UCPAutoConfiguration {
    private final DataSource dataSource;

    public UCPAutoConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        if (dataSource instanceof PoolDataSourceImpl ds) {
            try {
                ds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
                setIfNull(ds.getConnectionPoolName(), () -> ds.setConnectionPoolName("SpringConnectionPool"));
                setIfNull(ds.getInitialPoolSize(), () -> ds.setInitialPoolSize(15));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private <T> void setIfNull(T value, Setter setter) throws SQLException {
        if (value == null) {
            setter.set();
        } else if (value instanceof Integer intValue) {
            if (intValue < 1) {
                setter.set();
            }
        }
    }

    private interface Setter {
        void set() throws SQLException;
    }
}
