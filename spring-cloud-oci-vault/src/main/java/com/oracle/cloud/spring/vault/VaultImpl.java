/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.oracle.cloud.spring.vault;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
import com.oracle.bmc.vault.Vaults;
import com.oracle.bmc.vault.model.CreateSecretDetails;
import com.oracle.bmc.vault.model.ScheduleSecretDeletionDetails;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.bmc.vault.model.UpdateSecretDetails;
import com.oracle.bmc.vault.requests.CreateSecretRequest;
import com.oracle.bmc.vault.requests.ListSecretsRequest;
import com.oracle.bmc.vault.requests.ScheduleSecretDeletionRequest;
import com.oracle.bmc.vault.requests.UpdateSecretRequest;
import com.oracle.bmc.vault.responses.CreateSecretResponse;
import com.oracle.bmc.vault.responses.ListSecretsResponse;
import com.oracle.bmc.vault.responses.ScheduleSecretDeletionResponse;
import com.oracle.bmc.vault.responses.UpdateSecretResponse;
import org.springframework.util.Assert;

public class VaultImpl implements Vault {
    private final Vaults vaults;
    private final Secrets secrets;
    private final String vaultId;
    private final String compartmentId;

    public VaultImpl(Vaults vaults, Secrets secrets, String vaultId, String compartmentId) {
        Assert.notNull(vaults, "vaults must not be null");
        Assert.notNull(secrets, "secrets must not be null");
        Assert.hasText(vaultId, "vaultId must not be empty");
        Assert.hasText(compartmentId, "compartmentId must not be empty");
        this.vaults = vaults;
        this.secrets = secrets;
        this.vaultId = vaultId;
        this.compartmentId = compartmentId;
    }

    public GetSecretBundleByNameResponse getSecret(String secretName) {
        Assert.hasText(secretName, "secretName must not be empty");
        GetSecretBundleByNameRequest request = GetSecretBundleByNameRequest.builder()
                .vaultId(vaultId)
                .secretName(secretName)
                .build();
        return secrets.getSecretBundleByName(request);
    }

    public CreateSecretResponse createSecret(String secretName, CreateSecretDetails body) {
        Assert.hasText(secretName, "secretName must not be empty");
        Assert.notNull(body, "body must not be null");
        CreateSecretRequest request = CreateSecretRequest.builder()
                .body$(body.toBuilder()
                        .vaultId(vaultId)
                        .compartmentId(compartmentId)
                        .secretName(secretName)
                        .build())
                .build();
        return vaults.createSecret(request);
    }

    public ScheduleSecretDeletionResponse scheduleSecretDeletion(String secretName, Date timeOfDeletion) {
        Assert.hasText(secretName, "secretName must not be empty");
        Assert.notNull(timeOfDeletion, "timeOfDeletion must not be null");
        String secretId = getSecret(secretName)
                .getSecretBundle()
                .getSecretId();
        ScheduleSecretDeletionDetails body = ScheduleSecretDeletionDetails.builder()
                .timeOfDeletion(timeOfDeletion)
                .build();
        ScheduleSecretDeletionRequest request = ScheduleSecretDeletionRequest.builder()
                .secretId(secretId)
                .body$(body)
                .build();
        return vaults.scheduleSecretDeletion(request);
    }

    public UpdateSecretResponse updateSecret(String secretName, UpdateSecretDetails body) {
        Assert.hasText(secretName, "secretName must not be empty");
        Assert.notNull(body, "body must not be null");
        String secretId = getSecret(secretName)
                .getSecretBundle()
                .getSecretId();
        UpdateSecretRequest request = UpdateSecretRequest.builder()
                .secretId(secretId)
                .body$(body)
                .build();
        return vaults.updateSecret(request);
    }

    public List<SecretSummary> listSecrets() {
        List<SecretSummary> summaries = new ArrayList<>();
        String page = null;
        do {
            ListSecretsRequest request = ListSecretsRequest.builder()
                    .vaultId(vaultId)
                    .compartmentId(compartmentId)
                    .page(page)
                    .build();
            ListSecretsResponse listSecretsResponse = vaults.listSecrets(request);
            summaries.addAll(listSecretsResponse.getItems());
            page = listSecretsResponse.getOpcNextPage();
        } while(page != null);
        return summaries;
    }
}
