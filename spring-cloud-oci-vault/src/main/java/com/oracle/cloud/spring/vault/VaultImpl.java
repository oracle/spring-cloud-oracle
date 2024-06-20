// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

/**
 * Default implementation for Vault interface.
 * @see Vault
 */
public class VaultImpl implements Vault {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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

    /**
     * Retrieves a secret by name.
     * @param secretName The name of the secret.
     * @return The secret bundle response.
     */
    @Override
    public GetSecretBundleByNameResponse getSecret(String secretName) {
        Assert.hasText(secretName, "secretName must not be empty");
        GetSecretBundleByNameRequest request = GetSecretBundleByNameRequest.builder()
                .vaultId(vaultId)
                .secretName(secretName)
                .build();
        return secrets.getSecretBundleByName(request);
    }

    /**
     * Create a secret.
     * @param secretName The name of the secret being created.
     * @param body The secret body to create.
     * @return A create secret response.
     */
    @Override
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

    /**
     * Schedule the deletion of a secret.
     * @param secretName The name of the secret to schedule deletion for.
     * @param deleteAfterDays The number of days after which the secret will be deleted. May be between 1 and 30.
     * @return A delete secret respones.
     */
    @Override
    public ScheduleSecretDeletionResponse scheduleSecretDeletion(String secretName, int deleteAfterDays) {
        Assert.hasText(secretName, "secretName must not be empty");
        Assert.isTrue(deleteAfterDays >= 1 && deleteAfterDays <= 30, "deleteAfterDays must be between 1 and 30");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, deleteAfterDays);
        String secretId = getSecret(secretName)
                .getSecretBundle()
                .getSecretId();
        ScheduleSecretDeletionDetails body = ScheduleSecretDeletionDetails.builder()
                .timeOfDeletion(cal.getTime())
                .build();
        ScheduleSecretDeletionRequest request = ScheduleSecretDeletionRequest.builder()
                .secretId(secretId)
                .body$(body)
                .build();
        return vaults.scheduleSecretDeletion(request);
    }

    /**
     * Update a secret content.
     * @param secretName The name of the secret to update.
     * @param body The secret body to update.
     * @return An update secret response.
     */
    @Override
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

    /**
     * Lists all secrets in the Vault.
     * @return A list of secret summaries.
     */
    @Override
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
