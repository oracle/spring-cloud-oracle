// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers implementation for Oracle REST Data Services (ORDS).
 *
 * <p>The container requires an Oracle AI Database connection string and
 * administrator password. Schemas registered with {@link #withSchema(String,
 * String, String)} are REST-enabled after ORDS starts.</p>
 */
public class OrdsContainer extends GenericContainer<OrdsContainer> {

    /** Official ORDS container image. */
    public static final String DEFAULT_IMAGE = "container-registry.oracle.com/database/ords:latest";
    /** ORDS HTTP port. */
    public static final int HTTP_PORT = 8080;
    /** ORDS HTTPS port. */
    public static final int HTTPS_PORT = 8443;
    /** ORDS MongoDB API port. */
    public static final int MONGODB_API_PORT = 27017;

    private static final String CONNECTION_STRING_ENV = "CONN_STRING";
    private static final String ORACLE_PASSWORD_ENV = "ORACLE_PWD";
    private static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofMinutes(5);
    private static final String ORACLE_IDENTIFIER_PATTERN = "[A-Za-z][A-Za-z0-9_$#]{0,127}";
    private static final String SCHEMA_SCRIPT_PREFIX = "/tmp/ords-enable-schema-";
    private static final int OWNER_READ_WRITE_FILE_MODE = 0600;
    private static final String ROOT_USER = "0";

    private final List<SchemaConfiguration> schemas = new ArrayList<>();

    /** Creates a container using the official ORDS image. */
    public OrdsContainer() {
        this(DockerImageName.parse(DEFAULT_IMAGE));
    }

