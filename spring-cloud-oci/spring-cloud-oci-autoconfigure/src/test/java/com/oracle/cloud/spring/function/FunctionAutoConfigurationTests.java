/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.function;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.functions.FunctionsManagement;
import com.oracle.bmc.functions.FunctionsInvoke;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import com.oracle.cloud.spring.core.region.StaticRegionProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration.credentialsProviderQualifier;
import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.regionProviderQualifier;

public class FunctionAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(FunctionAutoConfiguration.class,
                    RefreshAutoConfiguration.class))
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
        @Bean(name = credentialsProviderQualifier)
        CredentialsProvider credentialsProvider() throws Exception {
            return new CredentialsProvider(new BasicAuthenticationDetailsProvider() {
                @Override
                public String getKeyId() {
                    return "ocid1.tenancy.oc1..test/ocid1.user.oc1..test/fingerprint";
                }

                @Override
                public java.io.InputStream getPrivateKey() {
                    return new java.io.ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
                }

                @Override
                public String getPassPhrase() {
                    return null;
                }

                @Override
                public char[] getPassphraseCharacters() {
                    return null;
                }
            });
        }

        @Bean(name = regionProviderQualifier)
        RegionProvider regionProvider() {
            return new StaticRegionProvider("us-phoenix-1");
        }

        @Bean
        FunctionsInvoke function() {
            return (FunctionsInvoke) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{FunctionsInvoke.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "setEndpoint", "setRegion", "refreshClient", "useRealmSpecificEndpointTemplate", "close" -> null;
                        case "getEndpoint", "getWaiters", "getPaginators" -> null;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }

        @Bean
        FunctionsManagement functionsManagement() {
            return (FunctionsManagement) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{FunctionsManagement.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "setEndpoint", "setRegion", "refreshClient", "useRealmSpecificEndpointTemplate", "close" -> null;
                        case "getEndpoint", "getWaiters", "getPaginators" -> null;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
    }
}
