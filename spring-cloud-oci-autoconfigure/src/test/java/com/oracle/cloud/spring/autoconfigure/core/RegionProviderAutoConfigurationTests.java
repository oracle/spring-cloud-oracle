/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
class RegionProviderAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(
                    RegionProviderAutoConfiguration.class));

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            RegionProperties config = context.getBean(RegionProperties.class);
                            assertEquals(config.isStatic(), false);
                            assertNull(config.getStatic());
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.region.static=us-phoenix-1")
                .run(
                        context -> {
                           RegionProperties config = context.getBean(RegionProperties.class);
                            assertEquals(config.getStatic(), "us-phoenix-1");
                            assertEquals(config.isStatic(), true);
                        });
    }
}
