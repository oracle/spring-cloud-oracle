// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.adb;

import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.responses.*;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


public class AutonomousDatabaseImplTests {

    final DatabaseClient client = mock(DatabaseClient.class);

    final AutonomousDatabase autonomousDatabase = new AutonomousDatabaseImpl(client);

    @Test
    void testDatabaseClient() {
        when(client.createAutonomousDatabase(any())).thenReturn(mock(CreateAutonomousDatabaseResponse.class));
    }

}