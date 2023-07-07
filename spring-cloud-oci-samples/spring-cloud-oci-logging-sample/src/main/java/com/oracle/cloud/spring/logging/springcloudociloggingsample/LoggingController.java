/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging.springcloudociloggingsample;

import com.oracle.bmc.loggingingestion.responses.PutLogsResponse;
import com.oracle.cloud.spring.logging.LoggingSvc;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demoapp/api/logging/")
@Tag(name = "logging APIs")
public class LoggingController {

    @Autowired
    LoggingSvc loggingSvc;

    @PostMapping(value = "putlogs")
    ResponseEntity<?> putLogs(@Parameter(required = true, example = "logText") @RequestParam String logText) {
        PutLogsResponse response = loggingSvc.putLogs(logText);
        return ResponseEntity.ok().body("opc request Id for posting the logs : " + response.getOpcRequestId());
    }

}
