// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class TrueCacheContainerTest {

    @Test
    void configuresTheTrueCacheImageContract() throws Exception {
        Path primaryPasswordFile = Files.createTempFile("true-cache-primary-", ".pw");
        try (OracleContainerSecrets secrets = OracleContainerSecrets.withOraclePassword(OracleContainer.DEFAULT_PASSWORD)) {
            TrueCacheContainer container = new TrueCacheContainer()
                    .withOracleSecrets(secrets)
                    .withPrimaryDatabase("pri-db-free", OracleContainer.ORACLE_PORT,
                            TrueCacheContainer.DEFAULT_PRIMARY_SERVICE)
                    .withPrimaryDatabasePasswordFile(primaryPasswordFile)
                    .withPdbService("freepdb1", "sales1", "sales1_tc")
                    .withPdbService("freepdb1", "sales2", "sales2_tc")
                    .withUsername("system");

            container.configure();

            assertEquals("true", container.getEnvMap().get("TRUE_CACHE"));
            assertEquals("pri-db-free:1521/FREE", container.getEnvMap().get("PRIMARY_DB_CONN_STR"));
            assertEquals(TrueCacheContainer.PRIMARY_PASSWORD_FILE,
                    container.getEnvMap().get("PRIMARY_DB_PWD_FILE"));
            assertEquals("TRUEFREE", container.getEnvMap().get("TRUEDB_UNIQUE_NAME"));
            assertEquals("FREEPDB1:SALES1:SALES1_TC;FREEPDB1:SALES2:SALES2_TC",
                    container.getEnvMap().get("PDB_TC_SVCS"));
            assertEquals("SYSTEM", container.getUsername());
            assertEquals(OracleContainer.DEFAULT_PASSWORD, container.getEnvMap().get("ORACLE_PWD"));
        } finally {
            Files.deleteIfExists(primaryPasswordFile);
        }
    }

    @Test
    void rejectsIncompleteTopologyBeforeContainerCreation() {
        assertThrows(IllegalStateException.class, () -> new TrueCacheContainer().configure());

        try (OracleContainerSecrets secrets = OracleContainerSecrets.withOraclePassword(OracleContainer.DEFAULT_PASSWORD)) {
            assertThrows(IllegalStateException.class,
                    () -> new TrueCacheContainer().withOracleSecrets(secrets).configure());
        }
    }

    @Test
    void validatesSecretLifecycleAndSupplementarySecretNames() {
        OracleContainerSecrets secrets = OracleContainerSecrets.withOraclePassword(OracleContainer.DEFAULT_PASSWORD);
        assertThrows(IllegalArgumentException.class, () -> secrets.withSecret("../unsafe", new byte[] {1}));
        secrets.close();
        assertThrows(IllegalStateException.class, secrets::getOraclePassword);
    }
}
