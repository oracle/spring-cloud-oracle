// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Testcontainers implementation for an Oracle AI Database Free True Cache instance.
 *
 * <p>Start the primary Oracle AI Database Free container first. Both containers must be
 * attached to the same Testcontainers {@code Network}; use aliases in the primary connection
 * string rather than fixed container IP addresses.</p>
 */
public class TrueCacheContainer extends OracleFreeContainer<TrueCacheContainer> {

    /** Default tag for the Oracle AI Database Free image with True Cache support. */
    public static final String DEFAULT_TAG = "latest";
    /** Default primary database service. */
    public static final String DEFAULT_PRIMARY_SERVICE = "FREE";
    /** Path of the primary password file inside the True Cache container. */
    public static final String PRIMARY_PASSWORD_FILE = "/var/tmp/orapwFREE";
    /** Source path of the password file in the running primary database container. */
    public static final String PRIMARY_SOURCE_PASSWORD_FILE =
            "/opt/oracle/product/26ai/dbhomeFree/dbs/orapwFREE";

    private static final String TRUE_CACHE_ENV = "TRUE_CACHE";
    private static final String PRIMARY_DATABASE_CONNECTION_ENV = "PRIMARY_DB_CONN_STR";
    private static final String PRIMARY_DATABASE_PASSWORD_FILE_ENV = "PRIMARY_DB_PWD_FILE";
    private static final String PDB_TRUE_CACHE_SERVICES_ENV = "PDB_TC_SVCS";
    private static final String TRUE_DATABASE_UNIQUE_NAME_ENV = "TRUEDB_UNIQUE_NAME";
    private static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofMinutes(20);

    private String primaryDatabaseConnection;
    private Path primaryDatabasePasswordFile;
    private final List<PdbService> services = new ArrayList<>();
    private String username = "SYSTEM";
    private String trueDatabaseUniqueName = "TRUEFREE";

    /** Creates a True Cache container from the official Oracle AI Database Free image. */
    public TrueCacheContainer() {
        this(IMAGE_NAME + ":" + DEFAULT_TAG);
    }

    /** Creates a True Cache container from a compatible Oracle AI Database Free image. */
    public TrueCacheContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /** Creates a True Cache container from a parsed compatible image name. */
    public TrueCacheContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        waitingFor(Wait.forListeningPort().withStartupTimeout(DEFAULT_STARTUP_TIMEOUT));
    }

    /** Configures the primary database connection using a network alias or DNS name. */
    public TrueCacheContainer withPrimaryDatabase(String host, int port, String serviceName) {
        requireNotBlank(host, "Primary database host");
        requireNotBlank(serviceName, "Primary database service name");
        if (containsWhitespace(host) || containsWhitespace(serviceName)
                || port < 1 || port > 65535) {
            throw new IllegalArgumentException("Primary database host, port, and service name must be valid single-line values");
        }
        return withPrimaryDatabaseConnection(host + ":" + port + "/" + serviceName);
    }

    /** Configures an already formatted primary database connection descriptor. */
    public TrueCacheContainer withPrimaryDatabaseConnection(String connection) {
        requireNotBlank(connection, "Primary database connection");
        if (containsWhitespace(connection)) {
            throw new IllegalArgumentException("Primary database connection must be a single-line value");
        }
        primaryDatabaseConnection = connection;
        return self();
    }

    /** Sets the unique name assigned to the True Cache database. */
    public TrueCacheContainer withTrueDatabaseUniqueName(String databaseUniqueName) {
        trueDatabaseUniqueName = normalizeOracleIdentifier(databaseUniqueName, "True Cache database unique name");
        if ("FREE".equals(trueDatabaseUniqueName)) {
            throw new IllegalArgumentException("True Cache database unique name must differ from the primary FREE database");
        }
        return self();
    }

    /**
     * Copies the password file extracted from the running primary container before this
     * container starts. The caller retains ownership of the local source file.
     */
    public TrueCacheContainer withPrimaryDatabasePasswordFile(Path passwordFile) {
        if (passwordFile == null || !passwordFile.isAbsolute()) {
            throw new IllegalArgumentException("Primary database password file must be an absolute path");
        }
        primaryDatabasePasswordFile = passwordFile;
        return self();
    }

    /** Adds a primary-PDB-service to True-Cache-service mapping. */
    public TrueCacheContainer withPdbService(String pdbName, String primaryService, String trueCacheService) {
        services.add(new PdbService(
                normalizeOracleIdentifier(pdbName, "PDB name"),
                normalizeOracleIdentifier(primaryService, "Primary service"),
                normalizeOracleIdentifier(trueCacheService, "True Cache service")));
        return self();
    }

    @Override
    public TrueCacheContainer withUsername(String username) {
        this.username = normalizeOracleIdentifier(username, "Username");
        return self();
    }

    @Override
    public String getJdbcUrl() {
        if (services.isEmpty()) {
            throw new IllegalStateException("Configure at least one True Cache service before requesting its JDBC URL");
        }
        return "jdbc:oracle:thin:@//" + getHost() + ":" + getOraclePort() + "/" + services.getFirst().trueCacheService();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getDatabaseName() {
        return DEFAULT_DATABASE_NAME;
    }

    @Override
    protected void configure() {
        if (!hasOracleSecrets()) {
            throw new IllegalStateException("Call withOracleSecrets(...) before starting a True Cache container");
        }
        if (primaryDatabaseConnection == null) {
            throw new IllegalStateException("Call withPrimaryDatabase(...) before starting a True Cache container");
        }
        if (primaryDatabasePasswordFile == null) {
            throw new IllegalStateException("Call withPrimaryDatabasePasswordFile(...) before starting a True Cache container");
        }
        if (services.isEmpty()) {
            throw new IllegalStateException("Configure at least one True Cache service before starting the container");
        }
        super.configure();
        this.withEnv(Map.of(
                TRUE_CACHE_ENV, "true",
                PRIMARY_DATABASE_CONNECTION_ENV, primaryDatabaseConnection,
                PRIMARY_DATABASE_PASSWORD_FILE_ENV, PRIMARY_PASSWORD_FILE,
                PDB_TRUE_CACHE_SERVICES_ENV,
                services.stream().map(PdbService::asEnvironmentValue).collect(Collectors.joining(";")),
                TRUE_DATABASE_UNIQUE_NAME_ENV, trueDatabaseUniqueName,
                ORACLE_PASSWORD_ENV, getPassword()
        )).withCopyToContainer(
                MountableFile.forHostPath(primaryDatabasePasswordFile, OracleContainerSecrets.SECRET_FILE_MODE),
                PRIMARY_PASSWORD_FILE);

    }

    @Override
    public TrueCacheContainer withUrlParam(String paramName, String paramValue) {
        throw new UnsupportedOperationException("True Cache JDBC URL parameters are not supported");
    }

    private static boolean containsWhitespace(String value) {
        return value.chars().anyMatch(Character::isWhitespace);
    }

    private record PdbService(String pdbName, String primaryService, String trueCacheService) {
        String asEnvironmentValue() {
            return pdbName + ":" + primaryService + ":" + trueCacheService;
        }
    }
}
