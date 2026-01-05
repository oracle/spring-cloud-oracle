/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.*;
import com.oracle.bmc.auth.okeworkloadidentity.OkeWorkloadIdentityAuthenticationDetailsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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
    void testAutoConfigBacksOffWithUserSuppliedBean() {
        contextRunner.run(ctx ->
            assertThat(ctx.getBeanNamesForType(BasicAuthenticationDetailsProvider.class))
                    .containsExactly("userSuppliedBasicAuthenticationDetailsProvider")
        );
    }

    @Test
    void testConfigurationValueDefaultsAreAsExpected() {
        contextRunner
                .run(
                        context -> {
                            CredentialsProperties config = context.getBean(CredentialsProperties.class);
                            assertNull(config.getTenantId());
                            assertEquals(CredentialsProperties.ConfigType.FILE, config.getType());
                            assertFalse(config.hasProfile());
                            assertFalse(config.hasFile());
                        });
    }

    @Test
    void testConfigurationValueConfiguredAreAsExpected() {
        contextRunner
                .withPropertyValues("spring.cloud.oci.config.type=SIMPLE")
                .withPropertyValues("spring.cloud.oci.config.federation-endpoint=federationEndpoint")
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
                            assertEquals(CredentialsProperties.ConfigType.SIMPLE, config.getType());
                            assertEquals("federationEndpoint", config.getFederationEndpoint());
                            assertEquals("userId", config.getUserId());
                            assertEquals("tenantId", config.getTenantId());
                            assertEquals("fingerprint", config.getFingerprint());
                            assertEquals("privateKey", config.getPrivateKey());
                            assertTrue(config.hasProfile());
                            assertEquals(PROFILE, config.getProfile());
                            assertTrue(config.hasFile());
                            assertEquals(CONFIG_FILE, config.getFile());
                            assertEquals("passPhrase", config.getPassPhrase());
                            assertEquals("us-ashburn-1", config.getRegion());
                        });
    }

    @Test
    void testWorkloadIdentityProvider() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.WORKLOAD_IDENTITY);
        properties.setFederationEndpoint("https://ds9");

        try (final var ignored = mockStatic(OkeWorkloadIdentityAuthenticationDetailsProvider.class)) {
            OkeWorkloadIdentityAuthenticationDetailsProvider.OkeWorkloadIdentityAuthenticationDetailsProviderBuilder builder =
                    mock(OkeWorkloadIdentityAuthenticationDetailsProvider.OkeWorkloadIdentityAuthenticationDetailsProviderBuilder.class, RETURNS_SELF);
            when(OkeWorkloadIdentityAuthenticationDetailsProvider.builder()).thenReturn(builder);

            final var mockProvider = mock(OkeWorkloadIdentityAuthenticationDetailsProvider.class);
            when(builder.build()).then(__ -> {
                verify(builder).federationEndpoint("https://ds9");
                return mockProvider;
            });

            assertSame(mockProvider, properties.createBasicAuthenticationDetailsProvider());
        }
    }

    @Test
    void testResourcePrincipalProvider() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.RESOURCE_PRINCIPAL);
        properties.setFederationEndpoint("https://ds9");

        try (final var ignored = mockStatic(ResourcePrincipalAuthenticationDetailsProvider.class)) {
            ResourcePrincipalAuthenticationDetailsProviderBuilder builder =
                    mock(ResourcePrincipalAuthenticationDetailsProviderBuilder.class, RETURNS_SELF);
            when(ResourcePrincipalAuthenticationDetailsProvider.builder()).thenReturn(builder);

            final var mockProvider = mock(ResourcePrincipalAuthenticationDetailsProvider.class);
            when(builder.build()).then(__ -> {
                verify(builder).federationEndpoint("https://ds9");
                return mockProvider;
            });

            assertSame(mockProvider, properties.createBasicAuthenticationDetailsProvider());
        }
    }

    @Test
    void testInstancePrincipalProvider() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.INSTANCE_PRINCIPAL);
        properties.setFederationEndpoint("https://ds9");

        try (final var ignored = mockStatic(InstancePrincipalsAuthenticationDetailsProvider.class)) {
            InstancePrincipalsAuthenticationDetailsProviderBuilder builder =
                    mock(InstancePrincipalsAuthenticationDetailsProviderBuilder.class, RETURNS_SELF);
            when(InstancePrincipalsAuthenticationDetailsProvider.builder()).thenReturn(builder);

            final var mockProvider = mock(InstancePrincipalsAuthenticationDetailsProvider.class);
            when(builder.build()).then(__ -> {
                verify(builder).federationEndpoint("https://ds9");
                return mockProvider;
            });

            assertSame(mockProvider, properties.createBasicAuthenticationDetailsProvider());
        }
    }

    @Test
    void testSimpleProvider() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.SIMPLE);
        properties.setRegion(Region.US_PHOENIX_1.getRegionId());

        try (final var ignored = mockStatic(SimpleAuthenticationDetailsProvider.class)) {
            SimpleAuthenticationDetailsProviderBuilder builder =
                    mock(SimpleAuthenticationDetailsProviderBuilder.class);
            when(SimpleAuthenticationDetailsProvider.builder()).thenReturn(builder);
            when(builder.userId(any())).thenReturn(builder);
            when(builder.tenantId(any())).thenReturn(builder);
            when(builder.fingerprint(any())).thenReturn(builder);
            when(builder.passPhrase(any())).thenReturn(builder);
            when(builder.privateKeySupplier(any())).thenReturn(builder);
            final var mockProvider = mock(SimpleAuthenticationDetailsProvider.class);
            when(builder.build()).thenReturn(mockProvider);

            assertSame(mockProvider, properties.createBasicAuthenticationDetailsProvider());
        }
    }

    @Test
    void testFileProviderWithProfileOnly() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.FILE);
        properties.setProfile(PROFILE);

        try (final var ignored = mockStatic(ConfigFileAuthenticationDetailsProvider.class);
             final var mock = mockConstruction(ConfigFileAuthenticationDetailsProvider.class,
                     (__, ctx) -> assertEquals(List.of(PROFILE), ctx.arguments()))
        ) {
            BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
            assertNotNull(provider);
            assertSame(mock.constructed().getFirst(), provider);
        }
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testFileProviderWithCustomConfigFile(boolean andProfile) throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.FILE);
        properties.setFile(CONFIG_FILE);

        final String expectedProfile;
        if(andProfile) {
            properties.setProfile(PROFILE);
            expectedProfile = PROFILE;
        }
        else {
            expectedProfile = "DEFAULT";
        }

        try (final var ignored = mockStatic(ConfigFileAuthenticationDetailsProvider.class);
             final var mock = mockConstruction(ConfigFileAuthenticationDetailsProvider.class,
                     (__, ctx) ->
                             assertEquals(List.of(CONFIG_FILE, expectedProfile), ctx.arguments()))
        ) {
            BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
            assertNotNull(provider);
            assertSame(mock.constructed().getFirst(), provider);
        }
    }

    @Test
    void testSessionTokenProviderWithProfileOnly() throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.SESSION_TOKEN);
        properties.setProfile(PROFILE);

        try (final var ignored = mockStatic(SessionTokenAuthenticationDetailsProvider.class);
             final var mock = mockConstruction(SessionTokenAuthenticationDetailsProvider.class,
                     (__, ctx) -> assertEquals(List.of(PROFILE), ctx.arguments()))
        ) {
            BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
            assertNotNull(provider);
            assertSame(mock.constructed().getFirst(), provider);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSessionTokenProviderWithProfileAndCustomConfigFile(boolean andProfile) throws Exception {
        CredentialsProperties properties = new CredentialsProperties();
        properties.setType(CredentialsProperties.ConfigType.SESSION_TOKEN);
        properties.setFile(CONFIG_FILE);

        final String expectedProfile;
        if(andProfile) {
            properties.setProfile(PROFILE);
            expectedProfile = PROFILE;
        }
        else {
            expectedProfile = "DEFAULT";
        }

        try (final var ignored = mockStatic(SessionTokenAuthenticationDetailsProvider.class);
             final var mock = mockConstruction(SessionTokenAuthenticationDetailsProvider.class,
                     (__, ctx) ->
                             assertEquals(List.of(CONFIG_FILE, expectedProfile), ctx.arguments()))
        ) {
            BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();
            assertNotNull(provider);
            assertSame(mock.constructed().getFirst(), provider);
        }
    }

    @Configuration
    static class TestConfigurationBean {
        @Bean
        BasicAuthenticationDetailsProvider userSuppliedBasicAuthenticationDetailsProvider() {
            return mock(BasicAuthenticationDetailsProvider.class);
        }
    }
}
