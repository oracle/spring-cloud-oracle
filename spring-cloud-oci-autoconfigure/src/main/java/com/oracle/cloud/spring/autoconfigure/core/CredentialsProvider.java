/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SessionTokenAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;

import java.io.IOException;

/**
 * Provider to wrap AuthenticationDetailsProvider for beans initialization
 */
public class CredentialsProvider {

    private static final String PROFILE_DEFAULT = "DEFAULT";
    public static final String USER_AGENT_SPRING_CLOUD = "Oracle-SpringCloud";

    private BasicAuthenticationDetailsProvider authenticationDetailsProvider;

    public CredentialsProvider(CredentialsProperties properties) throws IOException {
        this.authenticationDetailsProvider = createCredentialsProvider(properties);
    }

    /**
     * Initializes an Authentication provider based on {@link CredentialsProperties.ConfigType} type
     * @return BasicAuthenticationDetailsProvider
     * @throws IOException
     */
    private static BasicAuthenticationDetailsProvider createCredentialsProvider(CredentialsProperties properties)
            throws IOException {
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
                        .passPhrase(properties.getPassPhrase());
                if (properties.getRegion() != null) {
                    builder.region(Region.valueOf(properties.getRegion()));
                }
                authenticationDetailsProvider = builder.build();
                break;
            case SESSION_TOKEN:
                String configProfile = properties.hasProfile() ? properties.getProfile() : PROFILE_DEFAULT;

                if (properties.hasFile()) {
                    authenticationDetailsProvider = new SessionTokenAuthenticationDetailsProvider(properties.getFile(), configProfile);
                } else {
                    authenticationDetailsProvider = new SessionTokenAuthenticationDetailsProvider(configProfile);
                }
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
        ClientRuntime.setClientUserAgent(USER_AGENT_SPRING_CLOUD);
        return authenticationDetailsProvider;
    }

    public BasicAuthenticationDetailsProvider getAuthenticationDetailsProvider() {
        return authenticationDetailsProvider;
    }
}
