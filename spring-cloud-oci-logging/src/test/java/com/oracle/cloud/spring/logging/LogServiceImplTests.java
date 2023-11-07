/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging;

import com.oracle.bmc.loggingingestion.Logging;
import com.oracle.bmc.loggingingestion.LoggingClient;
import com.oracle.bmc.loggingingestion.responses.PutLogsResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;

class LogServiceImplTests {

    @Test
    void testLogServiceImplWithNullLogId() {
        assertThrows(IllegalArgumentException.class, ()-> { getLogService(null);});
    }

    @Test
    void testLogServiceImplWithNonNullLogId() {
        LogService logService = getLogService("demoLogId");
        assertNotNull(logService.getClient());
        assertNotNull(logService.putLog("sample log"));
    }

    private LogService getLogService(String logId) {
        Logging logging = getMockedLogging();
        PutLogsResponse response = mock(PutLogsResponse.class);
        when(logging.putLogs(any())).thenReturn(response);
        LogService logService = new LogServiceImpl(logging, logId);
        return logService;
    }

    private Logging getMockedLogging() {
        Logging logging = mock(LoggingClient.class);
        return logging;
    }
}
