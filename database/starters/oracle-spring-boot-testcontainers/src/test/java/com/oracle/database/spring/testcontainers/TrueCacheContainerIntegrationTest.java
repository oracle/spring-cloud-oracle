// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.Base64;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/** Live integration coverage for the primary-to-True-Cache setup. */
@Testcontainers(disabledWithoutDocker = true)
class TrueCacheContainerIntegrationTest {

    private static final String PRIMARY_ALIAS = "pri-db-free";
    private static final String CACHE_ALIAS = "tru-cc-free";
    private static final String PRIMARY_PDB = "FREEPDB1";
    private static final String PRIMARY_SERVICE = "FREEPDB1";
    private static final String TRUE_CACHE_SERVICE = "FREEPDB1_TC";
    private static final String PRIMARY_CONFIGURATION_SCRIPT =
            "/home/oracle/configure-primary-truecache-service.sh";
    private static final Network NETWORK = Network.newNetwork();
    private static final OracleContainerSecrets SECRETS =
            OracleContainerSecrets.withOraclePassword(OracleContainer.DEFAULT_PASSWORD);
    private static final OracleContainer PRIMARY = new OracleContainer(OracleContainer.IMAGE_NAME + ":latest")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withNetwork(NETWORK)
            .withNetworkAliases(PRIMARY_ALIAS)
            .withOracleSecrets(SECRETS)
            .withArchiveLog(true)
            .withForceLogging(true);
    private static final TrueCacheContainer CACHE = new TrueCacheContainer()
            .withStartupTimeout(Duration.ofMinutes(5))
            .withNetwork(NETWORK)
            .withNetworkAliases(CACHE_ALIAS)
            .withOracleSecrets(SECRETS)
            .withPrimaryDatabase(PRIMARY_ALIAS, OracleContainer.ORACLE_PORT,
                    TrueCacheContainer.DEFAULT_PRIMARY_SERVICE)
            .withPdbService(PRIMARY_PDB, PRIMARY_SERVICE, TRUE_CACHE_SERVICE);

    private static Path primaryPasswordFile;
    private static Path primaryConfigurationScript;

    @BeforeAll
    static void startTopology() throws IOException, InterruptedException {
        PRIMARY.start();
        primaryPasswordFile = Files.createTempFile("true-cache-primary-", ".pw");
        PRIMARY.copyFileFromContainer(TrueCacheContainer.PRIMARY_SOURCE_PASSWORD_FILE,
                primaryPasswordFile.toString());
        CACHE.withPrimaryDatabasePasswordFile(primaryPasswordFile).start();

        primaryConfigurationScript = Files.createTempFile("true-cache-primary-config-", ".sh");
        CACHE.copyFileFromContainer(PRIMARY_CONFIGURATION_SCRIPT, primaryConfigurationScript.toString());
        PRIMARY.copyFileToContainer(
                MountableFile.forHostPath(primaryConfigurationScript, 0555), PRIMARY_CONFIGURATION_SCRIPT);
        Container.ExecResult result = PRIMARY.execInContainer(
                "bash", PRIMARY_CONFIGURATION_SCRIPT,
                PRIMARY_SERVICE, TRUE_CACHE_SERVICE, PRIMARY_PDB,
                CACHE_ALIAS + ":" + OracleContainer.ORACLE_PORT + "/TRUEFREE",
                OracleContainer.DEFAULT_SID, OracleContainer.DEFAULT_SID, "false",
                "B64:" + Base64.getEncoder().encodeToString(
                        SECRETS.getOraclePassword().getBytes(StandardCharsets.UTF_8)));
        assertEquals(0, result.getExitCode(), result.getStderr());
    }

    @AfterAll
    static void stopTopology() throws IOException {
        try {
            CACHE.stop();
            PRIMARY.stop();
        } finally {
            if (primaryPasswordFile != null) {
                Files.deleteIfExists(primaryPasswordFile);
            }
            if (primaryConfigurationScript != null) {
                Files.deleteIfExists(primaryConfigurationScript);
            }
            SECRETS.close();
            NETWORK.close();
        }
    }

    @Test
    void readsPrimaryDataThroughTheTrueCacheService() throws Exception {
        try (Connection primaryConnection = PRIMARY.createConnection("");
             Statement statement = primaryConnection.createStatement()) {
            primaryConnection.setAutoCommit(false);
            statement.execute("CREATE TABLE true_cache_test (id NUMBER PRIMARY KEY, name VARCHAR2(30))");
            statement.execute("INSERT INTO true_cache_test VALUES (1, 'Ada')");
            primaryConnection.commit();
        }

        assertTrue(awaitCachedRow(), "Expected True Cache to apply the committed primary row");
    }

    private static boolean awaitCachedRow() throws Exception {
        long deadline = System.nanoTime() + Duration.ofMinutes(2).toNanos();
        do {
            try (Connection cacheConnection = CACHE.createConnection("");
                 Statement statement = cacheConnection.createStatement();
                 ResultSet rows = statement.executeQuery(
                         "SELECT name FROM " + OracleContainer.DEFAULT_USERNAME
                                 + ".true_cache_test WHERE id = 1")) {
                if (rows.next()) {
                    assertEquals("Ada", rows.getString(1));
                    return true;
                }
            } catch (java.sql.SQLException ignored) {
                // Service creation and redo apply are asynchronous after cache startup.
            }
            Thread.sleep(1_000);
        } while (System.nanoTime() < deadline);
        return false;
    }
}
