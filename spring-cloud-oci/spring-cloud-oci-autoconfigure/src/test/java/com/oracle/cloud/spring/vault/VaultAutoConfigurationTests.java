// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans.assertBeanLoaded;
import static com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans.assertBeanNotLoaded;

public class VaultAutoConfigurationTests {
    private final String vaultIdProperty = "spring.cloud.oci.vault.vault-id=xyz";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.cloud.oci.vault.enabled=true",
                    "spring.cloud.oci.vault.compartment=xyz")
            .withConfiguration(AutoConfigurations.of(VaultAutoConfiguration.class))
            .withUserConfiguration(TestCommonConfigurationBeans.class);


    @Test
    void beansAreLoaded() {
        contextRunner.withPropertyValues(vaultIdProperty)
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
}
