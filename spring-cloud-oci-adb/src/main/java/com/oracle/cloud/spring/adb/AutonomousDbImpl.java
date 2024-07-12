// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.adb;

import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.AutonomousDatabase;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.GenerateAutonomousDatabaseWalletDetails;
import com.oracle.bmc.database.responses.CreateAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.DeleteAutonomousDatabaseResponse;
import com.oracle.bmc.database.requests.GetAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.GenerateAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.requests.CreateAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.DeleteAutonomousDatabaseRequest;

/**
 * Implementation for the OCI Autonomous Database module.
 */
public class AutonomousDbImpl implements AutonomousDb {

    final DatabaseClient client;

    public AutonomousDbImpl(DatabaseClient client) {
        this.client = client;
    }

    /**
     * Direct instance of OCI Java SDK DatabaseClient.
     * @return DatabaseClient
     */
    public DatabaseClient getDatabaseClient() {
        return client;
    }

    /**
     * Create an Autonomous Database.
     * 
     * @param databaseName Name of the Autonomous Database to be created
     * @param compartmentId Compartment OCID where the Autonomous Database needs to be created
     * @return CreateAutonomousDatabaseResponse
     */
    public CreateAutonomousDatabaseResponse createAutonomousDatabase(
        String databaseName, 
        String compartmentId, 
        String adminPassword,
        Integer dataStorageSizeInGBs,
        Float computeCount
    ) {
        CreateAutonomousDatabaseRequest createAutonomousDatabaseRequest = CreateAutonomousDatabaseRequest.builder()
            .createAutonomousDatabaseDetails(CreateAutonomousDatabaseDetails.builder()
                .compartmentId(compartmentId)
                .dbName(databaseName)
                .adminPassword(adminPassword)
                .dataStorageSizeInGBs(dataStorageSizeInGBs)
                .computeModel(CreateAutonomousDatabaseBase.ComputeModel.Ecpu)
                .computeCount(computeCount)
                .build())
            .build();

        CreateAutonomousDatabaseResponse response = client.createAutonomousDatabase(createAutonomousDatabaseRequest);

        return response;
    }

    /**
     * Get details of an Autonomous Database.
     * 
     * @param databaseId OCID of the Autonomous Database to get details of
     * @return GetAutonomousDatabaseResponse
     */
    public AutonomousDbDetails getAutonomousDatabase(String databaseId) {
        GetAutonomousDatabaseRequest getAutonomousDatabaseRequest = GetAutonomousDatabaseRequest.builder()
		    .autonomousDatabaseId(databaseId)
		    .build();

        GetAutonomousDatabaseResponse response = client.getAutonomousDatabase(getAutonomousDatabaseRequest);
        AutonomousDatabase adb = response.getAutonomousDatabase();

        // work around the jackson deserialization issue in oci-java-sdk 3.44.2 - cannot handle explicitlySet - no filter
        AutonomousDbDetails add = new AutonomousDbDetails(
            adb.getCompartmentId(),
            adb.getDisplayName(),
            adb.getId(),
            adb.getDbName(),
            adb.getLifecycleState().toString(),
            adb.getTimeCreated().toString(),
            adb.getComputeCount(),
            adb.getDataStorageSizeInGBs(),
            adb.getLicenseModel().toString(),
            adb.getServiceConsoleUrl()
        );
   
        return add;
    }

    /**
     * Generate a wallet for an Autonomous Database.
     * 
     * @param databaseId OCID of the Autonomous Database to get generate a wallet for
     * @param password Password for the wallet
     * @return GenerateAutonomousDatabaseWalletResponse
     */
    public GenerateAutonomousDatabaseWalletResponse generateAutonomousDatabaseWallet(String databaseId, String password) {
        GenerateAutonomousDatabaseWalletDetails generateAutonomousDatabaseWalletDetails = GenerateAutonomousDatabaseWalletDetails.builder()
            .generateType(GenerateAutonomousDatabaseWalletDetails.GenerateType.All)
            .password(password)
            .isRegional(true).build();

        GenerateAutonomousDatabaseWalletRequest generateAutonomousDatabaseWalletRequest = GenerateAutonomousDatabaseWalletRequest.builder()
            .autonomousDatabaseId(databaseId)
            .generateAutonomousDatabaseWalletDetails(generateAutonomousDatabaseWalletDetails)
            .build();

        GenerateAutonomousDatabaseWalletResponse response = client.generateAutonomousDatabaseWallet(generateAutonomousDatabaseWalletRequest);

        return response;
    }
    
    /**
     * Delete an Autonomous Database.
     * 
     * @param databaseId OCID of the Autonomous Database to be deleted
     * @return DeleteAutonomousDatabaseResponse
     */
    public DeleteAutonomousDatabaseResponse deleteAutonomousDatabase(String databaseId) {
        DeleteAutonomousDatabaseRequest deleteAutonomousDatabaseRequest = DeleteAutonomousDatabaseRequest.builder()
		    .autonomousDatabaseId(databaseId)
		    .build();

        DeleteAutonomousDatabaseResponse response = client.deleteAutonomousDatabase(deleteAutonomousDatabaseRequest); 
        
        return response;
    }

}