// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import javax.sql.DataSource;

import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class Config {
    @Bean
    DataSource dataSource() {
        return PoolDataSourceFactory.getPoolDataSource();
    }
}
