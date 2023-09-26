/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging;

import com.oracle.bmc.loggingingestion.Logging;
import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            LoggingProperties config = context.getBean(LoggingProperties.class);
                            assertNull(config.getLogId());
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.logging.logId=demoLogId")
                .run(
                        context -> {
                            LoggingProperties config = context.getBean(LoggingProperties.class);
                            assertEquals(config.getLogId(), "demoLogId");
                        });
    }

    @Test
    void testConfigurationDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            String[] logServiceBeanNames = context.getBeanNamesForType(LogService.class);
                            assertTrue(logServiceBeanNames.length > 0);
                            LogService logService = context.getBean(LogService.class);
                            assertNotNull(logService);
                        });
    }

    @Test
    void testConfigurationConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.logging.enabled=false")
                .run(
                        context -> {
                            String[] logServiceBeanNames = context.getBeanNamesForType(LogService.class);
                            assertEquals(logServiceBeanNames.length, 0);
                        });
    }

    @Configuration
    static class TestSpecificConfigurationBeans {
        @Bean
        Logging logging() {
            return mock(Logging.class);
        }
    }

}
