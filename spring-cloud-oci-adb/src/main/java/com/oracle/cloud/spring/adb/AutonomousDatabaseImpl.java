// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.adb;

import com.oracle.bmc.database.DatabaseClient;

/**
 * Implementation for the OCI Autonomous Database module.
 */
public class AutonomousDatabaseImpl implements AutonomousDatabase {

    final DatabaseClient client;

    public AutonomousDatabaseImpl(DatabaseClient client) {
        this.client = client;
    }

}