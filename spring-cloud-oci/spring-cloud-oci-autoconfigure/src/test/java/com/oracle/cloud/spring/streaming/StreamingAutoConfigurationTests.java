/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.streaming;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.streaming.*;
import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import com.oracle.cloud.spring.core.region.StaticRegionProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class StreamingAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(StreamingAutoConfiguration.class,
                            RefreshAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(StreamingAutoConfigurationTests.TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            String[] streamingBeanNames = context.getBeanNamesForType(Streaming.class);
                            assertTrue(streamingBeanNames.length > 0);
                            Streaming streaming = context.getBean(Streaming.class);
                            assertNotNull(streaming);
                        });
    }

    @Test
    void testConfigurationConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.streaming.enabled=false")
                .run(
                        context -> {
                            String[] streamingBeanNames = context.getBeanNamesForType(Streaming.class);
                            assertEquals(streamingBeanNames.length, 0);
                        });
    }

    @Configuration
    static class TestSpecificConfigurationBeans {
        @Bean
        Stream stream() {
            StreamingAutoConfiguration configuration = new StreamingAutoConfiguration();
            Stream stream = null;
            try (MockedConstruction<StreamClient> mock =
                         mockConstruction(StreamClient.class)) {
                CredentialsProvider credentialsProvider =
                        Mockito.mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                stream = configuration.streamingClient(regionProvider,
                        credentialsProvider);
                assertNotNull(stream);
            }
            return stream;
        }

        @Bean
        StreamAdmin streamAdmin() {
            StreamingAutoConfiguration configuration = new StreamingAutoConfiguration();
            StreamAdmin streamAdmin = null;
            try (MockedStatic mocked = mockStatic(StreamAdminClient.class)) {
                StreamAdminClient.Builder builder = mock(StreamAdminClient.Builder.class);
                when(StreamAdminClient.builder()).thenReturn(builder);
                StreamAdminClient mockedStreamAdminClient = mock(StreamAdminClient.class);
                CredentialsProvider credentialsProvider =
                        mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                when(builder.build(credentialsProvider.getAuthenticationDetailsProvider())).thenReturn(mockedStreamAdminClient);
                streamAdmin = configuration.streamingAdminClient(regionProvider, credentialsProvider);
                assertNotNull(streamAdmin);
            }
            return streamAdmin;
        }
    }

}
