// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;

/**
 * Injects a VaultPropertySource for each OCI Vault property source specified in the application properties.
 * OCI Vault property sources will only be loaded if the com.oracle.cloud.spring.vault.VaulTemplate class is on the classpath.
 */
public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (areClassesLoaded()) {
            VaultPropertySource.configure(environment);
        }
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    private boolean areClassesLoaded() {
        return ClassUtils.isPresent("com.oracle.cloud.spring.vault.VaultTemplate", VaultEnvironmentPostProcessor.class.getClassLoader());
    }
}
