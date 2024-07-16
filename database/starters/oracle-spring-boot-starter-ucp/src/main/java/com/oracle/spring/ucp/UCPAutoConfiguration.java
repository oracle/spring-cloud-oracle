// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp;

import javax.sql.DataSource;

import java.sql.SQLException;

import jakarta.annotation.PostConstruct;
import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@AutoConfiguration
@ConditionalOnClass({OracleDataSource.class})
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
}
