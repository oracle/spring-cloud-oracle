// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.sample.vault.springcloudocivaultsample;

import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
import com.oracle.cloud.spring.vault.Vault;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demoapp/api/vault/")
@Tag(name = "streaming APIs")
public class VaultController {
    private final Vault vault;

    public VaultController(Vault vault) {
        this.vault = vault;
    }

    @GetMapping("secret")
    public ResponseEntity<?> getSecret(@RequestParam String secretName) {
        GetSecretBundleByNameResponse secret = vault.getSecret(secretName);
        return ResponseEntity.ok(vault.decodeBundle(secret));
    }
}
