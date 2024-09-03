package com.oracle.database.spring.cloud.stream.binder;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;

import oracle.ucp.jdbc.PoolDataSource;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;

public class Util {
    public static OracleContainer oracleContainer() {
        return new OracleContainer("gvenzl/oracle-free:23.5-slim-faststart")
                .withStartupTimeout(Duration.ofMinutes(2)) // Needed for M1 Mac
                .withUsername("testuser")
                .withPassword(("testpwd"));
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