    /**
     * Creates a container using an image name.
     *
     * @param dockerImageName ORDS image name
     */
    public OrdsContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /**
     * Creates a container using a parsed image name.
     *
     * @param dockerImageName ORDS image name
     */
    public OrdsContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(HTTP_PORT, HTTPS_PORT, MONGODB_API_PORT);
        waitingFor(Wait.forHttp("/")
                .forPort(HTTP_PORT)
                .forStatusCodeMatching(status -> status >= 200 && status < 500)
                .withStartupTimeout(DEFAULT_STARTUP_TIMEOUT));
    }

    /**
     * Configures the JDBC connection string ORDS uses to install and connect.
     *
     * @param connectionString JDBC connection string reachable from the ORDS container
     * @return this container
     */
    public OrdsContainer withDatabaseConnectionString(String connectionString) {
        return withEnv(CONNECTION_STRING_ENV, requireNonBlank(
                connectionString,
                "Database connection string cannot be null or empty"));
    }

    /**
     * Configures the Oracle AI Database administrator password used by ORDS.
     *
     * @param oraclePassword administrator password
     * @return this container
     */
    public OrdsContainer withOraclePassword(String oraclePassword) {
        return withEnv(ORACLE_PASSWORD_ENV, requireNonBlank(
                oraclePassword,
                "Oracle password cannot be null or empty"));
    }

    /**
     * Registers a database schema to enable after ORDS starts.
     *
     * @param username schema username
     * @param password schema password
     * @param connectDescriptor database connection descriptor reachable from the ORDS container
     * @return this container
     */
    public OrdsContainer withSchema(String username, String password, String connectDescriptor) {
        username = requireNonBlank(username, "Schema username is required");
        password = requireNonBlank(password, "Schema password is required");
        connectDescriptor = requireNonBlank(connectDescriptor, "Schema connect descriptor is required");

        requireUnquotedIdentifier(username);
        requireValidPassword(password);
        requireSingleLine(connectDescriptor, "Schema connect descriptor must be a single line");
        schemas.add(new SchemaConfiguration(
                username,
                password,
                connectDescriptor));
        return self();
    }

    /**
     * Returns the mapped ORDS HTTP URL.
     *
     * @return base HTTP URL
     */
    public String getBaseUrl() {
        return "http://" + getHost() + ":" + getHttpPort();
    }

    /**
     * Returns the mapped ORDS HTTP port.
     *
     * @return HTTP port
     */
    public int getHttpPort() {
        return getMappedPort(HTTP_PORT);
    }

    /**
     * Returns the mapped ORDS HTTPS port.
     *
     * @return HTTPS port
     */
    public int getHttpsPort() {
        return getMappedPort(HTTPS_PORT);
    }

    /**
     * Returns the mapped ORDS MongoDB API port.
     *
     * @return MongoDB API port
     */
    public int getMongoDbApiPort() {
        return getMappedPort(MONGODB_API_PORT);
    }

    @Override
    public void start() {
        validateRequiredEnv(CONNECTION_STRING_ENV);
        validateRequiredEnv(ORACLE_PASSWORD_ENV);
        super.start();
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        schemas.forEach(this::enableSchema);
    }

    private void enableSchema(SchemaConfiguration schema) {
        String scriptPath = SCHEMA_SCRIPT_PREFIX + java.util.UUID.randomUUID() + ".sql";
        ContainerLaunchException failure = null;

        try {
            copyFileToContainer(
                    Transferable.of(createEnableSchemaScript(schema), OWNER_READ_WRITE_FILE_MODE),
                    scriptPath);
            execInContainerOrThrow("ORDS schema enablement failed", "sql", "-s", "/nolog", "@" + scriptPath);
        } catch (IOException e) {
            failure = new ContainerLaunchException("Failed to run ORDS schema enablement command", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failure = new ContainerLaunchException("Interrupted while enabling ORDS schema", e);
        } catch (ContainerLaunchException e) {
            failure = e;
        } catch (RuntimeException e) {
            failure = new ContainerLaunchException("Failed to prepare ORDS schema enablement", e);
        }

        ContainerLaunchException cleanupFailure = removeSchemaScript(scriptPath);
        if (failure != null) {
            if (cleanupFailure != null) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
        if (cleanupFailure != null) {
            throw cleanupFailure;
        }
    }

    private ContainerLaunchException removeSchemaScript(String scriptPath) {
        try {
            execInContainerOrThrow(
                    "Failed to remove temporary ORDS schema credential script",
                    "rm", "-f", scriptPath);
            return null;
        } catch (IOException e) {
            return new ContainerLaunchException("Failed to remove temporary ORDS schema credential script", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ContainerLaunchException(
                    "Interrupted while removing temporary ORDS schema credential script", e);
        } catch (ContainerLaunchException e) {
            return e;
        } catch (RuntimeException e) {
            return new ContainerLaunchException("Failed to remove temporary ORDS schema credential script", e);
        }
    }

    private void execInContainerOrThrow(String failureMessage, String... command)
            throws IOException, InterruptedException {
        // Testcontainers copies the 0600 script as root, so the same user must read and remove it.
        ExecResult result = execInContainer(ExecConfig.builder()
                .user(ROOT_USER)
                .command(command)
                .build());
        if (result.getExitCode() == 0) {
            return;
        }

        throw new ContainerLaunchException(failureMessage + " (exit code " + result.getExitCode() + ")");
    }

    private void validateRequiredEnv(String envName) {
        if (isBlank(getEnvMap().get(envName))) {
            throw new IllegalStateException(envName + " must be configured before starting ORDS");
        }
    }

    private static String requireNonBlank(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void requireUnquotedIdentifier(String username) {
        if (!username.matches(ORACLE_IDENTIFIER_PATTERN)) {
            throw new IllegalArgumentException(
                    "Schema username must be a valid unquoted Oracle AI Database identifier");
        }
    }

    private static void requireValidPassword(String password) {
        if (password.indexOf('"') >= 0 || containsLineBreak(password)) {
            throw new IllegalArgumentException("Schema password cannot contain double quotes or line breaks");
        }
    }

    private static void requireSingleLine(String value, String message) {
        if (containsLineBreak(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean containsLineBreak(String value) {
        return value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
    }

    private static String createEnableSchemaScript(SchemaConfiguration schema) {
        return """
                WHENEVER SQLERROR EXIT SQL.SQLCODE
                SET DEFINE OFF
                CONNECT %s/\"%s\"@%s
                EXECUTE ORDS.ENABLE_SCHEMA;
                EXIT;
                """.formatted(schema.username(), schema.password(), schema.connectDescriptor());
    }

    private record SchemaConfiguration(String username, String password, String connectDescriptor) {
    }
}
