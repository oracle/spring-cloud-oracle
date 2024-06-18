// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.adb;

import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.responses.CreateAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.DeleteAutonomousDatabaseResponse;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

public class AutonomousDatabaseImplTests {

    final DatabaseClient client = mock(DatabaseClient.class);

    final AutonomousDatabase autonomousDatabase = new AutonomousDatabaseImpl(client);

    @Test
    void testDatabaseClient() {
        when(client.createAutonomousDatabase(any())).thenReturn(mock(CreateAutonomousDatabaseResponse.class));
    }

    @Test
    void testDatbaseImpl() {
        when(client.createAutonomousDatabase(any())).thenReturn(mock(CreateAutonomousDatabaseResponse.class));
        when(client.getAutonomousDatabase(any())).thenReturn(mock(GetAutonomousDatabaseResponse.class));
        when(client.generateAutonomousDatabaseWallet(any())).thenReturn(mock(GenerateAutonomousDatabaseWalletResponse.class));
        when(client.deleteAutonomousDatabase(any())).thenReturn(mock(DeleteAutonomousDatabaseResponse.class));

        CreateAutonomousDatabaseResponse cadr = autonomousDatabase.createAutonomousDatabase("name", "compartment");
        assertNotNull(cadr);

        GetAutonomousDatabaseResponse gadr = autonomousDatabase.getAutonomousDatabase("ocid");
        assertNotNull(gadr);

        GenerateAutonomousDatabaseWalletResponse gadwr = autonomousDatabase.generateAutonomousDatabaseWallet("ocid", "password");
        assertNotNull(gadwr);

        DeleteAutonomousDatabaseResponse dadr = autonomousDatabase.deleteAutonomousDatabase("ocid");
        assertNotNull(dadr);

    }

}