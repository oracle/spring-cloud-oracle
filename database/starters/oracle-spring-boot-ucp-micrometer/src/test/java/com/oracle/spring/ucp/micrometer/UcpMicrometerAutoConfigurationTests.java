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
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class UcpMicrometerAutoConfigurationTests {

    private static final String POOL_NAME_ATTRIBUTE = "db.client.connection.pool.name";
    private static final String STATE_ATTRIBUTE = "db.client.connection.state";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(UcpMicrometerAutoConfiguration.class));

    private final ApplicationContextRunner metricsContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MetricsAutoConfiguration.class,
                    SimpleMetricsExportAutoConfiguration.class, UcpMicrometerAutoConfiguration.class));

    @Test
    void bindsEveryUcpPool() throws Exception {
        PoolDataSource firstPool = pool("orders");
        PoolDataSource secondPool = pool("billing");

        contextRunner.withBean("ordersDataSource", DataSource.class, () -> firstPool)
                .withBean("billingDataSource", DataSource.class, () -> secondPool)
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .run(context -> {
                    MeterRegistry registry = context.getBean(MeterRegistry.class);
                    context.getBean(MeterBinder.class).bindTo(registry);

                    assertThat(registry.find("db.client.connection.count")
                            .tag(POOL_NAME_ATTRIBUTE, "orders")
                            .tag(STATE_ATTRIBUTE, "idle")
                            .gauge()).isNotNull();
                    assertThat(registry.find("db.client.connection.count")
                            .tag(POOL_NAME_ATTRIBUTE, "billing")
                            .tag(STATE_ATTRIBUTE, "idle")
                            .gauge()).isNotNull();
                    assertThat(registry.find("db.client.connection.count").meters()).hasSize(4);
                });
    }

    @Test
    void usesBeanNamesForUnnamedUcpPools() throws Exception {
        PoolDataSource ordersPool = pool();
        PoolDataSource billingPool = pool();

        contextRunner.withBean("ordersDataSource", DataSource.class, () -> ordersPool)
                .withBean("billingDataSource", DataSource.class, () -> billingPool)
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .run(context -> {
                    MeterRegistry registry = context.getBean(MeterRegistry.class);
                    context.getBean(MeterBinder.class).bindTo(registry);

                    assertThat(registry.find("db.client.connection.count")
                            .tag(POOL_NAME_ATTRIBUTE, "ordersDataSource")
                            .tag(STATE_ATTRIBUTE, "used")
                            .gauge()).isNotNull();
                    assertThat(registry.find("db.client.connection.count")
                            .tag(POOL_NAME_ATTRIBUTE, "billingDataSource")
                            .tag(STATE_ATTRIBUTE, "used")
                            .gauge()).isNotNull();
                });
    }

    @Test
    void usesBeanNameForBlankUcpPoolName() throws Exception {
        PoolDataSource pool = pool(" ");

        contextRunner.withBean("ordersDataSource", DataSource.class, () -> pool)
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .run(context -> {
                    MeterRegistry registry = context.getBean(MeterRegistry.class);
                    context.getBean(MeterBinder.class).bindTo(registry);

                    assertThat(registry.find("db.client.connection.count")
                            .tag(POOL_NAME_ATTRIBUTE, "ordersDataSource")
                            .tag(STATE_ATTRIBUTE, "idle")
                            .gauge()).isNotNull();
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
                    context.getBean(MeterBinder.class).bindTo(registry);
                    assertThat(registry.find("db.client.connection.count").meter()).isNull();
                });
    }

    @Test
    void bindsThroughSpringBootAfterRegistryCustomization() throws Exception {
        PoolDataSource pool = pool("orders");

        metricsContextRunner.withBean("ordersPool", DataSource.class, () -> pool)
                .withBean("commonTagsCustomizer", MeterRegistryCustomizer.class,
                        () -> (MeterRegistryCustomizer<MeterRegistry>) registry -> registry.config()
                                .commonTags("application", "ucp-test"))
                .run(context -> {
                    MeterRegistry registry = context.getBean(MeterRegistry.class);

                    assertThat(context).hasSingleBean(MeterBinder.class);
                    assertThat(context.containsBean("ucpMeterBinderInitializer")).isFalse();
                    assertThat(registry.find("db.client.connection.count")
                            .tag(POOL_NAME_ATTRIBUTE, "orders")
                            .tag(STATE_ATTRIBUTE, "used")
                            .tag("application", "ucp-test")
                            .gauge()).isNotNull();
                });
    }

    private PoolDataSource pool(String name) throws Exception {
        PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setConnectionPoolName(name);
        return dataSource;
    }

    private PoolDataSource pool() throws Exception {
        return PoolDataSourceFactory.getPoolDataSource();
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
