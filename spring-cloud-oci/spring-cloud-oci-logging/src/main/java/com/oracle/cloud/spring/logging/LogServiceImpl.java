/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging;

import com.oracle.bmc.loggingingestion.Logging;
import com.oracle.bmc.loggingingestion.model.LogEntry;
import com.oracle.bmc.loggingingestion.model.LogEntryBatch;
import com.oracle.bmc.loggingingestion.model.PutLogsDetails;
import com.oracle.bmc.loggingingestion.requests.PutLogsRequest;
import com.oracle.bmc.loggingingestion.responses.PutLogsResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * Implementation of the OCI Logging module.
 */
@SuppressFBWarnings(value = "EI",
        justification = "The OCI logging client is intentionally retained by this service and exposed through the public client accessor.")
public class LogServiceImpl implements LogService {

    private static final String LOG_SPEC_VERSION = "1.0";
    private static final String LOG_SOURCE = "Spring application";
    private static final String LOG_TYPE = "custom.application";
    private static final String SUBJECT = "custom.logging";

    private final Logging logging;

    private final String logId;
    public LogServiceImpl(Logging logging, String logId) {
        Assert.notNull(logId, "logId is required");
        this.logging = logging;
        this.logId = logId;
    }


    /**
     * Direct instance of OCI Java SDK Logging Client.
     * @return Logging
     */
    @Override
    public Logging getClient() {
        return logging;
    }

    /**
     * Ingest logs associated with a Log OCID.
     * @param logText Content of the log to be ingested
     * @return PutLogsResponse
     */
    public PutLogsResponse putLog(String logText) {

        PutLogsDetails putLogsDetails = PutLogsDetails.builder()
                .specversion(LOG_SPEC_VERSION)
                .logEntryBatches(new ArrayList<>(Arrays.asList(LogEntryBatch.builder()
                        .entries(new ArrayList<>(Arrays.asList(LogEntry.builder()
                                .data(logText)
                                .id(UUID.randomUUID().toString())
                                .time(new Date()).build())))
                        .source(LOG_SOURCE)
                        .type(LOG_TYPE)
                        .subject(SUBJECT)
                        .defaultlogentrytime(new Date()).build()))).build();

        PutLogsRequest putLogsRequest = PutLogsRequest.builder()
                .logId(logId)
                .putLogsDetails(putLogsDetails)
                .build();
        PutLogsResponse response = logging.putLogs(putLogsRequest);
        return response;
    }
}
