/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging;

import com.oracle.bmc.loggingingestion.Logging;
import com.oracle.bmc.loggingingestion.responses.PutLogsResponse;

/**
 * Interface for defining OCI logging module
 */
public interface LoggingSvc {

    /**
     * Direct instance of OCI Java SDK Logging Client.
     * @return Logging
     */
    Logging getClient();

    /**
     * Ingest logs associated with a Log OCID
     * @param logText Content of the log to be ingested
     * @return
     */
    PutLogsResponse putLogs(String logText);
}
