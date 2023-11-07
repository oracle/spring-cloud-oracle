/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.queue;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.queue.QueueAdminClient;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import com.oracle.cloud.spring.core.region.StaticRegionProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QueueAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(QueueAutoConfiguration.class,
                            RefreshAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            String[] queueBeanNames = context.getBeanNamesForType(Queue.class);
                            assertTrue(queueBeanNames.length > 0);
                            Queue queue = context.getBean(Queue.class);
                            assertNotNull(queue);
                        });
    }

    @Test
    void testConfigurationConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.queue.enabled=false")
                .run(
                        context -> {
                            String[] queueBeanNames = context.getBeanNamesForType(Queue.class);
                            assertEquals(queueBeanNames.length, 0);
                        });
    }

    @Configuration
    static class TestSpecificConfigurationBeans {
        @Bean
        QueueAdminClient queueAdminClient() {
            QueueAutoConfiguration configuration = new QueueAutoConfiguration();
            QueueAdminClient queueAdminClient = null;
            try (MockedStatic mocked = mockStatic(QueueAdminClient.class)) {
                QueueAdminClient.Builder builder = mock(QueueAdminClient.Builder.class);
                when(QueueAdminClient.builder()).thenReturn(builder);
                QueueAdminClient mockedQueueAdminClient = mock(QueueAdminClient.class);
                CredentialsProvider credentialsProvider =
                        mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                when(builder.build(credentialsProvider.getAuthenticationDetailsProvider())).thenReturn(mockedQueueAdminClient);
                queueAdminClient = configuration.queueAdminClient(regionProvider,
                        credentialsProvider);
                assertNotNull(queueAdminClient);
            }
            return queueAdminClient;
        }

        @Bean
        QueueClient queueClient() {
            QueueAutoConfiguration configuration = new QueueAutoConfiguration();
            QueueClient queueClient = null;
            try (MockedStatic mocked = mockStatic(QueueClient.class)) {
                QueueClient.Builder builder = mock(QueueClient.Builder.class);
                when(QueueClient.builder()).thenReturn(builder);
                QueueClient mockedQueueClient = mock(QueueClient.class);
                CredentialsProvider credentialsProvider =
                        mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                when(builder.build(credentialsProvider.getAuthenticationDetailsProvider())).thenReturn(mockedQueueClient);
                queueClient = configuration.queueClient(regionProvider,
                        credentialsProvider);
                assertNotNull(queueClient);
            }
            return queueClient;
        }
    }
}
