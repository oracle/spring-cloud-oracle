/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.logging.springcloudociloggingsample;

import com.oracle.bmc.loggingingestion.responses.PutLogsResponse;
import com.oracle.cloud.spring.logging.LogService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demoapp/api/logging/")
@Tag(name = "logging APIs")
public class LoggingController {

    @Autowired
    LogService logService;

    @PostMapping(value = "putlog")
    ResponseEntity<?> putLog(@Parameter(required = true, example = "logText") @RequestParam String logText) {
        PutLogsResponse response = logService.putLog(logText);
        return ResponseEntity.ok().body("opc request Id for posting the logs : " + response.getOpcRequestId());
    }

}
