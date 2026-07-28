// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp.micrometer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.junit.jupiter.api.Test;

class UcpMetricsTests {

    @Test
    void bindsLivePoolStatistics() throws Exception {
        PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setConnectionPoolName("orders");
        UcpTestStatistics statistics = new UcpTestStatistics();
        statistics.set("getMaxPoolSize", 30);
        statistics.set("getMinPoolSize", 10);
        statistics.set("getTotalConnectionsCount", 12);
        statistics.set("getAvailableConnectionsCount", 5);
        statistics.set("getBorrowedConnectionsCount", 7);
        statistics.set("getPendingRequestsCount", 2);
        statistics.set("getRemainingPoolCapacityCount", 18);
        statistics.set("getPeakConnectionsCount", 14);
        statistics.set("getAverageBorrowedConnectionsCount", 4);
        statistics.set("getPeakBorrowedConnectionsCount", 9);
        statistics.set("getLabeledConnectionsCount", 1);
        statistics.set("getAbandonedConnectionsCount", 3);
        statistics.set("getAverageConnectionWaitTime", 4L);
        statistics.set("getPeakConnectionWaitTime", 15L);
        statistics.set("getConnectionsCreatedCount", 20);
        statistics.set("getConnectionsClosedCount", 8);
        statistics.set("getCumulativeConnectionBorrowedCount", 40L);
        statistics.set("getCumulativeConnectionReturnedCount", 38L);
        statistics.set("getCumulativeConnectionCreationAttempts", 6L);
        statistics.set("getCumulativeSuccessfulConnectionWaitCount", 35L);
        statistics.set("getCumulativeSuccessfulConnectionWaitTime", 70L);
        statistics.set("getCumulativeFailedConnectionWaitCount", 5L);
        statistics.set("getCumulativeFailedConnectionWaitTime", 25L);
        statistics.set("getCumulativeConnectionWaitTime", 95L);
        statistics.set("getCumulativeConnectionUseTime", 760L);

        MeterRegistry registry = new SimpleMeterRegistry();
        new UcpMetrics(poolDataSource, statistics::statistics).bindTo(registry);

        assertThat(registry.getMeters()).hasSize(23);
        assertThat(registry.find("ucp.connections.active").tag("pool", "orders").gauge().value()).isEqualTo(7);
        assertThat(registry.find("ucp.connections.created").tag("pool", "orders").functionCounter().count()).isEqualTo(20);

        TimeGauge averageAcquire = registry.find("ucp.connections.acquire.average").tag("pool", "orders").timeGauge();
        assertThat(averageAcquire.value(TimeUnit.MILLISECONDS)).isEqualTo(4);

        FunctionTimer acquire = registry.find("ucp.connections.acquire").tag("pool", "orders").functionTimer();
        assertThat(acquire.count()).isEqualTo(35);
        assertThat(acquire.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(70);

        statistics.set("getBorrowedConnectionsCount", 9);
        statistics.set("getCumulativeSuccessfulConnectionWaitTime", 90L);
        Gauge activeGauge = registry.find("ucp.connections.active").tag("pool", "orders").gauge();
        assertThat(activeGauge.value()).isEqualTo(9);
        assertThat(acquire.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(90);
    }

    @Test
    void skipsCreationAttemptsWhenUnsupportedByUcp() throws Exception {
        PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setConnectionPoolName("legacy");
        UcpTestStatistics statistics = new UcpTestStatistics();
        statistics.unsupported("getCumulativeConnectionCreationAttempts");

        MeterRegistry registry = new SimpleMeterRegistry();
        new UcpMetrics(poolDataSource, statistics::statistics).bindTo(registry);

        assertThat(registry.find("ucp.connections.creation.attempts").tag("pool", "legacy").meter()).isNull();
        assertThat(registry.find("ucp.connections.active").tag("pool", "legacy").gauge().value()).isEqualTo(0);
    }
}
