/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.loggingingestion.Logging;
import com.oracle.bmc.loggingingestion.LoggingClient;
import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import com.oracle.cloud.spring.core.region.StaticRegionProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class LoggingAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class,
                            RefreshAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            assertThrows(Exception.class, () -> {LoggingProperties config =
                                    context.getBean(LoggingProperties.class);});
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
                .withPropertyValues("spring.cloud.oci.logging.logId=demoLogId")
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
            LoggingProperties properties = new LoggingProperties();
            LoggingAutoConfiguration configuration = new LoggingAutoConfiguration(properties);
            Logging logging = null;
            try (MockedStatic mocked = mockStatic(LoggingClient.class)) {
                LoggingClient.Builder builder = mock(LoggingClient.Builder.class);
                when(LoggingClient.builder()).thenReturn(builder);
                LoggingClient mockedLoggingClient = mock(LoggingClient.class);
                CredentialsProvider credentialsProvider =
                        mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                when(builder.build(credentialsProvider.getAuthenticationDetailsProvider())).thenReturn(mockedLoggingClient);
                logging = configuration.loggingClient(regionProvider, credentialsProvider);
                assertNotNull(logging);
            }
            return logging;
        }
    }

}
