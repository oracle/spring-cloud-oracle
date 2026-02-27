/*
 ** TxEventQ Support for Spring Cloud Stream
 ** Copyright (c) 2023, 2026 Oracle and/or its affiliates.
 **
 ** This file has been modified by Oracle Corporation.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oracle.database.spring.cloud.stream.binder;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;

import oracle.ucp.jdbc.PoolDataSource;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;

public class Util {
	@SuppressWarnings("resource")
    public static OracleContainer oracleContainer() {
        return new OracleContainer("gvenzl/oracle-free:23.26.0-slim-faststart")
                .withStartupTimeout(Duration.ofMinutes(2)) // Needed for M1 Mac
                .withUsername("testuser")
                .withPassword("testpwd");
    }

    public static void startOracleContainer(OracleContainer oracleContainer) throws IOException, InterruptedException {
        oracleContainer.start();
        oracleContainer.copyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/tmp/init.sql");
        oracleContainer.execInContainer("sqlplus", "sys / as sysdba", "@/tmp/init.sql");
    }

    public static void configurePoolDataSource(PoolDataSource ds, OracleContainer oracleContainer) throws SQLException {
        ds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        ds.setConnectionPoolName(UUID.randomUUID().toString());
        ds.setURL(oracleContainer.getJdbcUrl());
        ds.setUser(oracleContainer.getUsername());
        ds.setPassword(oracleContainer.getPassword());
    }
}
