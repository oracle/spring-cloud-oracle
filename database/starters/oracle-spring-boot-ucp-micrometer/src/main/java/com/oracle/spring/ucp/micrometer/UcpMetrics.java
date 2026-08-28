// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp.micrometer;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import oracle.ucp.UniversalConnectionPoolStatistics;
import oracle.ucp.jdbc.PoolDataSource;
import org.jspecify.annotations.NonNull;

/**
 * Binds Oracle Universal Connection Pool runtime statistics to Micrometer.
 */
public final class UcpMetrics implements MeterBinder {

    private static final String METRIC_PREFIX = "ucp.connections";
    private static final String POOL_TAG = "pool";

    private final PoolDataSource poolDataSource;
    private final Supplier<? extends UniversalConnectionPoolStatistics> statisticsSupplier;
    private final String poolName;

    UcpMetrics(PoolDataSource poolDataSource, String dataSourceBeanName) {
        this(poolDataSource, poolDataSource::getStatistics, dataSourceBeanName);
    }

    UcpMetrics(PoolDataSource poolDataSource, Supplier<? extends UniversalConnectionPoolStatistics> statisticsSupplier,
            String dataSourceBeanName) {
        this.poolDataSource = poolDataSource;
        this.statisticsSupplier = statisticsSupplier;
        this.poolName = poolName(poolDataSource.getConnectionPoolName(), dataSourceBeanName);
    }

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        registerGauge(registry, "", "Total connections", UniversalConnectionPoolStatistics::getTotalConnectionsCount);
        registerGauge(registry, ".idle", "Available connections", UniversalConnectionPoolStatistics::getAvailableConnectionsCount);
        registerGauge(registry, ".active", "Borrowed connections", UniversalConnectionPoolStatistics::getBorrowedConnectionsCount);
        registerGauge(registry, ".pending", "Pending connection requests", UniversalConnectionPoolStatistics::getPendingRequestsCount);
        registerGauge(registry, ".max", "Maximum pool size", statistics -> poolDataSource.getMaxPoolSize());
        registerGauge(registry, ".min", "Minimum pool size", statistics -> poolDataSource.getMinPoolSize());
        registerGauge(registry, ".capacity", "Remaining pool capacity", UniversalConnectionPoolStatistics::getRemainingPoolCapacityCount);
        registerGauge(registry, ".peak", "Peak connections", UniversalConnectionPoolStatistics::getPeakConnectionsCount);
        registerGauge(registry, ".active.average", "Average borrowed connections", UniversalConnectionPoolStatistics::getAverageBorrowedConnectionsCount);
        registerGauge(registry, ".active.peak", "Peak borrowed connections", UniversalConnectionPoolStatistics::getPeakBorrowedConnectionsCount);
        registerGauge(registry, ".labeled", "Labeled connections", UniversalConnectionPoolStatistics::getLabeledConnectionsCount);
        registerGauge(registry, ".abandoned", "Abandoned connections reclaimed", UniversalConnectionPoolStatistics::getAbandonedConnectionsCount);

        registerTimeGauge(registry, ".acquire.average", "Average connection acquire time", UniversalConnectionPoolStatistics::getAverageConnectionWaitTime);
        registerTimeGauge(registry, ".acquire.peak", "Peak connection acquire time", UniversalConnectionPoolStatistics::getPeakConnectionWaitTime);

        registerCounter(registry, ".created", "Connections created", UniversalConnectionPoolStatistics::getConnectionsCreatedCount);
        registerCounter(registry, ".closed", "Connections closed", UniversalConnectionPoolStatistics::getConnectionsClosedCount);
        registerCounter(registry, ".borrowed", "Connections borrowed", UniversalConnectionPoolStatistics::getCumulativeConnectionBorrowedCount);
        registerCounter(registry, ".returned", "Connections returned", UniversalConnectionPoolStatistics::getCumulativeConnectionReturnedCount);
        if (supports(UniversalConnectionPoolStatistics::getCumulativeConnectionCreationAttempts)) {
            registerCounter(registry, ".creation.attempts", "Connection creation attempts", UniversalConnectionPoolStatistics::getCumulativeConnectionCreationAttempts);
        }

