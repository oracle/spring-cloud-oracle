/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.*;
import com.oracle.bmc.auth.okeworkloadidentity.OkeWorkloadIdentityAuthenticationDetailsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static com.oracle.cloud.spring.autoconfigure.core.CredentialsProperties.ConfigType.FILE;

/**
 * Auto-configuration settings related to Credentials.
 */
@ConfigurationProperties(prefix = CredentialsProperties.PREFIX)
public class CredentialsProperties {
    public static final String PREFIX = "spring.cloud.oci.config";

    private static final String PROFILE_DEFAULT = "DEFAULT";
    public static final String USER_AGENT_SPRING_CLOUD = "Oracle-SpringCloud";


    public enum ConfigType {
        FILE,
        INSTANCE_PRINCIPAL,
        RESOURCE_PRINCIPAL,
        WORKLOAD_IDENTITY,
        SIMPLE,
        SESSION_TOKEN
    }

    @Nullable
    private ConfigType type;

    @Nullable
    private String profile;

    @Nullable
    private String file;

    @Nullable
    private String tenantId;

    @Nullable
    private String userId;

    @Nullable
    private String fingerprint;

    @Nullable
    private String privateKey;

    @Nullable
    private String passPhrase;

    @Nullable
    private String region;

    @Nullable
    public String getProfile() {
        return profile;
    }

    public boolean hasProfile() {
        return StringUtils.hasText(profile);
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    @Nullable
    public String getFile() {
        return file;
    }

    public boolean hasFile() {
        return StringUtils.hasText(file);
    }

    public void setFile(String file) {
        this.file = file;
    }

    public ConfigType getType() {
        return type != null ? type : FILE;
    }

    public void setType(ConfigType type) {
        this.type = type;
    }

    @Nullable
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(@Nullable String tenantId) {
        this.tenantId = tenantId;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    public void setUserId(@Nullable String userId) {
        this.userId = userId;
    }

    @Nullable
    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(@Nullable String fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Nullable
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(@Nullable String privateKey) {
        this.privateKey = privateKey;
    }

    @Nullable
    public String getPassPhrase() {
        return passPhrase;
    }

    public void setPassPhrase(@Nullable String passPhrase) {
        this.passPhrase = passPhrase;
    }

    @Nullable
    public String getRegion() {
        return region;
    }

    public void setRegion(@Nullable String region) {
        this.region = region;
    }

    public BasicAuthenticationDetailsProvider createBasicAuthenticationDetailsProvider() throws IOException {
        BasicAuthenticationDetailsProvider authenticationDetailsProvider;

        switch (getType()) {
            case WORKLOAD_IDENTITY:
                authenticationDetailsProvider = OkeWorkloadIdentityAuthenticationDetailsProvider.builder().build();
                break;
            case RESOURCE_PRINCIPAL:
                authenticationDetailsProvider = ResourcePrincipalAuthenticationDetailsProvider.builder().build();
                break;
            case INSTANCE_PRINCIPAL:
                authenticationDetailsProvider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
                break;
            case SIMPLE:
                SimpleAuthenticationDetailsProvider.SimpleAuthenticationDetailsProviderBuilder builder = SimpleAuthenticationDetailsProvider.builder()
                        .userId(getUserId())
                        .tenantId(getTenantId())
                        .fingerprint(getFingerprint())
                        .privateKeySupplier(new SimplePrivateKeySupplier(getPrivateKey()))
                        .passPhrase(getPassPhrase());
                if (getRegion() != null) {
                    builder.region(Region.valueOf(getRegion()));
                }
                authenticationDetailsProvider = builder.build();
                break;
            case SESSION_TOKEN:
                String configProfile = hasProfile() ? getProfile() : PROFILE_DEFAULT;

                if (hasFile()) {
                    authenticationDetailsProvider = new SessionTokenAuthenticationDetailsProvider(getFile(), configProfile);
                } else {
                    authenticationDetailsProvider = new SessionTokenAuthenticationDetailsProvider(configProfile);
                }
                break;
            default:
                String profile = hasProfile() ? getProfile() : PROFILE_DEFAULT;

                if (hasFile()) {
                    authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(getFile(), profile);
                } else {
                    authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(profile);
                }

                break;
        }
        ClientRuntime.setClientUserAgent(USER_AGENT_SPRING_CLOUD);
        return authenticationDetailsProvider;
    }
}
