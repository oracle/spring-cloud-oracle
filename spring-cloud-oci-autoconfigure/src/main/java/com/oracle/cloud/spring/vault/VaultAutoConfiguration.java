// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;


import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.vault.Vaults;
import com.oracle.bmc.vault.VaultsClient;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import static com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration.credentialsProviderQualifier;
import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.regionProviderQualifier;

/**
 * Auto-configuration for initializing the OCI Vault component.
 *  Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 *  {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 *  for loading the Authentication configuration
 *
 * @see Vault
 */
@AutoConfiguration
@ConditionalOnClass({Vault.class})
@EnableConfigurationProperties(VaultProperties.class)
@ConditionalOnProperty(name = "spring.cloud.oci.vault.enabled", havingValue = "true", matchIfMissing = true)
public class VaultAutoConfiguration {
    private final VaultProperties properties;

    public VaultAutoConfiguration(VaultProperties properties) {
        this.properties = properties;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(Vault.class)
    public Vault vault(Vaults vaults, Secrets secrets) {
        return new VaultImpl(vaults, secrets, properties.getVaultId(), properties.getCompartment());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    public Vaults vaults(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                         @Qualifier(credentialsProviderQualifier)
                         CredentialsProvider cp) {
        Vaults vaults = VaultsClient.builder()
                .build(cp.getAuthenticationDetailsProvider());
        if (regionProvider.getRegion() != null) {
            vaults.setRegion(regionProvider.getRegion());
        }
        return vaults;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    public Secrets secrets(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                          @Qualifier(credentialsProviderQualifier)
                         CredentialsProvider cp) {
        Secrets secrets = SecretsClient.builder()
                .build(cp.getAuthenticationDetailsProvider());
        if (regionProvider.getRegion() != null) {
            secrets.setRegion(regionProvider.getRegion());
        }
        return secrets;
    }
}

