// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp.micrometer;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;

import javax.sql.DataSource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class UcpMicrometerAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(UcpMicrometerAutoConfiguration.class));

    @Test
    void bindsEveryUcpPool() throws Exception {
        PoolDataSource firstPool = pool("orders");
        PoolDataSource secondPool = pool("billing");

        contextRunner.withBean("firstPool", DataSource.class, () -> firstPool)
                .withBean("secondPool", DataSource.class, () -> secondPool)
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .run(context -> {
                    MeterRegistry registry = context.getBean(MeterRegistry.class);

                    assertThat(registry.find("ucp.connections.active").tag("pool", "orders").gauge()).isNotNull();
                    assertThat(registry.find("ucp.connections.active").tag("pool", "billing").gauge()).isNotNull();
                });
    }

    @Test
    void ignoresNonUcpDataSources() {
        DataSource dataSource = (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DataSource.class },
                (proxy, method, args) -> defaultValue(method.getReturnType()));
        contextRunner.withBean(DataSource.class, () -> dataSource)
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .run(context -> {
                    MeterRegistry registry = context.getBean(MeterRegistry.class);
                    assertThat(registry.find("ucp.connections.active").meter()).isNull();
                });
    }

    private PoolDataSource pool(String name) throws Exception {
        PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setConnectionPoolName(name);
        return dataSource;
    }

    private Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        return null;
    }
}
