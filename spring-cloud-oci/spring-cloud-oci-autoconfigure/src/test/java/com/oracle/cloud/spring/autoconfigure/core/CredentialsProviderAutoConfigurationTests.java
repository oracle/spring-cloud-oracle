/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.*;
import com.oracle.bmc.auth.okeworkloadidentity.OkeWorkloadIdentityAuthenticationDetailsProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider.ResourcePrincipalAuthenticationDetailsProviderBuilder;
import static com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider.InstancePrincipalsAuthenticationDetailsProviderBuilder;
import static com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider.SimpleAuthenticationDetailsProviderBuilder;

class CredentialsProviderAutoConfigurationTests {

    private static final String CONFIG_FILE = "file";
    private static final String PROFILE = "profile";
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(
                    CredentialsProviderAutoConfiguration.class)).withUserConfiguration(TestConfigurationBean.class);

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            CredentialsProperties config = context.getBean(CredentialsProperties.class);
                            assertNull(config.getTenantId());
                            assertNotNull(config.getType());
                            assertEquals(config.getType(), CredentialsProperties.ConfigType.FILE);
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.config.type=SIMPLE")
                .withPropertyValues("spring.cloud.oci.config.userId=userId")
                .withPropertyValues("spring.cloud.oci.config.tenantId=tenantId")
                .withPropertyValues("spring.cloud.oci.config.fingerprint=fingerprint")
                .withPropertyValues("spring.cloud.oci.config.privateKey=privateKey")
                .withPropertyValues("spring.cloud.oci.config.profile=profile")
                .withPropertyValues("spring.cloud.oci.config.file=file")
                .withPropertyValues("spring.cloud.oci.config.passPhrase=passPhrase")
                .withPropertyValues("spring.cloud.oci.config.region=us-ashburn-1")
                .run(
                        context -> {
                            CredentialsProperties config = context.getBean(CredentialsProperties.class);
                            assertEquals(config.getType(), CredentialsProperties.ConfigType.SIMPLE);
                            assertEquals(config.getUserId(), "userId");
                            assertEquals(config.getTenantId(), "tenantId");
                            assertEquals(config.getFingerprint(), "fingerprint");
                            assertEquals(config.getPrivateKey(), "privateKey");
                            assertTrue(config.hasProfile());
                            assertEquals(config.getProfile(), PROFILE);
                            assertTrue(config.hasFile());
                            assertEquals(config.getFile(), CONFIG_FILE);
                            assertEquals(config.getPassPhrase(), "passPhrase");
                            assertEquals(config.getRegion(), "us-ashburn-1");
                        });
    }

    @Test
    void testWorkloadIdentityProvider() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.WORKLOAD_IDENTITY);
        CredentialsProviderAutoConfiguration configuration = new CredentialsProviderAutoConfiguration(properties);
        try (MockedStatic mocked = mockStatic(OkeWorkloadIdentityAuthenticationDetailsProvider.class)) {
            OkeWorkloadIdentityAuthenticationDetailsProvider.OkeWorkloadIdentityAuthenticationDetailsProviderBuilder builder =
                    mock(OkeWorkloadIdentityAuthenticationDetailsProvider.OkeWorkloadIdentityAuthenticationDetailsProviderBuilder.class);
            when(OkeWorkloadIdentityAuthenticationDetailsProvider.builder()).thenReturn(builder);
            BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
        }
    }

    @Test
    void testResourcePrincipalProvider() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.RESOURCE_PRINCIPAL);
        CredentialsProviderAutoConfiguration configuration = new CredentialsProviderAutoConfiguration(properties);
        try (MockedStatic mocked = mockStatic(ResourcePrincipalAuthenticationDetailsProvider.class)) {
            ResourcePrincipalAuthenticationDetailsProviderBuilder builder =
                    mock(ResourcePrincipalAuthenticationDetailsProviderBuilder.class);
            when(ResourcePrincipalAuthenticationDetailsProvider.builder()).thenReturn(builder);
            BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
        }
    }

    @Test
    void testInstancePrincipalProvider() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.INSTANCE_PRINCIPAL);
        CredentialsProviderAutoConfiguration configuration = new CredentialsProviderAutoConfiguration(properties);
        try (MockedStatic mocked = mockStatic(InstancePrincipalsAuthenticationDetailsProvider.class)) {
            InstancePrincipalsAuthenticationDetailsProviderBuilder builder =
                    mock(InstancePrincipalsAuthenticationDetailsProviderBuilder.class);
            when(InstancePrincipalsAuthenticationDetailsProvider.builder()).thenReturn(builder);
            BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
        }
    }

    @Test
    void testSimpleProvider() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.SIMPLE);
        properties.setRegion(Region.US_PHOENIX_1.getRegionId());
        CredentialsProviderAutoConfiguration configuration = new CredentialsProviderAutoConfiguration(properties);
        try (MockedStatic mocked = mockStatic(SimpleAuthenticationDetailsProvider.class)) {
            SimpleAuthenticationDetailsProviderBuilder builder =
                    mock(SimpleAuthenticationDetailsProviderBuilder.class);
            when(SimpleAuthenticationDetailsProvider.builder()).thenReturn(builder);
            when(builder.userId(any())).thenReturn(builder);
            when(builder.tenantId(any())).thenReturn(builder);
            when(builder.fingerprint(any())).thenReturn(builder);
            when(builder.passPhrase(any())).thenReturn(builder);
            when(builder.privateKeySupplier(any())).thenReturn(builder);
            BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
        }
    }

    @Test
    void testFileProviderWithProfileOnly() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.FILE);
        properties.setProfile(PROFILE);
        CredentialsProviderAutoConfiguration configuration = new CredentialsProviderAutoConfiguration(properties);
        try (MockedStatic mocked = mockStatic(ConfigFileAuthenticationDetailsProvider.class)) {
            try (MockedConstruction<ConfigFileAuthenticationDetailsProvider> mock =
                         mockConstruction(ConfigFileAuthenticationDetailsProvider.class)) {
                BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
                assertNotNull(provider);
            }
        }
    }

    @Test
    void testFileProviderWithProfileAndCustomConfigFile() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.FILE);
        properties.setFile(CONFIG_FILE);
        CredentialsProviderAutoConfiguration configuration = new CredentialsProviderAutoConfiguration(properties);
        try (MockedStatic mocked = mockStatic(ConfigFileAuthenticationDetailsProvider.class)) {
            try (MockedConstruction<ConfigFileAuthenticationDetailsProvider> mock =
                         mockConstruction(ConfigFileAuthenticationDetailsProvider.class)) {
                BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
                assertNotNull(provider);
            }
        }
    }

    @Test
    void testSessionTokenProviderWithProfileOnly() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.SESSION_TOKEN);
        properties.setProfile(PROFILE);
        CredentialsProviderAutoConfiguration configuration = new CredentialsProviderAutoConfiguration(properties);
        try (MockedStatic mocked = mockStatic(SessionTokenAuthenticationDetailsProvider.class)) {
            try (MockedConstruction<SessionTokenAuthenticationDetailsProvider> mock =
                         mockConstruction(SessionTokenAuthenticationDetailsProvider.class)) {
                BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
                assertNotNull(provider);
            }
        }
    }

    @Test
    void testSessionTokenProviderWithProfileAndCustomConfigFile() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.SESSION_TOKEN);
        properties.setFile(CONFIG_FILE);
        CredentialsProviderAutoConfiguration configuration = new CredentialsProviderAutoConfiguration(properties);
        try (MockedStatic mocked = mockStatic(SessionTokenAuthenticationDetailsProvider.class)) {
            try (MockedConstruction<SessionTokenAuthenticationDetailsProvider> mock =
                         mockConstruction(SessionTokenAuthenticationDetailsProvider.class)) {
                BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
                assertNotNull(provider);
            }
        }
    }

    @Configuration
    static class TestConfigurationBean {
        @Bean
        CredentialsProvider credentialsProvider() throws Exception {
            return mock(CredentialsProvider.class);
        }
    }
}
