// Copyright (c) 2023, 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.adb;

import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.database.DatabaseClient;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import static com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration.credentialsProviderQualifier;
import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.regionProviderQualifier;

/**
 * Auto-configuration for initializing the OCI Autonomous Database component.
 * Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 * {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 * for loading the Authentication configuration
 *
 * @see com.oracle.cloud.spring.adb.AutonomousDb
 */
@AutoConfiguration
@ConditionalOnClass({AutonomousDb.class})
@ConditionalOnProperty(name = "spring.cloud.oci.adb.enabled", havingValue = "true", matchIfMissing = true)
public class AutonomousDbAutoConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(AutonomousDb.class)
    AutonomousDb getQueueImpl(DatabaseClient databaseClient) {
        return new AutonomousDbImpl(databaseClient);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    DatabaseClient databaseClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                                      @Qualifier(credentialsProviderQualifier)
                                              CredentialsProvider cp) {
        DatabaseClient databaseClient = DatabaseClient.builder().build(cp.getAuthenticationDetailsProvider());
        if (regionProvider.getRegion() != null)databaseClient.setRegion(regionProvider.getRegion());
        return databaseClient;
    }

}
