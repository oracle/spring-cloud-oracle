/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.notification;

import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.NotificationDataPlane;
import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(NotificationAutoConfiguration.class))
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
            return mock(NotificationDataPlane.class);
        }

        @Bean
        NotificationControlPlane notificationCPClient() {
            return mock(NotificationControlPlane.class);
        }
    }
}
