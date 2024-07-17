// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
import com.oracle.bmc.vault.model.CreateSecretDetails;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.bmc.vault.model.UpdateSecretDetails;
import com.oracle.bmc.vault.responses.CreateSecretResponse;
import com.oracle.bmc.vault.responses.ScheduleSecretDeletionResponse;
import com.oracle.bmc.vault.responses.UpdateSecretResponse;

/**
 * The VaultTemplate interface defines the API for accessing OCI Vault Service.
 * Users can retrieve, create, update, list, and delete secrets within an OCI Vault.
 */
public interface VaultTemplate {
    /**
     * Retrieves a secret by name.
     * @param secretName The name of the secret.
     * @return The secret bundle response.
     */
    GetSecretBundleByNameResponse getSecret(String secretName);

    /**
     * Retrieve all secrets from the Vault.
     *
     * @return A mapping of secret names to secret values.
     */
    Map<String, String> getAllSecrets();

    /**
     * Lists all secrets in the Vault.
     * @return A list of secret summaries.
     */
    List<SecretSummary> listSecrets();

    /**
     * Create a secret.
     * @param secretName The name of the secret being created.
     * @param body The secret body to create.
     * @return A create secret response.
     */
    CreateSecretResponse createSecret(String secretName, CreateSecretDetails body);

    /**
     * Schedule the deletion of a secret.
     * @param secretName The name of the secret to schedule deletion for.
     * @param deleteAfterDays The number of days after which the secret will be deleted. May be between 1 and 30.
     * @return A delete secret respones.
     */
    ScheduleSecretDeletionResponse scheduleSecretDeletion(String secretName, int deleteAfterDays);

    /**
     * Update a secret content.
     * @param secretName The name of the secret to update.
     * @param body The secret body to update.
     * @return An update secret response.
     */
    UpdateSecretResponse updateSecret(String secretName, UpdateSecretDetails body);

    /**
     * Decode a secret bundle response as a String.
     * @param bundle The bundle to decode.
     * @return The secret String content.
     */
    default String decodeBundle(GetSecretBundleByNameResponse bundle) {
        SecretBundleContentDetails content = bundle.getSecretBundle().getSecretBundleContent();
        if (content instanceof Base64SecretBundleContentDetails) {
            Base64SecretBundleContentDetails encoded = (Base64SecretBundleContentDetails) content;
            return new String(Base64.getDecoder().decode(encoded.getContent()), StandardCharsets.UTF_8);
        } else {
            return content.toString();
        }
    }
}
