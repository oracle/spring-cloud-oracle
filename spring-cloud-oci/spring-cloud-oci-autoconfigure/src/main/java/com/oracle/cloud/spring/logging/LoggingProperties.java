/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

/**
 * Auto-configuration settings related to Logging component.
 */
@ConfigurationProperties(prefix = LoggingProperties.PREFIX)
public class LoggingProperties {
    public static final String PREFIX = "spring.cloud.oci.logging";


    @Nullable
    private String logId;

    @Nullable
    public String getLogId() {
        return logId;
    }

    public void setLogId(@Nullable String logId) {
        this.logId = logId;
    }

}
