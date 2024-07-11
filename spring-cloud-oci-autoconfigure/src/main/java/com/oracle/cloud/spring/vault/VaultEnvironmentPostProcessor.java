// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import java.io.IOException;

import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.vault.Vaults;
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
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.ClassUtils;

import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.createRegionProvider;
import static com.oracle.cloud.spring.vault.VaultAutoConfiguration.createSecretsClient;
import static com.oracle.cloud.spring.vault.VaultAutoConfiguration.createVaultClient;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

/**
 * Injects a VaultPropertySource for each OCI Vault property source specified in the application properties.
 * OCI Vault property sources will only be loaded if the com.oracle.cloud.spring.vault.Vault class is on the classpath.
 */
public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (areClassesLoaded()) {
            // Load Vault Properties
            Binder binder = Binder.get(environment);
            CredentialsProperties credentialsProperties = binder.bind(CredentialsProperties.PREFIX, Bindable.of(CredentialsProperties.class))
                    .orElse(new CredentialsProperties());
            RegionProperties regionProperties = binder.bind(RegionProperties.PREFIX, Bindable.of(RegionProperties.class))
                    .orElse(new RegionProperties());
            VaultProperties vaultProperties = binder.bind(VaultProperties.PREFIX, Bindable.of(VaultProperties.class))
                    .orElse(new VaultProperties());

            // Create vault/secrets clients
            RegionProvider regionProvider = createRegionProvider(regionProperties);
            CredentialsProvider credentialsProvider = getCredentialsProvider(credentialsProperties);
            Secrets secretsClient = createSecretsClient(regionProvider, credentialsProvider);
            Vaults vaultClient = createVaultClient(regionProvider, credentialsProvider);

            // Inject VaultPropertySources into the system property sources
            MutablePropertySources propertySources = environment.getPropertySources();
            for (VaultPropertySourceProperties properties : vaultProperties.getPropertySources()) {
                Vault vault = new VaultImpl(vaultClient, secretsClient, properties.getVaultId(), vaultProperties.getCompartment());
                VaultPropertyLoader vaultPropertyLoader = new VaultPropertyLoader(vault, vaultProperties.getPropertyRefreshInterval());
                VaultPropertySource vaultPropertySource = new VaultPropertySource(properties.getVaultId(), vaultPropertyLoader);
                if (propertySources.contains(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                    propertySources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, vaultPropertySource);
                } else {
                    propertySources.addFirst(vaultPropertySource);
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    private boolean areClassesLoaded() {
        return ClassUtils.isPresent("com.oracle.cloud.spring.vault.Vault", VaultEnvironmentPostProcessor.class.getClassLoader());
    }

    private CredentialsProvider getCredentialsProvider(CredentialsProperties credentialsProperties) {
        try {
            return new CredentialsProvider(credentialsProperties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
