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

public class LogServiceImplTests {

    @Test
    public void testLogServiceImplWithNullLogId() {
        LogService logService = getLogService(null);
        assertThrows(NullPointerException.class, ()-> { logService.putLog("sample log");});
    }

    @Test
    public void testLogServiceImplWithNonNullLogId() {
        LogService logService = getLogService("demoLogId");
        assertNotNull(logService.putLog("sample log"));
    }

    private LogService getLogService(String logId) {
        if (logId == null) {
            Logging logging = getMockedLogging();
            when(logging.putLogs(any())).thenCallRealMethod();
            LogService logService = new LogServiceImpl(logging, logId);
            return logService;
        }

        LogService logService = mock(LogService.class);
        PutLogsResponse response = mock(PutLogsResponse.class);
        when(logService.putLog(anyString())).thenReturn(response);
        return logService;
    }

    private Logging getMockedLogging() {
        Logging logging = mock(LoggingClient.class);
        return logging;
    }
}
