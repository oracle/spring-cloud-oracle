// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.stream.Stream;

import static com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans.assertBeanLoaded;
import static com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans.assertBeanNotLoaded;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

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
}
