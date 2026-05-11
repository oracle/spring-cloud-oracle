/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.io.IOException;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SessionTokenAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.auth.okeworkloadidentity.OkeWorkloadIdentityAuthenticationDetailsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * OCI authentication properties for Spring AI Oracle model clients.
 */
@ConfigurationProperties(prefix = OracleGenAiAuthenticationProperties.CONFIG_PREFIX)
public class OracleGenAiAuthenticationProperties {

    public static final String CONFIG_PREFIX = "spring.ai.oracle.auth";

    static final String USER_AGENT = "Oracle-SpringAI";

    private static final String DEFAULT_PROFILE = "DEFAULT";

    private Type type = Type.FILE;

    private String federationEndpoint;

    private String configFile;

    private String profile;

    private String tenantId;

    private String userId;

    private String fingerprint;

    private String privateKey;

    private String passPhrase;

    private String region;

    public enum Type {
        FILE,
        INSTANCE_PRINCIPAL,
        RESOURCE_PRINCIPAL,
        WORKLOAD_IDENTITY,
        SIMPLE,
        SESSION_TOKEN
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getFederationEndpoint() {
        return federationEndpoint;
    }

    public void setFederationEndpoint(String federationEndpoint) {
        this.federationEndpoint = federationEndpoint;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    BasicAuthenticationDetailsProvider createBasicAuthenticationDetailsProvider() throws IOException {
        BasicAuthenticationDetailsProvider authenticationDetailsProvider;
        switch (getType()) {
            case WORKLOAD_IDENTITY -> authenticationDetailsProvider =
                    OkeWorkloadIdentityAuthenticationDetailsProvider.builder()
                            .federationEndpoint(getFederationEndpoint())
                            .build();
            case RESOURCE_PRINCIPAL -> authenticationDetailsProvider =
                    ResourcePrincipalAuthenticationDetailsProvider.builder()
                            .federationEndpoint(getFederationEndpoint())
                            .build();
            case INSTANCE_PRINCIPAL -> authenticationDetailsProvider =
                    InstancePrincipalsAuthenticationDetailsProvider.builder()
                            .federationEndpoint(getFederationEndpoint())
                            .build();
            case SIMPLE -> {
                SimpleAuthenticationDetailsProvider.SimpleAuthenticationDetailsProviderBuilder builder =
                        SimpleAuthenticationDetailsProvider.builder()
                                .userId(getUserId())
                                .tenantId(getTenantId())
                                .fingerprint(getFingerprint())
                                .privateKeySupplier(new SimplePrivateKeySupplier(getPrivateKey()))
                                .passPhrase(getPassPhrase());
                if (StringUtils.hasText(getRegion())) {
                    builder.region(Region.valueOf(getRegion()));
                }
                authenticationDetailsProvider = builder.build();
            }
            case SESSION_TOKEN -> {
                String profile = StringUtils.hasText(getProfile()) ? getProfile() : DEFAULT_PROFILE;
                if (StringUtils.hasText(getConfigFile())) {
                    authenticationDetailsProvider = new SessionTokenAuthenticationDetailsProvider(getConfigFile(), profile);
                }
                else {
                    authenticationDetailsProvider = new SessionTokenAuthenticationDetailsProvider(profile);
                }
            }
            default -> {
                String profile = StringUtils.hasText(getProfile()) ? getProfile() : DEFAULT_PROFILE;
                if (StringUtils.hasText(getConfigFile())) {
                    authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(getConfigFile(), profile);
                }
                else {
                    authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(profile);
                }
            }
        }
        ClientRuntime.setClientUserAgent(USER_AGENT);
        return authenticationDetailsProvider;
    }
}
