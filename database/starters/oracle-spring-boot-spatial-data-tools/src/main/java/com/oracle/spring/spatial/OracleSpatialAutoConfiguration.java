// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import javax.sql.DataSource;

import oracle.jdbc.OracleConnection;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnClass(OracleConnection.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = OracleSpatialProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OracleSpatialProperties.class)
public class OracleSpatialAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    OracleSpatialGeoJsonConverter oracleSpatialGeoJsonConverter(OracleSpatialProperties properties) {
        return new OracleSpatialGeoJsonConverter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    OracleSpatialSqlBuilder oracleSpatialSqlBuilder(OracleSpatialGeoJsonConverter geoJsonConverter) {
        return new OracleSpatialSqlBuilder(geoJsonConverter);
    }
}
