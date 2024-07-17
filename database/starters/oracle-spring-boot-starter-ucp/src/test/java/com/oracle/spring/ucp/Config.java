package com.oracle.spring.ucp;

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
