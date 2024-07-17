/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.streaming;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.streaming.Stream;
import com.oracle.bmc.streaming.StreamAdmin;
import com.oracle.bmc.streaming.StreamAdminClient;
import com.oracle.bmc.streaming.StreamClient;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import java.text.MessageFormat;

import static com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration.credentialsProviderQualifier;
import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.regionProviderQualifier;

/**
 * Auto-configuration for initializing the OCI streaming component.
 *  Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 *  {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 *  for loading the Authentication configuration
 *
 * @see Streaming
 */
@AutoConfiguration
@ConditionalOnClass({Streaming.class})
@ConditionalOnProperty(name = "spring.cloud.oci.streaming.enabled", havingValue = "true", matchIfMissing = true)
public class StreamingAutoConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(Streaming.class)
    Streaming getStreamingImpl(Stream stream, StreamAdmin streamAdmin) {
        return new StreamingImpl(stream, streamAdmin);
    }

    // TODO: StreamClient has been deprecated
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    Stream streamingClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                        @Qualifier(credentialsProviderQualifier)
                                CredentialsProvider cp) {
        Stream stream = new StreamClient(cp.getAuthenticationDetailsProvider());
        Region regionProviderRegion = regionProvider.getRegion();
        if (regionProviderRegion != null) {
            stream.setEndpoint(MessageFormat.format("https://streaming.{0}.oci.oraclecloud.com",
                    regionProviderRegion.getRegionId()));
        }
        return stream;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    StreamAdmin streamingAdminClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                           @Qualifier(credentialsProviderQualifier)
                                   CredentialsProvider cp) {
        StreamAdmin streamAdmin = StreamAdminClient.builder().build(cp.getAuthenticationDetailsProvider());
        Region regionProviderRegion = regionProvider.getRegion();
        if (regionProviderRegion != null) {
            streamAdmin.setRegion(regionProvider.getRegion());
        }
        return streamAdmin;
    }
}
