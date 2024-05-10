/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

class RegionProviderAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(
                    RegionProviderAutoConfiguration.class, RefreshAutoConfiguration.class));

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            RegionProperties config = context.getBean(RegionProperties.class);
                            assertFalse(config.isStatic());
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
                            assertTrue(config.isStatic());
                        });
    }

    @Test
    void testRegionProvider() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.region.static=us-phoenix-1")
                .run(
                        context -> {
                            RegionProvider provider = context.getBean(RegionProvider.class);
                            assertEquals(provider.getRegion(), Region.US_PHOENIX_1);
                        });
    }
}
