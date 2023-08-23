/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Auto-configuration settings related to a Compartment.
 */
@ConfigurationProperties(prefix = CompartmentProperties.PREFIX)
public class CompartmentProperties {
    public static final String PREFIX = "spring.cloud.oci.compartment";

    @Nullable
    private String staticCompartment;

    @Nullable
    public String getStatic() {
        return staticCompartment;
    }

    public boolean isStatic() {
        return StringUtils.hasText(staticCompartment);
    }

    public void setStatic(String staticCompartment) {
        this.staticCompartment = staticCompartment;
    }
}
