// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.sample.adb.springcloudociadbsample;

import com.oracle.bmc.database.model.AutonomousDatabase;
import com.oracle.bmc.database.responses.CreateAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.DeleteAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseResponse;
import com.oracle.cloud.spring.adb.AutonomousDb;
import com.oracle.cloud.spring.adb.AutonomousDbDetails;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demoapp/api/adb")
@Tag(name = "Autonomous Database APIs")
public class AdbController {
    
    @Autowired
    AutonomousDb autonomousDatabase;

    @PostMapping
    ResponseEntity<?> createAutonomousDatabase(
        @Parameter(required = true, example = "databaseName") @RequestParam String databaseName,
        @Parameter(required = true, example = "compartmentId") @RequestParam String compartmentId,
        @Parameter(required = true, example = "adminPassword") @RequestParam String adminPassword,
        @Parameter(required = true, example = "200") @RequestParam Integer dataStorageSizeInGBs,
        @Parameter(required = true, example = "2.0") @RequestParam Float computeCount
    ) {
        CreateAutonomousDatabaseResponse response = autonomousDatabase.createAutonomousDatabase(
            databaseName, compartmentId, adminPassword, dataStorageSizeInGBs, computeCount);
        return ResponseEntity.accepted().body("opcRequestId : " + response.getOpcRequestId());
    }

    @GetMapping
    ResponseEntity<?> getAutonomousDatabase(@Parameter(required = true, example = "databaseId") @RequestParam String databaseId) {
        AutonomousDbDetails response = autonomousDatabase.getAutonomousDatabase(databaseId);
        //System.out.println("###MARK###\n" + response);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/wallet")
    ResponseEntity<?> getAutonomousDatabaseWallet(
        @Parameter(required = true, example = "databaseId") @RequestParam String databaseId,
        @Parameter(required = true, example = "password") @RequestParam String password
    ) {
        GenerateAutonomousDatabaseWalletResponse response = autonomousDatabase.generateAutonomousDatabaseWallet(databaseId, password);
        return ResponseEntity.ok().body("opcRequestId fo generating wallet : " + response.getOpcRequestId());
    }

    @DeleteMapping
    ResponseEntity<?> deleteAutonomousDatabase(@Parameter(required = true, example ="databaseId") @RequestParam String databaseId) {
        DeleteAutonomousDatabaseResponse response = autonomousDatabase.deleteAutonomousDatabase(databaseId);
        return ResponseEntity.ok().body("opcRequestId for deleting the database : " +response.getOpcRequestId());
    }

}
