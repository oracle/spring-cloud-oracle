/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.function;

import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.functions.FunctionsInvoke;
import com.oracle.bmc.functions.FunctionsInvokeClient;
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
 * Auto-configuration for initializing the OCI Function component.
 * Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 * {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 * for loading the Authentication configuration
 *
 * @see Function
 */
@AutoConfiguration
@ConditionalOnClass({Function.class})
@ConditionalOnProperty(name = "spring.cloud.oci.function.enabled", havingValue = "true", matchIfMissing = true)
public class FunctionAutoConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(Function.class)
    Function getFunctionImpl(FunctionsInvoke functionsInvokeClient) {
        return new FunctionImpl(functionsInvokeClient);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    FunctionsInvoke functionsInvokeClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                                          @Qualifier(credentialsProviderQualifier) CredentialsProvider cp) {
        FunctionsInvoke functionsInvokeClient = FunctionsInvokeClient.builder().build(cp.getAuthenticationDetailsProvider());

        if (regionProvider.getRegion() != null) functionsInvokeClient.setRegion(regionProvider.getRegion());
        return functionsInvokeClient;
    }

}
