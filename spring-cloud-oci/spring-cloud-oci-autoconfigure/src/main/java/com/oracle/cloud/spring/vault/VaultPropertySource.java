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
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.CollectionUtils;

import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.createRegionProvider;
import static com.oracle.cloud.spring.vault.VaultAutoConfiguration.createSecretsClient;
import static com.oracle.cloud.spring.vault.VaultAutoConfiguration.createVaultClient;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

public class VaultPropertySource extends EnumerablePropertySource<VaultPropertyLoader> {
    public VaultPropertySource(String name, VaultPropertyLoader source) {
        super(name, source);
    }

    public static void configure(ConfigurableEnvironment environment) {
        // Load Vault Properties
        Binder binder = Binder.get(environment);
        CredentialsProperties credentialsProperties = binder.bind(CredentialsProperties.PREFIX, Bindable.of(CredentialsProperties.class))
                .orElse(new CredentialsProperties());
        RegionProperties regionProperties = binder.bind(RegionProperties.PREFIX, Bindable.of(RegionProperties.class))
                .orElse(new RegionProperties());
        VaultProperties vaultProperties = binder.bind(VaultProperties.PREFIX, Bindable.of(VaultProperties.class))
                .orElse(new VaultProperties());

        final var vaultPropertySourceProperties = vaultProperties.getPropertySources();
        if(CollectionUtils.isEmpty(vaultPropertySourceProperties)) {
            //No need to waste time or possibly fail to create clients when there are no sources to populate.
            return;
        }

        // Create vault/secrets clients
        RegionProvider regionProvider = createRegionProvider(regionProperties);
        CredentialsProvider credentialsProvider = getCredentialsProvider(credentialsProperties);
        Secrets secretsClient = createSecretsClient(regionProvider, credentialsProvider);
        Vaults vaultClient = createVaultClient(regionProvider, credentialsProvider);

        // Inject VaultPropertySources into the system property sources
        MutablePropertySources propertySources = environment.getPropertySources();
        for (VaultPropertySourceProperties properties : vaultProperties.getPropertySources()) {
            VaultTemplate vaultTemplate = new VaultTemplateImpl(vaultClient, secretsClient, properties.getVaultId(), vaultProperties.getCompartment());
            VaultPropertyLoader vaultPropertyLoader = new VaultPropertyLoader(vaultTemplate, vaultProperties.getPropertyRefreshInterval());
            VaultPropertySource vaultPropertySource = new VaultPropertySource(properties.getVaultId(), vaultPropertyLoader);
            if (propertySources.contains(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                propertySources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, vaultPropertySource);
            } else {
                propertySources.addFirst(vaultPropertySource);
            }
        }
    }

    private static CredentialsProvider getCredentialsProvider(CredentialsProperties credentialsProperties) {
        try {
            return new CredentialsProvider(credentialsProperties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getPropertyNames() {
        return source.getPropertyNames();
    }

    @Override
    public Object getProperty(String name) {
        return source.getProperty(name);
    }

    @Override
    public boolean containsProperty(String name) {
        return source.containsProperty(name);
    }
}
