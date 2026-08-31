// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp.micrometer;

import javax.sql.DataSource;

import io.micrometer.core.instrument.binder.MeterBinder;
import oracle.ucp.jdbc.PoolDataSource;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for UCP Micrometer metrics.
 */
@AutoConfiguration(afterName = "com.oracle.spring.ucp.UCPAutoConfiguration")
@ConditionalOnClass({ MeterBinder.class, PoolDataSource.class })
public class UcpMicrometerAutoConfiguration {

    @Bean
    MeterBinder ucpMeterBinder(ListableBeanFactory beanFactory) {
        return registry -> beanFactory.getBeansOfType(DataSource.class).forEach((beanName, dataSource) -> {
            if (dataSource instanceof PoolDataSource poolDataSource) {
                new UcpMetrics(poolDataSource, beanName).bindTo(registry);
            }
        });
    }

}
