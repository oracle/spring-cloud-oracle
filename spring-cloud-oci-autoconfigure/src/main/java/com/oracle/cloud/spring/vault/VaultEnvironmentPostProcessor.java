// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import java.util.List;
import java.util.stream.Collectors;

import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.secrets.Secrets;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProperties;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import com.oracle.cloud.spring.autoconfigure.core.RegionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;

import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.createRegionProvider;
import static com.oracle.cloud.spring.vault.VaultAutoConfiguration.createSecretsClient;

public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (secretsClassLoaded()) {
            Binder binder = Binder.get(environment);
            CredentialsProperties credentialsProperties = binder.bind(CredentialsProperties.PREFIX, Bindable.of(CredentialsProperties.class))
                    .orElse(new CredentialsProperties());
            RegionProperties regionProperties = binder.bind(RegionProperties.PREFIX, Bindable.of(RegionProperties.class))
                    .orElse(new RegionProperties());
            VaultProperties vaultProperties = binder.bind(VaultProperties.PREFIX, Bindable.of(VaultProperties.class))
                    .orElse(new VaultProperties());
            RegionProvider regionProvider = createRegionProvider(regionProperties);
            CredentialsProvider credentialsProvider = new CredentialsProvider(credentialsProperties);
            Secrets vaultSecretsClient = createSecretsClient(regionProvider, credentialsProvider);

            List<String> secretIds = vaultProperties.getPropertySources().stream()
                    .map(VaultPropertySources::getSecretId)
                    .collect(Collectors.toList());
            VaultPropertyLoader vaultPropertyLoader = new VaultPropertyLoader(vaultSecretsClient, secretIds)
        }
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    private boolean secretsClassLoaded() {
        return ClassUtils.isPresent("com.oracle.bmc.secrets.Secrets", VaultEnvironmentPostProcessor.class.getClassLoader());
    }
}
