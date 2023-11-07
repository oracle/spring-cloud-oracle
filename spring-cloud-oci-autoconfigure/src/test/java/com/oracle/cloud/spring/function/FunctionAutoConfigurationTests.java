/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.function;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.functions.FunctionsInvoke;
import com.oracle.bmc.functions.FunctionsInvokeClient;
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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class FunctionAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(FunctionAutoConfiguration.class,
                    RefreshAutoConfiguration.class))
                    .withUserConfiguration(TestCommonConfigurationBeans.class)
                    .withUserConfiguration(TestSpecificConfigurationBeans.class);

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            String[] functionBeanNames = context.getBeanNamesForType(Function.class);
                            assertTrue(functionBeanNames.length > 0);
                            Function function = context.getBean(Function.class);
                            assertNotNull(function);
                        });
    }

    @Test
    void testConfigurationConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.function.enabled=false")
                .run(
                        context -> {
                            String[] functionBeanNames = context.getBeanNamesForType(Function.class);
                            assertEquals(functionBeanNames.length, 0);
                        });
    }


    @Configuration
    static class TestSpecificConfigurationBeans {
        @Bean
        FunctionsInvoke function() {
            FunctionAutoConfiguration fnAutoConfiguration = new FunctionAutoConfiguration();
            FunctionsInvoke fnInvoke = null;
            try (MockedStatic mocked = mockStatic(FunctionsInvokeClient.class)) {
                FunctionsInvokeClient.Builder builder = mock(FunctionsInvokeClient.Builder.class);
                when(FunctionsInvokeClient.builder()).thenReturn(builder);
                FunctionsInvokeClient mockedFnClient = mock(FunctionsInvokeClient.class);
                CredentialsProvider credentialsProvider =
                        mock(CredentialsProvider.class);
                RegionProvider regionProvider = new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
                when(builder.build(credentialsProvider.getAuthenticationDetailsProvider())).thenReturn(mockedFnClient);
                fnInvoke = fnAutoConfiguration.functionsInvokeClient(regionProvider, credentialsProvider);
                assertNotNull(fnInvoke);
            }
            return fnInvoke;
        }
    }
}
