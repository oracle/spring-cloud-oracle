/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.oracle.cloud.spring.vault;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
import com.oracle.bmc.vault.model.CreateSecretDetails;
import com.oracle.bmc.vault.model.UpdateSecretDetails;
import com.oracle.bmc.vault.responses.CreateSecretResponse;
import com.oracle.bmc.vault.responses.ScheduleSecretDeletionResponse;
import com.oracle.bmc.vault.responses.UpdateSecretResponse;

public interface Vault {
    GetSecretBundleByNameResponse getSecret(String secretName);
    CreateSecretResponse createSecret(String secretName, CreateSecretDetails body);
    ScheduleSecretDeletionResponse scheduleSecretDeletion(String secretName, Date timeOfDeletion);
    UpdateSecretResponse updateSecret(String secretName, UpdateSecretDetails body);

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
