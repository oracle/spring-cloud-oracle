/*
 ** Copyright (c) 2024, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.oracle.cloud.spring.nosql;

import java.io.IOException;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProperties;
import com.oracle.nosql.spring.data.config.NosqlDbConfig;
import oracle.nosql.driver.iam.SignatureProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.regionProviderQualifier;

@AutoConfiguration
@ConditionalOnClass(NosqlDbConfig.class)
@ConditionalOnProperty(name = "spring.cloud.oci.nosql.enabled", havingValue = "true", matchIfMissing = true)
public class NoSQLAutoConfiguration {
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    public NosqlDbConfig nosqlDbConfig(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                                       CredentialsProperties properties) throws IOException {
        String profile = properties.hasProfile() ? properties.getProfile() : "DEFAULT";
        SignatureProvider signatureProvider;
        switch (properties.getType()) {
            case WORKLOAD_IDENTITY -> signatureProvider = SignatureProvider.createWithOkeWorkloadIdentity();
            case RESOURCE_PRINCIPAL -> signatureProvider = SignatureProvider.createWithResourcePrincipal();
            case INSTANCE_PRINCIPAL ->
                    signatureProvider = SignatureProvider.createWithInstancePrincipal();
            case SIMPLE -> {
                String passPhrase = properties.getPassPhrase();
                signatureProvider = new SignatureProvider(
                    properties.getTenantId(),
                    properties.getUserId(),
                    properties.getFingerprint(),
                    properties.getPrivateKey(),
                    passPhrase != null ? passPhrase.toCharArray() : null
                );
            }
            case SESSION_TOKEN -> {
                if (properties.hasFile()) {
                    signatureProvider = SignatureProvider.createWithSessionToken(properties.getFile(), profile);
                } else {
                    signatureProvider = SignatureProvider.createWithSessionToken(profile);
                }
            }
            default -> {
                if (properties.hasFile()) {
                    signatureProvider = new SignatureProvider(properties.getFile(), profile);
                } else {
                    signatureProvider = new SignatureProvider(profile);
                }

            }
        }
        Region region = regionProvider.getRegion();
        if (region == null) {
            throw new IllegalStateException("OCI region must be configured for Oracle NoSQL Database");
        }
        return new NosqlDbConfig(region.getRegionId(), signatureProvider);
    }
}
