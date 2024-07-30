/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.queue;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.QueueAdminClient;
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
 * Auto-configuration for initializing the OCI Queue component.
 * Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 * {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 * for loading the Authentication configuration
 *
 * @see com.oracle.cloud.spring.queue.Queue
 */
@AutoConfiguration
@ConditionalOnClass({Queue.class})
@ConditionalOnProperty(name = "spring.cloud.oci.queue.enabled", havingValue = "true", matchIfMissing = true)
public class QueueAutoConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(Queue.class)
    Queue getQueueImpl(QueueAdminClient queueAdminClient, QueueClient queueClient) {
        return new QueueImpl(queueAdminClient, queueClient);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    QueueAdminClient queueAdminClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                                      @Qualifier(credentialsProviderQualifier)
                                              CredentialsProvider cp) {
        QueueAdminClient queueAdminClient = QueueAdminClient.builder().build(cp.getAuthenticationDetailsProvider());
        if (regionProvider.getRegion() != null) queueAdminClient.setRegion(regionProvider.getRegion());
        return queueAdminClient;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    QueueClient queueClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                            @Qualifier(credentialsProviderQualifier)
                                    CredentialsProvider cp) {
        QueueClient queueClient = QueueClient.builder().build(cp.getAuthenticationDetailsProvider());
        Region region = regionProvider.getRegion();
        if (region != null) {
            queueClient.setEndpoint(MessageFormat.format("https://cell-1.queue.messaging.{0}.oci.oraclecloud.com", region.getRegionId()));
        }
        return queueClient;
    }
}
