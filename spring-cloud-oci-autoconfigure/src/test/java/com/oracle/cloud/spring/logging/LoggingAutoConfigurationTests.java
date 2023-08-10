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
import org.springframework.util.Assert;

import static org.mockito.Mockito.mock;

public class LoggingAutoConfigurationTests {
    private ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        this.contextRunner
                .run(
                        context -> {
                            LoggingProperties config = context.getBean(LoggingProperties.class);
                            Assert.isTrue(config.getLogId() == null);
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        this.contextRunner
                .withPropertyValues("spring.cloud.oci.logging.logId=demoLogId")
                .run(
                        context -> {
                            LoggingProperties config = context.getBean(LoggingProperties.class);
                            Assert.isTrue(config.getLogId().equals("demoLogId"));
                        });
    }

    @Test
    void testConfigurationDefaultsAreAsExpected() {
        this.contextRunner
                .run(
                        context -> {
                            String[] logServiceBeanNames = context.getBeanNamesForType(LogService.class);
                            Assert.isTrue(logServiceBeanNames.length > 0);
                            LogService logService = context.getBean(LogService.class);
                            Assert.isTrue(logService != null);
                        });
    }

    @Test
    void testConfigurationConfiguredAreAsExpected() {
        this.contextRunner
                .withPropertyValues("spring.cloud.oci.logging.enabled=false")
                .run(
                        context -> {
                            String[] logServiceBeanNames = context.getBeanNamesForType(LogService.class);
                            Assert.isTrue(logServiceBeanNames.length == 0);
                        });
    }

    @Configuration
    static class TestSpecificConfigurationBeans {
        @Bean
        public Logging logging() {
            return mock(Logging.class);
        }
    }

}
