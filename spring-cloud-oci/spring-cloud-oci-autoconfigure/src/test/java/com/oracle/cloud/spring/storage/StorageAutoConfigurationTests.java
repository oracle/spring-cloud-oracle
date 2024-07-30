/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import com.oracle.cloud.spring.core.region.StaticRegionProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class StorageAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class,
                            RefreshAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            String[] storageBeanNames = context.getBeanNamesForType(Storage.class);
                            assertTrue(storageBeanNames.length > 0);
                            Storage storage = context.getBean(Storage.class);
                            assertNotNull(storage);
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.storage.enabled=false")
                .run(
                        context -> {
                            String[] storageBeanNames = context.getBeanNamesForType(Storage.class);
                            assertEquals(storageBeanNames.length, 0);
                        });
    }

    @Configuration
    static class TestSpecificConfigurationBeans {
        @Bean
        ObjectStorageClient objectStorageClient() {
            StorageAutoConfiguration configuration = new StorageAutoConfiguration();
            ObjectStorageClient storageClient = null;
            try (MockedStatic mocked = mockStatic(ObjectStorageClient.class)) {
                ObjectStorageClient.Builder builder = mock(ObjectStorageClient.Builder.class);
                when(ObjectStorageClient.builder()).thenReturn(builder);
                ObjectStorageClient mockedStorageClient = mock(ObjectStorageClient.class);
                CredentialsProvider credentialsProvider =
                        mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                when(builder.build(credentialsProvider.getAuthenticationDetailsProvider())).thenReturn(mockedStorageClient);
                storageClient = configuration.objectStorageClient(regionProvider,
                        credentialsProvider);
                assertNotNull(storageClient);
            }
            return storageClient;
        }

    }
}