        registerTimer(registry, ".acquire", "Successful connection acquire time",
                UniversalConnectionPoolStatistics::getCumulativeSuccessfulConnectionWaitCount,
                UniversalConnectionPoolStatistics::getCumulativeSuccessfulConnectionWaitTime);
        registerTimer(registry, ".acquire.failed", "Failed connection acquire time",
                UniversalConnectionPoolStatistics::getCumulativeFailedConnectionWaitCount,
                UniversalConnectionPoolStatistics::getCumulativeFailedConnectionWaitTime);
        registerTimer(registry, ".acquire.total", "Total connection acquire time",
                statistics -> statistics.getCumulativeSuccessfulConnectionWaitCount() + statistics.getCumulativeFailedConnectionWaitCount(),
                UniversalConnectionPoolStatistics::getCumulativeConnectionWaitTime);
        registerTimer(registry, ".usage", "Connection usage time", UniversalConnectionPoolStatistics::getCumulativeConnectionReturnedCount,
                UniversalConnectionPoolStatistics::getCumulativeConnectionUseTime);
    }

    private void registerGauge(MeterRegistry registry, String suffix, String description,
            ToDoubleFunction<UniversalConnectionPoolStatistics> statistic) {
        Gauge.builder(metricName(suffix), this, metrics -> metrics.statistic(statistic))
                .description(description)
                .tag(POOL_TAG, poolName)
                .strongReference(true)
                .register(registry);
    }

    private void registerTimeGauge(MeterRegistry registry, String suffix, String description,
            ToDoubleFunction<UniversalConnectionPoolStatistics> statistic) {
        TimeGauge.builder(metricName(suffix), this, TimeUnit.MILLISECONDS, metrics -> metrics.statistic(statistic))
                .description(description)
                .tag(POOL_TAG, poolName)
                .strongReference(true)
                .register(registry);
    }

    private void registerCounter(MeterRegistry registry, String suffix, String description,
            ToDoubleFunction<UniversalConnectionPoolStatistics> statistic) {
        FunctionCounter.builder(metricName(suffix), this, metrics -> metrics.statistic(statistic))
                .description(description)
                .tag(POOL_TAG, poolName)
                .register(registry);
    }

    private void registerTimer(MeterRegistry registry, String suffix, String description,
            ToLongFunction<UniversalConnectionPoolStatistics> count, ToDoubleFunction<UniversalConnectionPoolStatistics> totalTime) {
        FunctionTimer.builder(metricName(suffix), this, metrics -> metrics.statistic(count), metrics -> metrics.statistic(totalTime), TimeUnit.MILLISECONDS)
                .description(description)
                .tag(POOL_TAG, poolName)
                .register(registry);
    }

    private String metricName(String suffix) {
        return METRIC_PREFIX + suffix;
    }

    private String poolName(String configuredPoolName, String dataSourceBeanName) {
        if (configuredPoolName != null && !configuredPoolName.isBlank()) {
            return configuredPoolName;
        }
        return dataSourceBeanName;
    }

    private boolean supports(ToDoubleFunction<UniversalConnectionPoolStatistics> statistic) {
        return !Double.isNaN(statistic(statistic));
    }

    private double statistic(ToDoubleFunction<UniversalConnectionPoolStatistics> statistic) {
        try {
            return statistic.applyAsDouble(statisticsSupplier.get());
        } catch (LinkageError | RuntimeException ex) {
            return Double.NaN;
        }
    }

    private long statistic(ToLongFunction<UniversalConnectionPoolStatistics> statistic) {
        try {
            return statistic.applyAsLong(statisticsSupplier.get());
        } catch (LinkageError | RuntimeException ex) {
            return 0;
        }
    }
}
