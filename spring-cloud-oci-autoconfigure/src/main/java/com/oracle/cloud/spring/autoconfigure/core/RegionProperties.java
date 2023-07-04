/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Auto-configure settings related to a Region
 */
@ConfigurationProperties(prefix = RegionProperties.PREFIX)
public class RegionProperties {
    public static final String PREFIX = "spring.cloud.oci.region";

    @Nullable
    private String staticRegion;

    @Nullable
    public String getStatic() {
        return staticRegion;
    }

    public boolean isStatic() {
        return StringUtils.hasText(staticRegion);
    }

    public void setStatic(String staticRegion) {
        this.staticRegion = staticRegion;
    }
}
