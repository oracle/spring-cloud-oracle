// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.vault;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = VaultProperties.PREFIX)
public class VaultProperties {
    public static final String PREFIX = "spring.cloud.oci.vault";

    private String compartment;
    private String vaultId;

    private List<VaultPropertySources> propertySources;

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

    public List<VaultPropertySources> getPropertySources() {
        return propertySources;
    }

    public void setPropertySources(List<VaultPropertySources> propertySources) {
        this.propertySources = propertySources;
    }
}
