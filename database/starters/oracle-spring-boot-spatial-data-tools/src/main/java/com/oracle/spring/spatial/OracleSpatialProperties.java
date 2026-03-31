// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = OracleSpatialProperties.PREFIX)
public class OracleSpatialProperties {
    public static final String PREFIX = "oracle.database.spatial";

    private boolean enabled = true;
    private int defaultSrid = 4326;
    private String defaultDistanceUnit = "M";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultSrid() {
        return defaultSrid;
    }

    public void setDefaultSrid(int defaultSrid) {
        this.defaultSrid = defaultSrid;
    }

    public String getDefaultDistanceUnit() {
        return defaultDistanceUnit;
    }

    public void setDefaultDistanceUnit(String defaultDistanceUnit) {
        this.defaultDistanceUnit = defaultDistanceUnit;
    }
}
