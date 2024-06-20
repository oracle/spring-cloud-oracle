/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.oracle.cloud.spring.vault;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundle;
import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
import com.oracle.bmc.vault.Vaults;
import com.oracle.bmc.vault.model.CreateSecretDetails;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.bmc.vault.model.UpdateSecretDetails;
import com.oracle.bmc.vault.responses.CreateSecretResponse;
import com.oracle.bmc.vault.responses.ListSecretsResponse;
import com.oracle.bmc.vault.responses.ScheduleSecretDeletionResponse;
import com.oracle.bmc.vault.responses.UpdateSecretResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VaultImplTest {
    private Vaults vaults;
    private Secrets secrets;
    private Vault vault;

    private final String compartmentId = "mycompartment";
    private final String vaultId = "myvault";
    private final String secretName = "mysecret";
    private final String secretValue = "foo";
    private final String secretValueEncoded = Base64.getEncoder().encodeToString(secretValue.getBytes(StandardCharsets.UTF_8));


    @BeforeEach
    void setUp() {
        vaults = mock(Vaults.class);
        secrets = mock(Secrets.class);
        vault = new VaultImpl(vaults, secrets, vaultId, compartmentId);
        GetSecretBundleByNameResponse response = GetSecretBundleByNameResponse.builder()
                .secretBundle(SecretBundle.builder()
                        .secretId(secretName)
                        .secretBundleContent(Base64SecretBundleContentDetails.builder()
                                .content(secretValueEncoded)
                                .build())
                        .build())
                .build();
        when(secrets.getSecretBundleByName(any())).thenReturn(response);
    }

    @Test
    void getSecretBundle() {
        GetSecretBundleByNameResponse foo = vault.getSecret(secretName);
        String decoded = vault.decodeBundle(foo);
        assertThat(decoded).isEqualTo(secretValue);
    }

    @Test
    void createSecret() {
        when(vaults.createSecret(any())).thenReturn(CreateSecretResponse.builder().build());
        CreateSecretResponse response = vault.createSecret(secretName, CreateSecretDetails.builder().build());
        assertThat(response).isNotNull();
    }

    @Test
    void scheduleSecretDeletion() {
        when(vaults.scheduleSecretDeletion(any())).thenReturn(ScheduleSecretDeletionResponse.builder().build());
        ScheduleSecretDeletionResponse response = vault.scheduleSecretDeletion(secretName, new Date());
        assertThat(response).isNotNull();
    }

    @Test
    void updateSecret() {
        when(vaults.updateSecret(any())).thenReturn(UpdateSecretResponse.builder().build());
        UpdateSecretResponse response = vault.updateSecret(secretName, UpdateSecretDetails.builder().build());
        assertThat(response).isNotNull();
    }

    @Test
    void listSecrets() {
        List<SecretSummary> summaries = List.of(SecretSummary.builder().build());
        ListSecretsResponse r1 = ListSecretsResponse.builder()
                .items(summaries)
                .opcNextPage("next")
                .build();
        ListSecretsResponse r2 = ListSecretsResponse.builder()
                .items(summaries)
                .build();
        when(vaults.listSecrets(any())).thenReturn(r1).thenReturn(r2);
        List<SecretSummary> actual = vault.listSecrets();
        assertThat(actual).hasSize(2);
    }
}
