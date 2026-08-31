// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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
        schemas.add(new SchemaConfiguration(
                requireNonBlank(username, "Schema username is required"),
                requireNonBlank(password, "Schema password is required"),
                requireNonBlank(connectDescriptor, "Schema connect descriptor is required")));
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
        String command = String.format(
                "printf 'WHENEVER SQLERROR EXIT SQL.SQLCODE\\nEXECUTE ORDS.ENABLE_SCHEMA;\\nEXIT;\\n' | sql -s %s",
                shellQuote(schema.username() + "/" + schema.password() + "@" + schema.connectDescriptor()));

        try {
            execInContainerOrThrow("ORDS schema enablement failed", "bash", "-lc", command);
        } catch (IOException e) {
            throw new ContainerLaunchException("Failed to run ORDS schema enablement command", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ContainerLaunchException("Interrupted while enabling ORDS schema", e);
        }
    }

    private void execInContainerOrThrow(String failureMessage, String... command)
            throws IOException, InterruptedException {
        ExecResult result = execInContainer(command);
        if (result.getExitCode() == 0) {
            return;
        }

        throw new ContainerLaunchException(
                failureMessage + ".\nstdout:\n" + result.getStdout() + "\nstderr:\n" + result.getStderr());
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

    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private record SchemaConfiguration(String username, String password, String connectDescriptor) {
    }
}
