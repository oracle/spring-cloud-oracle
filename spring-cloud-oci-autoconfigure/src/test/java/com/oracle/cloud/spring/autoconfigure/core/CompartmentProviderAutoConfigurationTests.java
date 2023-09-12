/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.util.Assert;

class CompartmentProviderAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(
                    CompartmentProviderAutoConfiguration.class));

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            CompartmentProperties config = context.getBean(CompartmentProperties.class);
                            Assert.isTrue(config.getStatic() == null);
                            Assert.isTrue(!config.isStatic());
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.compartment.static=demoCompartment")
                .run(
                        context -> {
                            CompartmentProperties config = context.getBean(CompartmentProperties.class);
                            Assert.isTrue(config.getStatic().equals("demoCompartment"));
                            Assert.isTrue(config.isStatic());
                        });
    }
}
