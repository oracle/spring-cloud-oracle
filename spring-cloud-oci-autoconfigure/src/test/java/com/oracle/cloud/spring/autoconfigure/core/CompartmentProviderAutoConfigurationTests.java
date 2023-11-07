/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.cloud.spring.core.compartment.CompartmentProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

class CompartmentProviderAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(
                    CompartmentProviderAutoConfiguration.class, RefreshAutoConfiguration.class));

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            CompartmentProperties config = context.getBean(CompartmentProperties.class);
                            assertNull(config.getStatic());
                            assertFalse(config.isStatic());
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.compartment.static=demoCompartment")
                .run(
                        context -> {
                            CompartmentProperties config = context.getBean(CompartmentProperties.class);
                            assertEquals(config.getStatic(), "demoCompartment");
                            assertTrue(config.isStatic());
                        });
    }

    @Test
    void testCompartmentProvider() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.compartment.static=demoCompartment")
                .run(
                        context -> {
                            CompartmentProvider provider = context.getBean(CompartmentProvider.class);
                            assertEquals(provider.getCompartmentOCID(), "demoCompartment");
                        });
    }
}
