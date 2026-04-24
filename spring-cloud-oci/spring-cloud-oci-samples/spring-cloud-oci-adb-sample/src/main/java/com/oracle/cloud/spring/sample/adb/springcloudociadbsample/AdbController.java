// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.sample.adb.springcloudociadbsample;

import com.oracle.bmc.database.responses.CreateAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.DeleteAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.StartAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.StopAutonomousDatabaseResponse;
import com.oracle.cloud.spring.adb.AutonomousDb;
import com.oracle.cloud.spring.adb.AutonomousDbDetails;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        @Parameter(required = true) @RequestBody CreateAutonomousDatabaseRequest request
    ) {
        CreateAutonomousDatabaseResponse response = autonomousDatabase.createAutonomousDatabase(
            request.getDatabaseName(),
            request.getCompartmentId(),
            request.getAdminPassword(),
            request.getDisplayName(),
            request.getDataStorageSizeInGBs(),
            request.getComputeCount());
        var adb = response.getAutonomousDatabase();
        var result = Map.of(
            "opcRequestId", response.getOpcRequestId(),
            "autonomousDatabaseOcid", adb.getId(),
            "displayName", adb.getDisplayName(),
            "lifecycleState", adb.getLifecycleState().getValue()
        );

        return ResponseEntity.accepted().body(result);
    }

    @GetMapping
    ResponseEntity<?> getAutonomousDatabase(@Parameter(required = true, example = "databaseId") @RequestParam String databaseId) {
        AutonomousDbDetails response = autonomousDatabase.getAutonomousDatabase(databaseId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/wallet")
    ResponseEntity<?> getAutonomousDatabaseWallet(
        @Parameter(required = true) @RequestBody GenerateAutonomousDatabaseWalletRequest request
    ) {
        GenerateAutonomousDatabaseWalletResponse response = autonomousDatabase.generateAutonomousDatabaseWallet(
            request.getDatabaseId(),
            request.getPassword());
        InputStreamResource isr = new InputStreamResource(response.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(response.getContentLength());
        return ResponseEntity.ok().headers(headers).body(isr);
    }

    @DeleteMapping
    ResponseEntity<?> deleteAutonomousDatabase(@Parameter(required = true, example ="databaseId") @RequestParam String databaseId) {
        DeleteAutonomousDatabaseResponse response = autonomousDatabase.deleteAutonomousDatabase(databaseId);
        return ResponseEntity.ok().body("opcRequestId for deleting the database : " +response.getOpcRequestId());
    }

    @PostMapping("/start")
    ResponseEntity<?> startAutonomousDatabase(@Parameter(required = true, example = "databaseId") @RequestParam String databaseId) {
        StartAutonomousDatabaseResponse response = autonomousDatabase.startAutonomousDatabase(databaseId);
        var adb = response.getAutonomousDatabase();
        var result = Map.of(
            "opcRequestId", response.getOpcRequestId(),
            "autonomousDatabaseOcid", adb.getId(),
            "displayName", adb.getDisplayName(),
            "lifecycleState", adb.getLifecycleState().getValue()
        );
        return ResponseEntity.accepted().body(result);
    }

    @PostMapping("/stop")
    ResponseEntity<?> stopAutonomousDatabase(@Parameter(required = true, example = "databaseId") @RequestParam String databaseId) {
        StopAutonomousDatabaseResponse response = autonomousDatabase.stopAutonomousDatabase(databaseId);
        var adb = response.getAutonomousDatabase();
        var result = Map.of(
            "opcRequestId", response.getOpcRequestId(),
            "autonomousDatabaseOcid", adb.getId(),
            "displayName", adb.getDisplayName(),
            "lifecycleState", adb.getLifecycleState().getValue()
        );
        return ResponseEntity.accepted().body(result);
    }

    static class CreateAutonomousDatabaseRequest {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "databaseName")
        private String databaseName;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "compartmentId")
        private String compartmentId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "adminPassword", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        private String adminPassword;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "displayName")
        private String displayName;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
        private Integer dataStorageSizeInGBs;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2.0")
        private Float computeCount;

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getCompartmentId() {
            return compartmentId;
        }

        public void setCompartmentId(String compartmentId) {
            this.compartmentId = compartmentId;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Integer getDataStorageSizeInGBs() {
            return dataStorageSizeInGBs;
        }

        public void setDataStorageSizeInGBs(Integer dataStorageSizeInGBs) {
            this.dataStorageSizeInGBs = dataStorageSizeInGBs;
        }

        public Float getComputeCount() {
            return computeCount;
        }

        public void setComputeCount(Float computeCount) {
            this.computeCount = computeCount;
        }
    }

    static class GenerateAutonomousDatabaseWalletRequest {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "databaseId")
        private String databaseId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "password", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        private String password;

        public String getDatabaseId() {
            return databaseId;
        }

        public void setDatabaseId(String databaseId) {
            this.databaseId = databaseId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
