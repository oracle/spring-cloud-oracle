// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundle;
import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
import com.oracle.bmc.vault.VaultsClient;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.bmc.vault.requests.ListSecretsRequest;
import com.oracle.bmc.vault.responses.ListSecretsResponse;
import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

import static com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans.assertBeanLoaded;
import static com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans.assertBeanNotLoaded;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.Mockito.*;

public class VaultAutoConfigurationTests {
    private final String vaultIdProperty = "spring.cloud.oci.vault.vault-id=xyz";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.cloud.oci.vault.compartment=123")
            .withConfiguration(AutoConfigurations.of(VaultAutoConfiguration.class))
            .withUserConfiguration(TestCommonConfigurationBeans.class);

    private static final ApplicationContextInitializer<ConfigurableApplicationContext> PROPERTY_SOURCE_INITIALIZER =
            ctx -> new VaultEnvironmentPostProcessor()
                            .postProcessEnvironment(ctx.getEnvironment(), null);


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void beansAreLoaded(boolean enableExplicitly) {
        contextRunner.withPropertyValues(vaultIdProperty,
                        enableExplicitly ? "spring.cloud.oci.vault.enabled=true" : "anything=else")
                .run(ctx -> assertBeanLoaded(ctx, VaultTemplate.class));
    }

    @Test
    void beansAreNotLoadedWhenDisabled() {
        contextRunner.withPropertyValues(vaultIdProperty,
                        "spring.cloud.oci.vault.enabled=false")
                .run(ctx -> assertBeanNotLoaded(ctx, VaultTemplate.class));
    }

    @Test
    void beansAreNotLoadedWhenNoVault() {
        contextRunner.run(ctx -> assertBeanNotLoaded(ctx, VaultTemplate.class));
    }

    static Stream<Named<String[]>> propertySourceNotAdded() {
        return Stream.of(
            named("when disabled", new String[] {
                    "spring.cloud.oci.vault.enabled=false",
                    "spring.cloud.oci.vault.property-sources[0].vault-id=abc"
            }),
            named("when no property sources are listed", new String[] {
                    "spring.cloud.oci.vault.enabled=true"
            })
        );
    }
    @ParameterizedTest(name = "{0}")
    @MethodSource
    void propertySourceNotAdded(String[] propertyPairs) {
        contextRunner.withPropertyValues(propertyPairs)
                .withInitializer(PROPERTY_SOURCE_INITIALIZER)
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx.getEnvironment().getPropertySources())
                            .doesNotHaveAnyElementsOfTypes(VaultPropertySource.class);
                });
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void propertySourceAdded(boolean enableExplicitly) {
        try(final var ignored1 = mockStatic(CredentialsProvider.class);
            final var ignored2 = mockStatic(SecretsClient.class, Answers.RETURNS_DEEP_STUBS);
            final var ignored3 = mockStatic(VaultsClient.class, Answers.RETURNS_DEEP_STUBS);
            final var mockSecretsClient = mock(SecretsClient.class);
            final var mockVaultsClient = mock(VaultsClient.class)
        ) {
            when(SecretsClient.builder().build(any())).thenReturn(mockSecretsClient);
            when(VaultsClient.builder().build(any())).thenReturn(mockVaultsClient);

            when(mockVaultsClient.listSecrets(any()))
                .then(answer((ListSecretsRequest req) -> {
                    assertEquals("abc", req.getVaultId());
                    assertEquals("123", req.getCompartmentId());
                    return newListSecretsResponse("secA", "secB");
                }));

            when(mockSecretsClient.getSecretBundleByName(any()))
                .then(answer((GetSecretBundleByNameRequest req) -> {
                    assertEquals("abc", req.getVaultId());
                    //Return something arbitrary yet predictable for the contents:
                    return newSecretBundleResponse(req.getSecretName() + "-val");
                }));

            contextRunner.withPropertyValues("spring.cloud.oci.vault.property-sources[0].vault-id=abc",
                            enableExplicitly ? "spring.cloud.oci.vault.enabled=true" : "anything=else")
                    .withInitializer(PROPERTY_SOURCE_INITIALIZER)
                    .run(ctx -> {
                        assertThat(ctx).hasNotFailed();
                        assertThat(ctx.getEnvironment().getPropertySources())
                                .hasAtLeastOneElementOfType(VaultPropertySource.class);
                        assertEquals("secA-val", ctx.getEnvironment().getProperty("secA"));
                        assertEquals("secB-val", ctx.getEnvironment().getProperty("secB"));
                    });
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static ListSecretsResponse newListSecretsResponse(String... secretNames) {
        return ListSecretsResponse.builder()
            .items(Arrays.stream(secretNames)
                .map(secretName -> SecretSummary.builder().secretName(secretName).build())
                .collect(toList())
            )
            .build();
    }

    private static GetSecretBundleByNameResponse newSecretBundleResponse(String secretContent) {
        return GetSecretBundleByNameResponse.builder()
            .secretBundle(SecretBundle.builder()
                .secretBundleContent(Base64SecretBundleContentDetails.builder()
                    .content(Base64.getEncoder().encodeToString(secretContent.getBytes(UTF_8)))
                    .build()
                )
                .build()
            )
            .build();
    }
}
