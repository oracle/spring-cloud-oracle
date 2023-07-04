/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.notification;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.NotificationControlPlaneClient;
import com.oracle.bmc.ons.NotificationDataPlane;
import com.oracle.bmc.ons.NotificationDataPlaneClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import java.io.IOException;

/**
 * Auto-configuration for initializing the OCI Notification component.
 * Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 * {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 * for loading the Authentication configuration
 *
 * @see com.oracle.cloud.spring.notification.Notification
 */
@AutoConfiguration
@ConditionalOnClass({Notification.class})
@ConditionalOnProperty(name = "spring.cloud.oci.notification.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Notification.class)
    Notification getNotificationImpl(NotificationDataPlane notificationDataPlane, NotificationControlPlane notificationControlPlane ) {
        return new NotificationImpl(notificationDataPlane, notificationControlPlane);
    }

    @Bean
    @ConditionalOnMissingBean
    NotificationDataPlane notificationDataPlaneClient(BasicAuthenticationDetailsProvider adp,
                                                      RegionProvider regionProvider) throws IOException {
        NotificationDataPlane notificationDataPlaneClient = new NotificationDataPlaneClient(adp);
        if (regionProvider.getRegion() != null) notificationDataPlaneClient.setRegion(regionProvider.getRegion());
        return notificationDataPlaneClient;
    }

    @Bean
    @ConditionalOnMissingBean
    NotificationControlPlane notificationControlPlaneClient(BasicAuthenticationDetailsProvider adp,
                                                            RegionProvider regionProvider) throws IOException {
        NotificationControlPlane notificationControlPlaneClient = new NotificationControlPlaneClient(adp);
        if (regionProvider.getRegion() != null) notificationControlPlaneClient.setRegion(regionProvider.getRegion());
        return notificationControlPlaneClient;
    }
}
