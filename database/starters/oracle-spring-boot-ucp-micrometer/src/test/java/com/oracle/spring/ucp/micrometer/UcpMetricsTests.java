// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp.micrometer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.junit.jupiter.api.Test;

class UcpMetricsTests {

    private static final String POOL_NAME_ATTRIBUTE = "db.client.connection.pool.name";
    private static final String STATE_ATTRIBUTE = "db.client.connection.state";

    @Test
    void bindsLivePoolStatistics() throws Exception {
        PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setConnectionPoolName("orders");
        poolDataSource.setMaxPoolSize(30);
        poolDataSource.setMinPoolSize(10);
        poolDataSource.setMinIdle(4);
        UcpTestStatistics statistics = statistics();

        MeterRegistry registry = new SimpleMeterRegistry();
        new UcpMetrics(poolDataSource, statistics::statistics, "orders").bindTo(registry);

        Collection<Meter> connectionCounts = registry.find("db.client.connection.count")
                .tag(POOL_NAME_ATTRIBUTE, "orders")
                .meters();
        assertThat(connectionCounts).hasSize(2);
        assertThat(connectionCounts)
                .extracting(meter -> meter.getId().getTag(STATE_ATTRIBUTE))
                .containsExactlyInAnyOrder("idle", "used");

        Gauge idle = semanticGauge(registry, "db.client.connection.count", "idle", "orders");
        Gauge used = semanticGauge(registry, "db.client.connection.count", "used", "orders");
        Gauge max = semanticGauge(registry, "db.client.connection.max", null, "orders");
        Gauge minIdle = semanticGauge(registry, "db.client.connection.idle.min", null, "orders");
        Gauge pendingRequests = semanticGauge(registry, "db.client.connection.pending_requests", null, "orders");

        assertThat(idle.value()).isEqualTo(5);
        assertThat(used.value()).isEqualTo(7);
        assertThat(max.value()).isEqualTo(30);
        assertThat(minIdle.value()).isEqualTo(4);
        assertThat(pendingRequests.value()).isEqualTo(2);
        assertThat(idle.getId().getDescription())
                .isEqualTo("The number of connections that are currently in state described by the state attribute");
        assertThat(max.getId().getDescription()).isEqualTo("The maximum number of open connections allowed");
        assertThat(minIdle.getId().getDescription()).isEqualTo("The minimum number of idle open connections allowed");
        assertThat(pendingRequests.getId().getDescription())
                .isEqualTo("The number of current pending requests for an open connection");
        assertThat(idle.getId().getBaseUnit()).isEqualTo("connections");
        assertThat(used.getId().getBaseUnit()).isEqualTo("connections");
        assertThat(max.getId().getBaseUnit()).isEqualTo("connections");
        assertThat(minIdle.getId().getBaseUnit()).isEqualTo("connections");
        assertThat(pendingRequests.getId().getBaseUnit()).isEqualTo("requests");

        assertSemanticMeterTags(registry);
        assertVendorMeters(registry);
        assertRemovedMetersAreAbsent(registry);

        Gauge created = registry.find("ucp.connections.created").tag("pool", "orders").gauge();
        Gauge closed = registry.find("ucp.connections.closed").tag("pool", "orders").gauge();
        assertThat(created.value()).isEqualTo(20);
        assertThat(closed.value()).isEqualTo(8);
        assertThat(created.getId().getDescription()).isEqualTo("Connections created by the current pool instance");
        assertThat(closed.getId().getDescription()).isEqualTo("Connections closed by the current pool instance");
        assertThat(registry.find("ucp.connections.created").tag("pool", "orders").functionCounter()).isNull();
        assertThat(registry.find("ucp.connections.closed").tag("pool", "orders").functionCounter()).isNull();

        FunctionCounter borrowed = registry.find("ucp.connections.borrowed").tag("pool", "orders").functionCounter();
        FunctionCounter returned = registry.find("ucp.connections.returned").tag("pool", "orders").functionCounter();
        FunctionCounter creationAttempts = registry.find("ucp.connections.creation.attempts").tag("pool", "orders").functionCounter();
        assertThat(borrowed.count()).isEqualTo(40);
        assertThat(returned.count()).isEqualTo(38);
        assertThat(creationAttempts.count()).isEqualTo(6);

        TimeGauge averageAcquire = registry.find("ucp.connections.acquire.average").tag("pool", "orders").timeGauge();
        assertThat(averageAcquire.value(TimeUnit.MILLISECONDS)).isEqualTo(4);

        FunctionTimer acquire = registry.find("ucp.connections.acquire").tag("pool", "orders").functionTimer();
        FunctionTimer failedAcquire = registry.find("ucp.connections.acquire.failed").tag("pool", "orders").functionTimer();
        FunctionTimer totalAcquire = registry.find("ucp.connections.acquire.total").tag("pool", "orders").functionTimer();
        FunctionTimer usage = registry.find("ucp.connections.usage").tag("pool", "orders").functionTimer();
        assertThat(acquire.count()).isEqualTo(35);
        assertThat(acquire.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(70);
        assertThat(failedAcquire.count()).isEqualTo(5);
        assertThat(failedAcquire.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(25);
        assertThat(totalAcquire.count()).isEqualTo(40);
        assertThat(totalAcquire.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(95);
        assertThat(usage.count()).isEqualTo(38);
        assertThat(usage.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(760);

        statistics.set("getAvailableConnectionsCount", 3);
        statistics.set("getBorrowedConnectionsCount", 9);
        statistics.set("getConnectionsCreatedCount", 2);
        statistics.set("getConnectionsClosedCount", 1);
        statistics.set("getCumulativeSuccessfulConnectionWaitTime", 90L);
        assertThat(idle.value()).isEqualTo(3);
        assertThat(used.value()).isEqualTo(9);
        assertThat(created.value()).isEqualTo(2);
        assertThat(closed.value()).isEqualTo(1);
        assertThat(acquire.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(90);
    }

    @Test
    void skipsIdleMinimumWhenUnsupportedByUcp() throws Exception {
        PoolDataSource legacyPool = PoolDataSourceFactory.getPoolDataSource();
        legacyPool.setConnectionPoolName("legacy");

        MeterRegistry registry = new SimpleMeterRegistry();
        new UcpMetrics(legacyPool, new UcpTestStatistics()::statistics, "legacy", pool -> {
            throw new NoSuchMethodError("getMinIdle");
        }).bindTo(registry);

        assertThat(registry.find("db.client.connection.idle.min").meter()).isNull();
        assertThat(semanticGauge(registry, "db.client.connection.max", null, "legacy")).isNotNull();
        assertThat(semanticGauge(registry, "db.client.connection.count", "idle", "legacy")).isNotNull();
    }

    @Test
    void skipsCreationAttemptsWhenUnsupportedByUcp() throws Exception {
        PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setConnectionPoolName("legacy");
        UcpTestStatistics statistics = new UcpTestStatistics();
        statistics.unsupported("getCumulativeConnectionCreationAttempts");

        MeterRegistry registry = new SimpleMeterRegistry();
        new UcpMetrics(poolDataSource, statistics::statistics, "legacy").bindTo(registry);

        assertThat(registry.find("ucp.connections.creation.attempts").tag("pool", "legacy").meter()).isNull();
        assertThat(semanticGauge(registry, "db.client.connection.count", "used", "legacy").value()).isEqualTo(0);
    }

    private UcpTestStatistics statistics() {
        UcpTestStatistics statistics = new UcpTestStatistics();
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
        return statistics;
    }

    private Gauge semanticGauge(MeterRegistry registry, String name, String state, String poolName) {
        Search search = registry.find(name).tag(POOL_NAME_ATTRIBUTE, poolName);
        if (state != null) {
            search.tag(STATE_ATTRIBUTE, state);
        }
        return search.gauge();
    }

    private void assertSemanticMeterTags(MeterRegistry registry) {
        for (Meter meter : registry.getMeters()) {
            if (!meter.getId().getName().startsWith("db.client.connection.")) {
                continue;
            }
            assertThat(meter.getId().getTag(POOL_NAME_ATTRIBUTE)).isEqualTo("orders");
            assertThat(meter.getId().getTag("pool")).isNull();
            if (meter.getId().getName().equals("db.client.connection.count")) {
                assertThat(meter.getId().getTags()).containsExactlyInAnyOrder(
                        Tag.of(POOL_NAME_ATTRIBUTE, "orders"),
                        Tag.of(STATE_ATTRIBUTE, meter.getId().getTag(STATE_ATTRIBUTE)));
            } else {
                assertThat(meter.getId().getTags()).containsExactly(Tag.of(POOL_NAME_ATTRIBUTE, "orders"));
            }
        }
    }

    private void assertVendorMeters(MeterRegistry registry) {
        assertThat(registry.find("ucp.connections").tag("pool", "orders").gauge()).isNotNull();
        assertThat(registry.find("ucp.connections.min").tag("pool", "orders").gauge().value()).isEqualTo(10);
        for (String name : List.of("ucp.connections.capacity", "ucp.connections.peak",
                "ucp.connections.active.average", "ucp.connections.active.peak", "ucp.connections.labeled",
                "ucp.connections.abandoned", "ucp.connections.created", "ucp.connections.closed")) {
            assertThat(registry.find(name).tag("pool", "orders").gauge()).as(name).isNotNull();
        }
        for (String name : List.of("ucp.connections.acquire.average", "ucp.connections.acquire.peak")) {
            assertThat(registry.find(name).tag("pool", "orders").timeGauge()).as(name).isNotNull();
        }
        for (String name : List.of("ucp.connections.borrowed", "ucp.connections.returned",
                "ucp.connections.creation.attempts")) {
            assertThat(registry.find(name).tag("pool", "orders").functionCounter()).as(name).isNotNull();
        }
        for (String name : List.of("ucp.connections.acquire", "ucp.connections.acquire.failed",
                "ucp.connections.acquire.total", "ucp.connections.usage")) {
            assertThat(registry.find(name).tag("pool", "orders").functionTimer()).as(name).isNotNull();
        }
        registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("ucp.connections"))
                .forEach(meter -> {
                    assertThat(meter.getId().getTag("pool")).isEqualTo("orders");
                    assertThat(meter.getId().getTag(POOL_NAME_ATTRIBUTE)).isNull();
                    assertThat(meter.getId().getTag(STATE_ATTRIBUTE)).isNull();
                });
    }

    private void assertRemovedMetersAreAbsent(MeterRegistry registry) {
        for (String name : List.of("ucp.connections.active", "ucp.connections.idle",
                "ucp.connections.pending", "ucp.connections.max")) {
            assertThat(registry.find(name).meter()).as(name).isNull();
        }
    }

}
