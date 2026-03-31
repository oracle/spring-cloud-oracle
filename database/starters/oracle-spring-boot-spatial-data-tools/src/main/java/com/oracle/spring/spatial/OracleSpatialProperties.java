// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * Configuration properties for Oracle Spatial helper beans.
 */
@ConfigurationProperties(prefix = OracleSpatialProperties.PREFIX)
public class OracleSpatialProperties {
    /**
     * Property prefix for Oracle Spatial starter configuration.
     */
    public static final String PREFIX = "oracle.database.spatial";

    /**
     * Enables or disables Oracle Spatial auto-configuration.
     */
    private boolean enabled = true;

    /**
     * Default SRID used when converting GeoJSON values into {@code SDO_GEOMETRY}.
     * The value must be a positive Oracle Spatial SRID such as {@code 4326}.
     */
    private int defaultSrid = 4326;

    /**
     * Default distance unit appended to generated distance clauses.
     * This value is intentionally validated loosely so Oracle-supported formats
     * such as {@code M}, {@code KM}, or {@code UNIT=MILE} remain usable.
     */
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
        Assert.isTrue(defaultSrid > 0, "oracle.database.spatial.default-srid must be greater than 0");
        this.defaultSrid = defaultSrid;
    }

    public String getDefaultDistanceUnit() {
        return defaultDistanceUnit;
    }

    public void setDefaultDistanceUnit(String defaultDistanceUnit) {
        Assert.hasText(defaultDistanceUnit, "oracle.database.spatial.default-distance-unit must not be blank");
        String trimmed = defaultDistanceUnit.trim();
        Assert.isTrue(!trimmed.contains("'"),
                "oracle.database.spatial.default-distance-unit must not contain single quotes");
        this.defaultDistanceUnit = trimmed;
    }
}
