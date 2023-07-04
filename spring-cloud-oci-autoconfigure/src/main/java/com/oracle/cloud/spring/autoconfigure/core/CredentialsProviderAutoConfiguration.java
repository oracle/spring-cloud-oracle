/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * Auto-configuration for initializing the OCI Credentials provider type
 */
@AutoConfiguration
@ConditionalOnClass({AuthenticationDetailsProvider.class})
@EnableConfigurationProperties(CredentialsProperties.class)
public class CredentialsProviderAutoConfiguration {

    private static final String PROFILE_DEFAULT = "DEFAULT";
    private final CredentialsProperties properties;

    public CredentialsProviderAutoConfiguration(CredentialsProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a Authentication provider based on {@link CredentialsProperties.ConfigType} type
     * @return
     * @throws IOException
     */
    @Bean
    @ConditionalOnMissingBean
    public BasicAuthenticationDetailsProvider credentialsProvider() throws IOException {
        return createCredentialsProvider(properties);
    }

    public static BasicAuthenticationDetailsProvider createCredentialsProvider(CredentialsProperties properties) throws IOException {
        BasicAuthenticationDetailsProvider authenticationDetailsProvider;

        switch (properties.getType()) {
            case RESOURCE_PRINCIPAL:
                authenticationDetailsProvider = ResourcePrincipalAuthenticationDetailsProvider.builder().build();
                break;
            case INSTANCE_PRINCIPAL:
                authenticationDetailsProvider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
                break;
            case SIMPLE:
                SimpleAuthenticationDetailsProvider.SimpleAuthenticationDetailsProviderBuilder builder = SimpleAuthenticationDetailsProvider.builder()
                        .userId(properties.getUserId())
                        .tenantId(properties.getTenantId())
                        .fingerprint(properties.getFingerprint())
                        .privateKeySupplier(new SimplePrivateKeySupplier(properties.getPrivateKey()))
                        .passPhrase(properties.getPassPhrase())
                        .region(Region.valueOf(properties.getRegion()));

                authenticationDetailsProvider = builder.build();
                break;
            default:
                String profile = properties.hasProfile() ? properties.getProfile() : PROFILE_DEFAULT;

                if (properties.hasFile()) {
                    authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(properties.getFile(), profile);
                } else {
                    authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(profile);
                }

                break;
        }

        return authenticationDetailsProvider;
    }

}
