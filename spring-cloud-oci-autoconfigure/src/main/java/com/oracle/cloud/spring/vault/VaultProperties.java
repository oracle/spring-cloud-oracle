package com.oracle.cloud.spring.vault;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = VaultProperties.PREFIX)
public class VaultProperties {
    public static final String PREFIX = "spring.cloud.oci.vault";

    private String compartment;
    private String vaultId;

    public String getCompartment() {
        return compartment;
    }

    public void setCompartment(String compartment) {
        this.compartment = compartment;
    }

    public String getVaultId() {
        return vaultId;
    }

    public void setVaultId(String vaultId) {
        this.vaultId = vaultId;
    }
}
