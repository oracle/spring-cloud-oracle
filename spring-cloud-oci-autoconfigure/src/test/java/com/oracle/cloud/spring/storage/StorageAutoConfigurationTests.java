/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import static org.mockito.Mockito.mock;

public class StorageAutoConfigurationTests {
    private ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        this.contextRunner
                .run(
                        context -> {
                            String[] storageBeanNames = context.getBeanNamesForType(Storage.class);
                            Assert.isTrue(storageBeanNames.length > 0);
                            Storage storage = context.getBean(Storage.class);
                            Assert.isTrue(storage != null);
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        this.contextRunner
                .withPropertyValues("spring.cloud.oci.storage.enabled=false")
                .run(
                        context -> {
                            String[] storageBeanNames = context.getBeanNamesForType(Storage.class);
                            Assert.isTrue(storageBeanNames.length == 0);
                        });
    }

    @Configuration
    static class TestSpecificConfigurationBeans {
        @Bean
        public ObjectStorageClient objectStorageClient() {
            return mock(ObjectStorageClient.class);
        }

    }
}
