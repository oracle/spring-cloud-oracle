// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.adb;

import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.responses.CreateAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.DeleteAutonomousDatabaseResponse;

/**
 * Interface for OCI Autonomous Database module.
 */
public interface AutonomousDatabase {

    /**
     * Direct instance of OCI Java SDK DatabaseClient.
     * @return DatabaseClient
     */
    DatabaseClient getDatatbaseClient();

    /**
     * Create an Autonomous Database.
     * 
     * @param databaseName Name of the Autonomous Database to be created
     * @param compartmentId Compartment OCID where the Autonomous Database needs to be created
     * @return CreateAutonomousDatabaseResponse
     */
    CreateAutonomousDatabaseResponse createAutonomousDatabase(
        String databaseName, 
        String compartmentId, 
        String adminPassword,
        Integer dataStorageSizeInGBs,
        Float computeCount
        );

    /**
     * Get details of an Autonomous Database.
     * 
     * @param databaseId OCID of the Autonomous Database to get details of
     * @return GetAutonomousDatabaseResponse
     */
    GetAutonomousDatabaseResponse getAutonomousDatabase(String databaseId);

    /**
     * Generate a wallet for an Autonomous Database.
     * 
     * @param databaseId OCID of the Autonomous Database to get generate a wallet for
     * @param password Password for the wallet
     * @return GenerateAutonomousDatabaseWalletResponse
     */
    GenerateAutonomousDatabaseWalletResponse generateAutonomousDatabaseWallet(String databaseId, String password);
    
    /**
     * Delete an Autonomous Database.
     * 
     * @param databaseId OCID of the Autonomous Database to be deleted
     * @return DeleteAutonomousDatabaseResponse
     */
    DeleteAutonomousDatabaseResponse deleteAutonomousDatabase(String databaseId);

}