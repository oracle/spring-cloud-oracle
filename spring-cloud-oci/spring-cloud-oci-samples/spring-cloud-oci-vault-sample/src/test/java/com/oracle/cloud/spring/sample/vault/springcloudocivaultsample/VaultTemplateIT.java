// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.sample.vault.springcloudocivaultsample;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
import com.oracle.bmc.vault.model.Base64SecretContentDetails;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.bmc.vault.model.UpdateSecretDetails;
import com.oracle.bmc.vault.responses.UpdateSecretResponse;
import com.oracle.cloud.spring.vault.VaultTemplate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Requires an existing vault, identified by the OCI_VAULT_ID environment variable.
 * The vault must have at least one secret named "mysecret" present.
 * This secret must be in the "ACTIVE" lifecycle state.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OCI_COMPARTMENT_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OCI_VAULT_ID", matches = ".+")
@Disabled
public class VaultTemplateIT {
    @Autowired
    VaultTemplate vaultTemplate;

    @Autowired
    VaultController vaultController;

    @Value("${secretName:mysecret}")
    private String secretName;

    @Value("${numSecrets:1}")
    private int numSecrets;

    @Test
    void getSecret() {
        GetSecretBundleByNameResponse secret = vaultTemplate.getSecret(secretName);
        String decoded = vaultTemplate.decodeBundle(secret);
        assertThat(decoded).isNotNull();
        assertThat(decoded).hasSizeGreaterThan(1);
    }

    @Test
    void updateSecret() {
        String content = UUID.randomUUID().toString();
        Base64SecretContentDetails contentDetails = Base64SecretContentDetails.builder()
                .content(Base64.getEncoder().encodeToString(content.getBytes()))
                .name(content)
                .build();
        UpdateSecretResponse response = vaultTemplate.updateSecret(secretName, UpdateSecretDetails.builder()
                .secretContent(contentDetails)
                .build());
        assertThat(response.getSecret()).isNotNull();
    }

    @Test
    void listSecret() {
        List<SecretSummary> summaries = vaultTemplate.listSecrets();
        assertThat(summaries).hasSizeGreaterThanOrEqualTo(numSecrets);
    }

    @Test
    void propertySourceLoaded() {
        assertThat(vaultController.getVaultSecretValue()).hasSizeGreaterThan(1);
    }
}
