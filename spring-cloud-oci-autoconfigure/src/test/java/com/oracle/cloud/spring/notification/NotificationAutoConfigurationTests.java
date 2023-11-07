/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.notification;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.NotificationControlPlaneClient;
import com.oracle.bmc.ons.NotificationDataPlane;
import com.oracle.bmc.ons.NotificationDataPlaneClient;
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class NotificationAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(NotificationAutoConfiguration.class,
                            RefreshAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            String[] notificationBeanNames = context.getBeanNamesForType(Notification.class);
                            assertTrue(notificationBeanNames.length > 0);
                            Notification notification = context.getBean(Notification.class);
                            assertNotNull(notification);
                        });
    }

    @Test
    void testConfigurationConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.notification.enabled=false")
                .run(
                        context -> {
                            String[] notificationBeanNames = context.getBeanNamesForType(Notification.class);
                            assertEquals(notificationBeanNames.length, 0);
                        });
    }

    @Configuration
    static class TestSpecificConfigurationBeans {
        @Bean
        NotificationDataPlane notificationDPClient() {
            NotificationAutoConfiguration configuration = new NotificationAutoConfiguration();
            NotificationDataPlane notificationDataPlane = null;
            try (MockedStatic mocked = mockStatic(NotificationDataPlaneClient.class)) {
                NotificationDataPlaneClient.Builder builder = mock(NotificationDataPlaneClient.Builder.class);
                when(NotificationDataPlaneClient.builder()).thenReturn(builder);
                NotificationDataPlaneClient mockedNotificationDataPlane = mock(NotificationDataPlaneClient.class);
                CredentialsProvider credentialsProvider =
                        mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                when(builder.build(credentialsProvider.getAuthenticationDetailsProvider())).thenReturn(mockedNotificationDataPlane);
                notificationDataPlane = configuration.notificationDataPlaneClient(regionProvider,
                        credentialsProvider);
                assertNotNull(notificationDataPlane);
            }
            return notificationDataPlane;
        }

        @Bean
        NotificationControlPlane notificationCPClient() {
            NotificationAutoConfiguration configuration = new NotificationAutoConfiguration();
            NotificationControlPlane notificationControlPlane = null;
            try (MockedStatic mocked = mockStatic(NotificationControlPlaneClient.class)) {
                NotificationControlPlaneClient.Builder builder = mock(NotificationControlPlaneClient.Builder.class);
                when(NotificationControlPlaneClient.builder()).thenReturn(builder);
                NotificationControlPlaneClient mockedNotificationControlPlane = mock(NotificationControlPlaneClient.class);
                CredentialsProvider credentialsProvider =
                        mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                when(builder.build(credentialsProvider.getAuthenticationDetailsProvider())).thenReturn(mockedNotificationControlPlane);
                notificationControlPlane = configuration.notificationControlPlaneClient(regionProvider,
                        credentialsProvider);
                assertNotNull(notificationControlPlane);
            }
            return notificationControlPlane;
        }
    }
}
